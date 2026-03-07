package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.ProjectDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.repository.ProjectRepository;
import com.nhom3.ct240.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Project createProject(ProjectDTO dto, String currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setOwnerId(currentUserId);
        project.getMemberIds().add(currentUserId); // Owner cũng là member
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        user.getOwnedProjectIds().add(savedProject.getId());
        user.getParticipatingProjectIds().add(savedProject.getId());
        userRepository.save(user);

        return savedProject;
    }

    @Override
    @Transactional
    public Project updateProject(String projectId, ProjectDTO dto, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(currentUserId) && !project.getManagerIds().contains(currentUserId)) {
            throw new RuntimeException("You do not have permission to update this project");
        }

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Only the project owner can delete this project");
        }
        
        // Xóa project khỏi tất cả các thành viên
        project.getMemberIds().forEach(memberId -> {
            userRepository.findById(memberId).ifPresent(user -> {
                user.getParticipatingProjectIds().remove(projectId);
                userRepository.save(user);
            });
        });

        // Xóa project khỏi owner
        User owner = userRepository.findById(project.getOwnerId()).orElse(null);
        if (owner != null) {
            owner.getOwnedProjectIds().remove(projectId);
            userRepository.save(owner);
        }

        projectRepository.delete(project);
    }

    @Override
    public Project getProjectDetail(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getMemberIds().contains(currentUserId)) {
            throw new RuntimeException("You are not a member of this project");
        }

        return project;
    }

    @Override
    public List<Project> getAllProjects(String currentUserId) {
        // Chỉ hiển thị các dự án do người dùng hiện tại sở hữu
        return projectRepository.findByOwnerId(currentUserId);
    }
    @Override
    public List<Project> getAllSystemProjects() {
        return projectRepository.findAll();
    }

    // --- CN_15: Phân quyền quản lý dự án ---

    @Override
    @Transactional
    public Project assignManager(String projectId, String userIdToAssign, String currentUserId) {
        Project project = getProjectAndCheckOwnerPermission(projectId, currentUserId);
        User userToAssign = findUserById(userIdToAssign);

        if (!project.getManagerIds().contains(userIdToAssign)) {
            project.getManagerIds().add(userIdToAssign);
            notificationService.createNotification(userIdToAssign, "Bạn đã được thăng chức làm quản lý dự án: " + project.getName(), null);
        }
        if (!project.getMemberIds().contains(userIdToAssign)) {
            project.getMemberIds().add(userIdToAssign);
            userToAssign.getParticipatingProjectIds().add(projectId);
            userRepository.save(userToAssign);
        }

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project removeManager(String projectId, String userIdToRemove, String currentUserId) {
        Project project = getProjectAndCheckOwnerPermission(projectId, currentUserId);
        
        project.getManagerIds().remove(userIdToRemove);
        notificationService.createNotification(userIdToRemove, "Bạn đã bị xóa khỏi vai trò quản lý dự án: " + project.getName(), null);
        
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project assignMember(String projectId, String userIdToAssign, String currentUserId) {
        Project project = getProjectAndCheckOwnerOrManagerPermission(projectId, currentUserId);
        User userToAssign = findUserById(userIdToAssign);

        if (!project.getMemberIds().contains(userIdToAssign)) {
            project.getMemberIds().add(userIdToAssign);
            userToAssign.getParticipatingProjectIds().add(projectId);
            userRepository.save(userToAssign);
            notificationService.createNotification(userIdToAssign, "Bạn đã được thêm vào dự án: " + project.getName(), null);
        }
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project removeMember(String projectId, String userIdToRemove, String currentUserId) {
        Project project = getProjectAndCheckOwnerOrManagerPermission(projectId, currentUserId);
        User userToRemove = findUserById(userIdToRemove);

        // Owner không thể bị xóa khỏi dự án
        if (project.getOwnerId().equals(userIdToRemove)) {
            throw new RuntimeException("Cannot remove the project owner.");
        }

        project.getMemberIds().remove(userIdToRemove);
        project.getManagerIds().remove(userIdToRemove); // Nếu là manager cũng xóa luôn
        userToRemove.getParticipatingProjectIds().remove(projectId);
        userRepository.save(userToRemove);
        
        notificationService.createNotification(userIdToRemove, "Bạn đã bị xóa khỏi dự án: " + project.getName(), null);

        return projectRepository.save(project);
    }

    // --- CN_16: Tham gia/rời dự án ---

    @Override
    @Transactional
    public void requestToJoinProject(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = findUserById(currentUserId);

        if (project.getMemberIds().contains(currentUserId)) {
            throw new RuntimeException("You are already a member of this project.");
        }
        if (project.getPendingMemberIds().contains(currentUserId)) {
            throw new RuntimeException("You have already requested to join this project.");
        }

        project.getPendingMemberIds().add(currentUserId);
        projectRepository.save(project);
        
        // Gửi thông báo cho Owner
        notificationService.createNotification(project.getOwnerId(), "Có yêu cầu tham gia mới từ " + user.getFullName() + " vào dự án " + project.getName(), null);
        
        // Gửi thông báo cho các Manager
        for (String managerId : project.getManagerIds()) {
            notificationService.createNotification(managerId, "Có yêu cầu tham gia mới từ " + user.getFullName() + " vào dự án " + project.getName(), null);
        }
    }

    @Override
    @Transactional
    public Project approveJoinRequest(String projectId, String userIdToApprove, String currentUserId) {
        Project project = getProjectAndCheckOwnerOrManagerPermission(projectId, currentUserId);

        if (!project.getPendingMemberIds().contains(userIdToApprove)) {
            throw new RuntimeException("User has not requested to join this project.");
        }

        User userToApprove = findUserById(userIdToApprove);

        project.getPendingMemberIds().remove(userIdToApprove);
        project.getMemberIds().add(userIdToApprove);
        userToApprove.getParticipatingProjectIds().add(projectId);
        
        userRepository.save(userToApprove);
        
        notificationService.createNotification(userIdToApprove, "Yêu cầu tham gia dự án " + project.getName() + " của bạn đã được chấp nhận.", null);
        
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project rejectJoinRequest(String projectId, String userIdToReject, String currentUserId) {
        Project project = getProjectAndCheckOwnerOrManagerPermission(projectId, currentUserId);

        if (!project.getPendingMemberIds().contains(userIdToReject)) {
            throw new RuntimeException("User has not requested to join this project.");
        }

        project.getPendingMemberIds().remove(userIdToReject);
        
        notificationService.createNotification(userIdToReject, "Yêu cầu tham gia dự án " + project.getName() + " của bạn đã bị từ chối.", null);
        
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void leaveProject(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User userLeaving = findUserById(currentUserId);

        if (!project.getMemberIds().contains(currentUserId)) {
            throw new RuntimeException("You are not a member of this project.");
        }

        if (project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Project owner cannot leave the project. Please transfer ownership or delete the project.");
        }

        project.getMemberIds().remove(currentUserId);
        project.getManagerIds().remove(currentUserId); // Nếu là manager cũng xóa luôn
        
        User user = findUserById(currentUserId);
        user.getParticipatingProjectIds().remove(projectId);
        userRepository.save(user);
        
        projectRepository.save(project);
        
        // Gửi thông báo cho Owner
        notificationService.createNotification(project.getOwnerId(), "Thành viên " + userLeaving.getFullName() + " đã rời khỏi dự án " + project.getName(), null);
        
        // Gửi thông báo cho các Manager
        for (String managerId : project.getManagerIds()) {
            notificationService.createNotification(managerId, "Thành viên " + userLeaving.getFullName() + " đã rời khỏi dự án " + project.getName(), null);
        }
    }

    // --- Helper Methods ---

    private Project getProjectAndCheckOwnerPermission(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Only the project owner can perform this action.");
        }
        return project;
    }

    private Project getProjectAndCheckOwnerOrManagerPermission(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getOwnerId().equals(currentUserId) && !project.getManagerIds().contains(currentUserId)) {
            throw new RuntimeException("Only the project owner or managers can perform this action.");
        }
        return project;
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to assign/remove not found."));
    }
}
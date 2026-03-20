package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.project.ProjectDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.NotificationType;
import com.nhom3.ct240.entity.enums.Role;
import com.nhom3.ct240.repository.ProjectRepository;
import com.nhom3.ct240.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    private ActivityLogService activityLogService;

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
        
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "private");

        project.setOwnerId(currentUserId);
        project.getMemberIds().add(currentUserId); // Owner cũng là member
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        user.getOwnedProjectIds().add(savedProject.getId());
        user.getParticipatingProjectIds().add(savedProject.getId());
        userRepository.save(user);

        activityLogService.logActivity(
            savedProject.getId(),
            currentUserId,
            "Tạo dự án",
            "Dự án '" + savedProject.getName() + "' vừa được tạo",
            "mdi-rocket-launch",
            "info"
        );

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
        
        if (dto.getStartDate() != null) project.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) project.setEndDate(dto.getEndDate());
        if (dto.getVisibility() != null) project.setVisibility(dto.getVisibility());

        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Cập nhật dự án",
            "Thông tin chung của dự án đã được thay đổi",
            "mdi-information-outline",
            "primary"
        );

        return savedProject;
    }

    @Override
    @Transactional
    public void deleteProject(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = project.getOwnerId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Only the project owner or admin can delete this project");
        }

        project.getMemberIds().forEach(memberId -> {
            if (!memberId.equals(currentUserId)) {
                notificationService.createNotification(
                        memberId,
                        "Dự án [" + project.getName() + "] đã bị xóa bởi quản lý",
                        NotificationType.PROJECT_DELETED, projectId, null
                );
            }
            userRepository.findById(memberId).ifPresent(user -> {
                user.getParticipatingProjectIds().removeIf(id -> id.equals(projectId));
                userRepository.save(user);
            });
        });

        User owner = userRepository.findById(project.getOwnerId()).orElse(null);
        if (owner != null) {
            owner.getOwnedProjectIds().removeIf(id -> id.equals(projectId));
            userRepository.save(owner);
        }

        projectRepository.delete(project);
    }

    @Override
    public Project getProjectDetail(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isMember = project.getMemberIds().contains(currentUserId);
        boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER;

        if (!isMember && !isAdminOrManager) {
             throw new RuntimeException("You do not have permission to view this project");
        }

        return project;
    }

    @Override
    public List<Project> getAllProjects(String currentUserId) {
        List<Project> allProjects = projectRepository.findAll();
        
        return allProjects.stream()
                .filter(p -> 
                    "public".equalsIgnoreCase(p.getVisibility()) || 
                    currentUserId.equals(p.getOwnerId()) || 
                    (p.getMemberIds() != null && p.getMemberIds().contains(currentUserId)) ||
                    (p.getManagerIds() != null && p.getManagerIds().contains(currentUserId))
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<Project> getAllSystemProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional
    public Project assignManager(String projectId, String userIdToAssign, String currentUserId) {
        Project project = getProjectAndCheckOwnerPermission(projectId, currentUserId);
        User userToAssign = findUserById(userIdToAssign);
        User currentUser = findUserById(currentUserId);

        if (!project.getManagerIds().contains(userIdToAssign)) {
            project.getManagerIds().add(userIdToAssign);
            notificationService.createNotification(
                userIdToAssign, 
                "Bạn đã được thăng chức làm quản lý dự án: " + project.getName(), 
                NotificationType.PROJECT_INVITE, 
                projectId, 
                null
            );

            activityLogService.logActivity(
                projectId,
                currentUserId,
                "Cập nhật vai trò",
                userToAssign.getFullName() + " đã được thăng cấp làm Quản lý",
                "mdi-account-star",
                "info"
            );
        }
        if (!project.getMemberIds().contains(userIdToAssign)) {
            project.getMemberIds().add(userIdToAssign);
            userToAssign.getParticipatingProjectIds().add(projectId);
            userRepository.save(userToAssign);
            
            notificationService.createNotification(
                    userIdToAssign,
                    "Bạn được mời tham gia dự án: [" + project.getName() + "] bởi " + currentUser.getFullName(),
                    NotificationType.PROJECT_INVITE, projectId, null
            );

            activityLogService.logActivity(
                projectId,
                currentUserId,
                "Thêm thành viên",
                userToAssign.getFullName() + " đã được thêm vào dự án",
                "mdi-account-plus",
                "success"
            );
        }

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project removeManager(String projectId, String userIdToRemove, String currentUserId) {
        Project project = getProjectAndCheckOwnerPermission(projectId, currentUserId);
        User userToRemove = findUserById(userIdToRemove);
        
        project.getManagerIds().removeIf(id -> id.equals(userIdToRemove));
        notificationService.createNotification(userIdToRemove,
                "Bạn đã bị xóa khỏi vai trò quản lý dự án: " + project.getName(),
                NotificationType.PROJECT_DELETED,
                projectId,
                null);

        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Cập nhật vai trò",
            userToRemove.getFullName() + " đã bị gỡ quyền Quản lý",
            "mdi-account-minus",
            "warning"
        );
        
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
            notificationService.createNotification(userIdToAssign, "Bạn đã được thêm vào dự án: " + project.getName(),NotificationType.PROJECT_INVITE,
                    projectId, null);

            activityLogService.logActivity(
                projectId,
                currentUserId,
                "Thêm thành viên",
                userToAssign.getFullName() + " đã được thêm vào dự án",
                "mdi-account-plus",
                "success"
            );
        }
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project removeMember(String projectId, String userIdToRemove, String currentUserId) {
        Project project = getProjectAndCheckOwnerOrManagerPermission(projectId, currentUserId);
        User userToRemove = findUserById(userIdToRemove);

        if (project.getOwnerId().equals(userIdToRemove)) {
            throw new RuntimeException("Cannot remove the project owner.");
        }

        project.getMemberIds().removeIf(id -> id.equals(userIdToRemove));
        project.getManagerIds().removeIf(id -> id.equals(userIdToRemove));
        userToRemove.getParticipatingProjectIds().removeIf(id -> id.equals(projectId));
        userRepository.save(userToRemove);
        
        notificationService.createNotification(userIdToRemove, "Bạn đã bị xóa khỏi dự án: " + project.getName(),NotificationType.PROJECT_DELETED,
                projectId, null);

        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Xóa thành viên",
            userToRemove.getFullName() + " đã bị xóa khỏi dự án",
            "mdi-account-minus",
            "error"
        );

        return projectRepository.save(project);
    }

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
        
        notificationService.createNotification(project.getOwnerId(), "Có yêu cầu tham gia mới từ " + user.getFullName() + " vào dự án " + project.getName(),NotificationType.MEMBER_REQUEST_JOIN,
                projectId, null);
        
        for (String managerId : project.getManagerIds()) {
            notificationService.createNotification(managerId, "Có yêu cầu tham gia mới từ " + user.getFullName() + " vào dự án " + project.getName(),NotificationType.MEMBER_REQUEST_JOIN,
                    projectId, null);
        }

        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Yêu cầu tham gia",
            user.getFullName() + " đã gửi yêu cầu tham gia dự án",
            "mdi-account-clock",
            "info"
        );
    }

    @Override
    @Transactional
    public void cancelJoinRequest(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getPendingMemberIds().contains(currentUserId)) {
            project.getPendingMemberIds().removeIf(id -> id.equals(currentUserId));
            projectRepository.save(project);
            
            User user = findUserById(currentUserId);
            activityLogService.logActivity(
                projectId,
                currentUserId,
                "Hủy yêu cầu tham gia",
                user.getFullName() + " đã hủy yêu cầu tham gia dự án",
                "mdi-account-cancel",
                "warning"
            );
        }
    }

    @Override
    @Transactional
    public Project approveJoinRequest(String projectId, String userIdToApprove, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isProjectManager = project.getOwnerId().equals(currentUserId) || project.getManagerIds().contains(currentUserId);
        boolean isSystemAdminOrManager = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER;

        if (!isProjectManager && !isSystemAdminOrManager) {
            throw new AccessDeniedException("You do not have permission to approve join requests for this project.");
        }

        if (!project.getPendingMemberIds().contains(userIdToApprove)) {
            throw new RuntimeException("User has not requested to join this project.");
        }

        User userToApprove = findUserById(userIdToApprove);

        project.getPendingMemberIds().removeIf(id -> id.equals(userIdToApprove));
        project.getMemberIds().add(userIdToApprove);
        userToApprove.getParticipatingProjectIds().add(projectId);
        
        userRepository.save(userToApprove);
        
        notificationService.createNotification(userIdToApprove, "Yêu cầu tham gia dự án " + project.getName() + " của bạn đã được chấp nhận.",NotificationType.PROJECT_JOIN_APPROVED,
                projectId, null);
        
        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Duyệt thành viên",
            userToApprove.getFullName() + " đã được phê duyệt vào dự án",
            "mdi-account-check",
            "success"
        );

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project rejectJoinRequest(String projectId, String userIdToReject, String currentUserId) {
        Project project = getProjectAndCheckOwnerOrManagerPermission(projectId, currentUserId);

        if (!project.getPendingMemberIds().contains(userIdToReject)) {
            throw new RuntimeException("User has not requested to join this project.");
        }

        User userToReject = findUserById(userIdToReject);

        project.getPendingMemberIds().removeIf(id -> id.equals(userIdToReject));
        
        notificationService.createNotification(userIdToReject, "Yêu cầu tham gia dự án " + project.getName() + " của bạn đã bị từ chối.",NotificationType.PROJECT_JOIN_REJECTED,
                projectId, null);
        
        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Từ chối thành viên",
            "Yêu cầu tham gia của " + userToReject.getFullName() + " đã bị từ chối",
            "mdi-account-cancel",
            "error"
        );

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

        // Sử dụng removeIf để đảm bảo xóa chính xác ID ra khỏi Collection
        project.getMemberIds().removeIf(id -> id.equals(currentUserId));
        project.getManagerIds().removeIf(id -> id.equals(currentUserId));
        
        User user = findUserById(currentUserId);
        user.getParticipatingProjectIds().removeIf(id -> id.equals(projectId));
        userRepository.save(user);
        
        // Đảm bảo lưu lại Project sau khi đã remove ID
        projectRepository.save(project);
        
        notificationService.createNotification(project.getOwnerId(), "Thành viên " + userLeaving.getFullName() + " đã rời khỏi dự án " + project.getName(),NotificationType.MEMBER_LEFT_PROJECT,
                projectId, null);
        
        for (String managerId : project.getManagerIds()) {
            notificationService.createNotification(managerId, "Thành viên " + userLeaving.getFullName() + " đã rời khỏi dự án " + project.getName(),NotificationType.MEMBER_LEFT_PROJECT,
                    projectId, null);
        }

        activityLogService.logActivity(
            projectId,
            currentUserId,
            "Rời dự án",
            userLeaving.getFullName() + " đã rời khỏi dự án",
            "mdi-account-arrow-right",
            "warning"
        );
    }

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

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

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Project createProject(ProjectDTO dto, String currentUserId) {
        // Kiểm tra user tồn tại
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setOwnerId(currentUserId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        // Thêm owner vào danh sách thành viên (nếu cần thiết theo logic nghiệp vụ)
        // project.getMemberIds().add(currentUserId);

        Project savedProject = projectRepository.save(project);

        // Cập nhật danh sách dự án sở hữu của user
        user.getOwnedProjectIds().add(savedProject.getId());
        userRepository.save(user);

        return savedProject;
    }

    @Override
    @Transactional
    public Project updateProject(String projectId, ProjectDTO dto, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Kiểm tra quyền sở hữu (chỉ owner hoặc manager mới được sửa - tùy logic)
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

        // Chỉ owner mới được xóa dự án
        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Only the project owner can delete this project");
        }

        // Xóa reference trong User (owner)
        User owner = userRepository.findById(project.getOwnerId()).orElse(null);
        if (owner != null) {
            owner.getOwnedProjectIds().remove(projectId);
            userRepository.save(owner);
        }

        // Xóa reference trong các thành viên khác (nếu có lưu)
        // ... (logic xóa reference phức tạp hơn nếu cần)

        projectRepository.delete(project);
    }

    @Override
    public Project getProjectDetail(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Kiểm tra xem user có thuộc dự án không (nếu dự án private)
        boolean isMember = project.getOwnerId().equals(currentUserId) ||
                           project.getManagerIds().contains(currentUserId) ||
                           project.getMemberIds().contains(currentUserId);

        if (!isMember) {
            throw new RuntimeException("You are not a member of this project");
        }

        return project;
    }
}
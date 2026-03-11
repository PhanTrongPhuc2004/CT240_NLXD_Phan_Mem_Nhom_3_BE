package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.task.CreateTaskDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.Role;
import com.nhom3.ct240.entity.enums.TaskStatus;
import com.nhom3.ct240.repository.ProjectRepository;
import com.nhom3.ct240.repository.TaskRepository;
import com.nhom3.ct240.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    // === LOGIC PHÂN QUYỀN ĐÃ CẬP NHẬT ===

    /**
     * Kiểm tra quyền quản lý (Manager/Owner) cho các hành động Ghi/Sửa/Xóa.
     * ADMIN SẼ BỊ CHẶN Ở ĐÂY.
     */
    private void checkManagerPermission(String projectId, String username) {
        User user = getUserByUsername(username);

        // THAY ĐỔI 1: Chặn Admin thực hiện các hành động nguy hiểm.
        if (user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Admin cannot create, update, or delete tasks.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(user.getId()) && !project.getManagerIds().contains(user.getId())) {
            throw new AccessDeniedException("User is not a manager of this project");
        }
    }

    /**
     * Kiểm tra quyền thành viên (Member) cho các hành động Chỉ Đọc.
     * ADMIN VẪN ĐƯỢC PHÉP ĐỌC.
     */
    private void checkMemberPermission(String projectId, String username) {
        User user = getUserByUsername(username);

        // Giữ nguyên: Admin có quyền xem
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(user.getId()) && !project.getManagerIds().contains(user.getId()) && !project.getMemberIds().contains(user.getId())) {
            throw new AccessDeniedException("User is not a member of this project");
        }
    }

    // === CÁC PHƯƠNG THỨC NGHIỆP VỤ ===

    public Task createTask(CreateTaskDTO createTaskDTO, String creatorUsername) {
        checkManagerPermission(createTaskDTO.getProjectId(), creatorUsername);

        if (createTaskDTO.getAssigneeId() != null && !createTaskDTO.getAssigneeId().isEmpty()) {
            Project project = projectRepository.findById(createTaskDTO.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            
            boolean isMember = project.getMemberIds().contains(createTaskDTO.getAssigneeId())
                    || project.getManagerIds().contains(createTaskDTO.getAssigneeId())
                    || project.getOwnerId().equals(createTaskDTO.getAssigneeId());

            if (!isMember) {
                throw new RuntimeException("Assignee is not a member of this project");
            }
        }

        Task task = new Task();
        task.setProjectId(createTaskDTO.getProjectId());
        task.setTitle(createTaskDTO.getTitle());
        task.setDescription(createTaskDTO.getDescription());
        task.setAssigneeId(createTaskDTO.getAssigneeId());
        task.setDeadline(createTaskDTO.getDeadline());
        task.setPriority(createTaskDTO.getPriority());
        task.setStatus(TaskStatus.TO_DO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public Task updateTask(String taskId, Task taskDetails, String editorUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        checkManagerPermission(existingTask.getProjectId(), editorUsername);

        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setDeadline(taskDetails.getDeadline());
        existingTask.setPriority(taskDetails.getPriority());
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    public void deleteTask(String taskId, String deleterUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        checkManagerPermission(existingTask.getProjectId(), deleterUsername);
        taskRepository.deleteById(taskId);
    }

    public Task assignTask(String taskId, String assigneeId, String assignerUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        checkManagerPermission(existingTask.getProjectId(), assignerUsername);

        Project project = projectRepository.findById(existingTask.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        boolean isMember = project.getMemberIds().contains(assigneeId)
                || project.getManagerIds().contains(assigneeId)
                || project.getOwnerId().equals(assigneeId);

        if (!isMember) {
            throw new RuntimeException("Assignee is not a member of this project");
        }

        existingTask.setAssigneeId(assigneeId);
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    public Task updateTaskStatus(String taskId, TaskStatus newStatus, String cancelReason, String updaterUsername) {
        User updater = getUserByUsername(updaterUsername);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // THAY ĐỔI 2: Bỏ qua ngoại lệ cho Admin, áp dụng quy tắc chung cho tất cả.
        Project project = projectRepository.findById(existingTask.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        boolean isManager = project.getOwnerId().equals(updater.getId()) || project.getManagerIds().contains(updater.getId());
        boolean isAssignee = existingTask.getAssigneeId() != null && existingTask.getAssigneeId().equals(updater.getId());

        // Admin sẽ không phải là isManager hoặc isAssignee của task, nên sẽ bị chặn ở đây.
        if (!isManager && !isAssignee) {
            throw new AccessDeniedException("User does not have permission to update task status");
        }

        existingTask.setStatus(newStatus);
        if (newStatus == TaskStatus.CANCELLED) {
            existingTask.setCancelReason(cancelReason);
        } else {
            existingTask.setCancelReason(null);
        }
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    public Optional<Task> findTaskById(String taskId, String viewerUsername) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        taskOpt.ifPresent(task -> checkMemberPermission(task.getProjectId(), viewerUsername));
        return taskOpt;
    }

    public List<Task> findTasksByProjectId(String projectId, String viewerUsername) {
        checkMemberPermission(projectId, viewerUsername);
        return taskRepository.findByProjectId(projectId);
    }
    
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByProjectAndStatus(String projectId, TaskStatus filterStatus, String viewerUsername) {
        checkMemberPermission(projectId, viewerUsername);
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (filterStatus != null) {
            return tasks.stream()
                    .filter(t -> t.getStatus() == filterStatus)
                    .toList();
        }
        return tasks;
    }
}

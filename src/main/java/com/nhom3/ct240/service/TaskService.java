package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.task.CreateTaskDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.NotificationType;
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
    private NotificationService notificationService; // Thêm sự kiện thông báo

    @Autowired
    private ActivityLogService activityLogService; // Thêm ActivityLogService

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

    private void checkManagerPermission(String projectId, String username) {
        User user = getUserByUsername(username);

        // Admin và Manager hệ thống có toàn quyền
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Owner và Manager của dự án cũng có quyền
        if (!project.getOwnerId().equals(user.getId()) && !project.getManagerIds().contains(user.getId())) {
            throw new AccessDeniedException("User is not a manager of this project");
        }
    }

    private void checkMemberPermission(String projectId, String username) {
        User user = getUserByUsername(username);

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getMemberIds().contains(user.getId())) {
            throw new AccessDeniedException("User is not a member of this project");
        }
    }

    public Task createTask(CreateTaskDTO createTaskDTO, String creatorUsername) {
        checkManagerPermission(createTaskDTO.getProjectId(), creatorUsername);
        Project project = projectRepository.findById(createTaskDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User creator = getUserByUsername(creatorUsername); // Lấy user hiện tại
        
        if (createTaskDTO.getAssigneeId() != null && !createTaskDTO.getAssigneeId().isEmpty()) {
            boolean isMember = project.getMemberIds().contains(createTaskDTO.getAssigneeId());
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
        Task savedTask = taskRepository.save(task);

        // Ghi log hoạt động
        activityLogService.logActivity(
            task.getProjectId(),
            creator.getId(),
            "Tạo công việc",
            "Công việc '" + task.getTitle() + "' vừa được tạo",
            "mdi-plus-box",
            "info"
        );

        // Gửi thông báo nếu có người được gán
        if (savedTask.getAssigneeId() != null) {
            notificationService.createNotification(
                    savedTask.getAssigneeId(),
                    "Công việc mới: [" + savedTask.getTitle() + "] trong dự án [" + project.getName() + "]",
                    NotificationType.TASK_ASSIGNED, project.getId(), savedTask.getId()
            );
        }
        return savedTask;
    }

    public Task updateTask(String taskId, Task taskDetails, String editorUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        User editor = getUserByUsername(editorUsername);

        checkManagerPermission(existingTask.getProjectId(), editorUsername);

        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setDeadline(taskDetails.getDeadline());
        existingTask.setPriority(taskDetails.getPriority());
        existingTask.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(existingTask);

        // Ghi log hoạt động
        activityLogService.logActivity(
            existingTask.getProjectId(),
            editor.getId(),
            "Cập nhật công việc",
            "Thông tin công việc '" + existingTask.getTitle() + "' đã được cập nhật",
            "mdi-pencil",
            "warning"
        );

        return savedTask;
    }

    public void deleteTask(String taskId, String deleterUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        User deleter = getUserByUsername(deleterUsername);
        
        checkManagerPermission(existingTask.getProjectId(), deleterUsername);
        taskRepository.deleteById(taskId);

        // Ghi log hoạt động
        activityLogService.logActivity(
            existingTask.getProjectId(),
            deleter.getId(),
            "Xóa công việc",
            "Công việc '" + existingTask.getTitle() + "' đã bị xóa",
            "mdi-delete",
            "error"
        );
    }

    public Task assignTask(String taskId, String assigneeId, String assignerUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        checkManagerPermission(existingTask.getProjectId(), assignerUsername);
        User assigner = getUserByUsername(assignerUsername);

        Project project = projectRepository.findById(existingTask.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        boolean isMember = project.getMemberIds().contains(assigneeId);

        if (!isMember) {
            throw new RuntimeException("Assignee is not a member of this project");
        }

        existingTask.setAssigneeId(assigneeId);
        existingTask.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(existingTask);

        // Tùy chọn: Bạn có thể thêm log activity ở đây
        // Ghi log hoạt động
        User assignee = userRepository.findById(assigneeId).orElse(null);
        String assigneeName = assignee != null ? assignee.getFullName() : "Ai đó";
        activityLogService.logActivity(
            existingTask.getProjectId(),
            assigner.getId(),
            "Phân công",
            "Công việc '" + existingTask.getTitle() + "' đã được giao cho " + assigneeName,
            "mdi-account-arrow-right",
            "primary"
        );

        // TRIGGER: Thông báo cho Member (Assignee)
        notificationService.createNotification(
                assigneeId,
                "Công việc mới: [" + savedTask.getTitle() + "] trong dự án [" + project.getName() + "]",
                NotificationType.TASK_ASSIGNED,
                project.getId(),
                savedTask.getId()
        );

        return savedTask;
    }

    public Task updateTaskStatus(String taskId, TaskStatus newStatus, String cancelReason, String updaterUsername) {
        User updater = getUserByUsername(updaterUsername);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Project project = projectRepository.findById(existingTask.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
        boolean isOwner = project.getOwnerId().equals(updater.getId());
        boolean isProjectManager = project.getManagerIds().contains(updater.getId());
        boolean isSystemManagerInProject = updater.getRole() == Role.MANAGER && project.getMemberIds().contains(updater.getId());
        boolean isAssignee = existingTask.getAssigneeId() != null && existingTask.getAssigneeId().equals(updater.getId());

        if (!isOwner && !isProjectManager && !isSystemManagerInProject && !isAssignee) {
            throw new AccessDeniedException("User does not have permission to update task status");
        }

        // Ghi log hoạt động trước khi đổi trạng thái (để log chính xác trạng thái cũ - nếu muốn, ở đây chỉ log trạng thái mới)
        activityLogService.logActivity(
            existingTask.getProjectId(),
            updater.getId(),
            "Cập nhật trạng thái",
            "Công việc '" + existingTask.getTitle() + "' chuyển sang trạng thái " + newStatus,
            "mdi-check-circle",
            "success"
        );

        // Thông báo cho người tạo task và người thực hiện khi trạng thái thay đổi
        String statusMsg = "Cập nhật: [" + existingTask.getTitle() + "] đã chuyển sang [" + newStatus + "]";
        // Thông báo cho Member (Người thực hiện)
        if (existingTask.getAssigneeId() != null) {
            notificationService.createNotification(
                    existingTask.getAssigneeId(),
                    statusMsg,
                    NotificationType.TASK_STATUS_CHANGED, project.getId(), taskId
            );
        }

        // Thông báo cho Manager/Owner (Khi task Done hoặc Cancelled)
        if (newStatus == TaskStatus.DONE || newStatus == TaskStatus.CANCELLED) {
            String managerMsg = "Task [" + existingTask.getTitle() + "] " +
                    (newStatus == TaskStatus.DONE ? "đã HOÀN THÀNH" : "đã bị HỦY bởi " + updater.getFullName());

            notificationService.createNotification(
                    project.getOwnerId(),
                    managerMsg,
                    NotificationType.TASK_STATUS_CHANGED, project.getId(), taskId
            );
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
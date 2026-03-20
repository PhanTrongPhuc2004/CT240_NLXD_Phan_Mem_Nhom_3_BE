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
    private NotificationService notificationService;

    @Autowired
    private ActivityLogService activityLogService;

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

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

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
        User creator = getUserByUsername(creatorUsername);
        
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

        activityLogService.logActivity(
            task.getProjectId(),
            creator.getId(),
            "Tạo công việc",
            "Công việc '" + task.getTitle() + "' vừa được tạo",
            "mdi-plus-box",
            "info"
        );

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
            
        // 1. User là người tạo dự án
        boolean isOwner = project.getOwnerId().equals(updater.getId());
        
        // 2. User có role là MANAGER VÀ đồng thời có mặt trong danh sách thành viên của dự án
        boolean isSystemManagerInProject = updater.getRole() == Role.MANAGER && project.getMemberIds().contains(updater.getId());
        
        // 3. User là Quản lý của dự án đó (được chỉ định)
        boolean isProjectManager = project.getManagerIds().contains(updater.getId());
        
        // 4. User chính là người được giao công việc đó
        boolean isAssignee = existingTask.getAssigneeId() != null && existingTask.getAssigneeId().equals(updater.getId());

        // CHỈ cho phép Owner, Project Manager, System Manager có trong dự án, hoặc Assignee
        // ADMIN KHÔNG CÓ QUYỀN ĐỔI TRẠNG THÁI
        if (!isOwner && !isProjectManager && !isSystemManagerInProject && !isAssignee) {
            throw new AccessDeniedException("User does not have permission to update task status");
        }

        activityLogService.logActivity(
            existingTask.getProjectId(),
            updater.getId(),
            "Cập nhật trạng thái",
            "Công việc '" + existingTask.getTitle() + "' chuyển sang trạng thái " + newStatus,
            "mdi-check-circle",
            "success"
        );

        String statusMsg = "Cập nhật: [" + existingTask.getTitle() + "] đã chuyển sang [" + newStatus + "]";
        if (existingTask.getAssigneeId() != null && !existingTask.getAssigneeId().equals(updater.getId())) {
            notificationService.createNotification(
                    existingTask.getAssigneeId(),
                    statusMsg,
                    NotificationType.TASK_STATUS_CHANGED, project.getId(), taskId
            );
        }

        if (newStatus == TaskStatus.DONE || newStatus == TaskStatus.CANCELLED) {
            String managerMsg = "Task [" + existingTask.getTitle() + "] " +
                    (newStatus == TaskStatus.DONE ? "đã HOÀN THÀNH" : "đã bị HỦY bởi " + updater.getFullName());

            if (!project.getOwnerId().equals(updater.getId())) {
                notificationService.createNotification(
                        project.getOwnerId(),
                        managerMsg,
                        NotificationType.TASK_STATUS_CHANGED, project.getId(), taskId
                );
            }
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

package com.nhom3.ct240.listener;

import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.NotificationType;
import com.nhom3.ct240.entity.enums.TaskStatus;
import com.nhom3.ct240.event.*;
import com.nhom3.ct240.repository.ProjectRepository;
import com.nhom3.ct240.repository.UserRepository;
import com.nhom3.ct240.service.ActivityLogService;
import com.nhom3.ct240.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Async
    @EventListener
    public void handleTaskCreated(TaskCreatedEvent event) {
        Task task = event.getTask();
        activityLogService.logActivity(
            task.getProjectId(),
            event.getCreatorId(),
            "Tạo công việc",
            "Công việc '" + task.getTitle() + "' vừa được tạo",
            "mdi-plus-box",
            "info"
        );
        
        // Check if assigned initially
        if (task.getAssigneeId() != null) {
             Project project = projectRepository.findById(task.getProjectId()).orElse(null);
             if (project != null) {
                 notificationService.createNotification(
                    task.getAssigneeId(),
                    "Công việc mới: [" + task.getTitle() + "] trong dự án [" + project.getName() + "]",
                    NotificationType.TASK_ASSIGNED, project.getId(), task.getId()
                 );
             }
        }
    }

    @Async
    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        Task task = event.getTask();
        activityLogService.logActivity(
            task.getProjectId(),
            event.getEditorId(),
            "Cập nhật công việc",
            "Thông tin công việc '" + task.getTitle() + "' đã được cập nhật",
            "mdi-pencil",
            "warning"
        );
    }

    @Async
    @EventListener
    public void handleTaskDeleted(TaskDeletedEvent event) {
        Task task = event.getTask();
        activityLogService.logActivity(
            task.getProjectId(),
            event.getDeleterId(),
            "Xóa công việc",
            "Công việc '" + task.getTitle() + "' đã bị xóa",
            "mdi-delete",
            "error"
        );
    }

    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        Task task = event.getTask();
        User assignee = userRepository.findById(task.getAssigneeId()).orElse(null);
        String assigneeName = assignee != null ? assignee.getFullName() : "Ai đó";
        
        activityLogService.logActivity(
            task.getProjectId(),
            event.getAssignerId(),
            "Phân công",
            "Công việc '" + task.getTitle() + "' đã được giao cho " + assigneeName,
            "mdi-account-arrow-right",
            "primary"
        );

        Project project = projectRepository.findById(task.getProjectId()).orElse(null);
        if (project != null) {
            notificationService.createNotification(
                task.getAssigneeId(),
                "Công việc mới: [" + task.getTitle() + "] trong dự án [" + project.getName() + "]",
                NotificationType.TASK_ASSIGNED,
                project.getId(),
                task.getId()
            );
        }
    }

    @Async
    @EventListener
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        Task task = event.getTask();
        String updaterId = event.getUpdaterId();
        TaskStatus newStatus = task.getStatus();
        
        activityLogService.logActivity(
            task.getProjectId(),
            updaterId,
            "Cập nhật trạng thái",
            "Công việc '" + task.getTitle() + "' chuyển sang trạng thái " + newStatus,
            "mdi-check-circle",
            "success"
        );

        Project project = projectRepository.findById(task.getProjectId()).orElse(null);
        if (project == null) return;
        
        User updater = userRepository.findById(updaterId).orElse(null);
        String updaterName = updater != null ? updater.getFullName() : "Ai đó";

        String statusMsg = "Cập nhật: [" + task.getTitle() + "] đã chuyển sang [" + newStatus + "]";
        if (task.getAssigneeId() != null && !task.getAssigneeId().equals(updaterId)) {
            notificationService.createNotification(
                    task.getAssigneeId(),
                    statusMsg,
                    NotificationType.TASK_STATUS_CHANGED, project.getId(), task.getId()
            );
        }

        if (newStatus == TaskStatus.DONE || newStatus == TaskStatus.CANCELLED) {
            String managerMsg = "Task [" + task.getTitle() + "] " +
                    (newStatus == TaskStatus.DONE ? "đã HOÀN THÀNH" : "đã bị HỦY bởi " + updaterName);

            if (!project.getOwnerId().equals(updaterId)) {
                notificationService.createNotification(
                        project.getOwnerId(),
                        managerMsg,
                        NotificationType.TASK_STATUS_CHANGED, project.getId(), task.getId()
                );
            }
        }
    }
}

package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.task.CreateTaskDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.Role;
import com.nhom3.ct240.entity.enums.TaskStatus;
import com.nhom3.ct240.event.*;
import com.nhom3.ct240.factory.TaskFactory;
import com.nhom3.ct240.repository.ProjectRepository;
import com.nhom3.ct240.repository.TaskRepository;
import com.nhom3.ct240.repository.UserRepository;
import com.nhom3.ct240.strategy.TaskStatusStrategyFactory;
import com.nhom3.ct240.strategy.TaskStatusUpdateStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final TaskFactory taskFactory;
    private final TaskStatusStrategyFactory statusStrategyFactory;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ApplicationEventPublisher eventPublisher,
                       TaskFactory taskFactory,
                       TaskStatusStrategyFactory statusStrategyFactory) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.taskFactory = taskFactory;
        this.statusStrategyFactory = statusStrategyFactory;
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
        
        if (createTaskDTO.getAssigneeId() != null && !createTaskDTO.getAssigneeId().isEmpty()) {
            boolean isMember = project.getMemberIds().contains(createTaskDTO.getAssigneeId());
            if (!isMember) {
                throw new RuntimeException("Assignee is not a member of this project");
            }
        }

        // Use Factory
        Task task = taskFactory.createTask(createTaskDTO);
        Task savedTask = taskRepository.save(task);

        User creator = getUserByUsername(creatorUsername);
        
        // Use Event (Observer)
        eventPublisher.publishEvent(new TaskCreatedEvent(this, savedTask, creator.getId()));
        
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

        // Use Event (Observer)
        eventPublisher.publishEvent(new TaskUpdatedEvent(this, savedTask, editor.getId()));

        return savedTask;
    }

    public void deleteTask(String taskId, String deleterUsername) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        User deleter = getUserByUsername(deleterUsername);
        
        checkManagerPermission(existingTask.getProjectId(), deleterUsername);
        taskRepository.deleteById(taskId);

        // Use Event (Observer)
        eventPublisher.publishEvent(new TaskDeletedEvent(this, existingTask, deleter.getId()));
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

        // Use Event (Observer)
        eventPublisher.publishEvent(new TaskAssignedEvent(this, savedTask, assigner.getId()));

        return savedTask;
    }

    public Task updateTaskStatus(String taskId, TaskStatus newStatus, String cancelReason, String updaterUsername) {
        User updater = getUserByUsername(updaterUsername);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        TaskStatus oldStatus = existingTask.getStatus();

        Project project = projectRepository.findById(existingTask.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
        // Permission logic
        boolean isOwner = project.getOwnerId().equals(updater.getId());
        boolean isSystemManagerInProject = updater.getRole() == Role.MANAGER && project.getMemberIds().contains(updater.getId());
        boolean isProjectManager = project.getManagerIds().contains(updater.getId());
        boolean isAssignee = existingTask.getAssigneeId() != null && existingTask.getAssigneeId().equals(updater.getId());

        if (!isOwner && !isProjectManager && !isSystemManagerInProject && !isAssignee) {
            throw new AccessDeniedException("User does not have permission to update task status");
        }

        // Use Strategy
        TaskStatusUpdateStrategy strategy = statusStrategyFactory.getStrategy(newStatus);
        strategy.update(existingTask, updater, cancelReason);
        
        Task savedTask = taskRepository.save(existingTask);

        // Use Event (Observer)
        eventPublisher.publishEvent(new TaskStatusChangedEvent(this, savedTask, oldStatus, updater.getId(), cancelReason));
        
        return savedTask;
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

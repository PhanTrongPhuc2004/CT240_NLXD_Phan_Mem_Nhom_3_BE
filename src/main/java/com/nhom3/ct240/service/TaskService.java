package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.CreateTaskDTO;
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

    private void checkManagerPermission(String projectId, String username) {
        User user = getUserByUsername(username);

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(user.getId()) && !project.getManagerIds().contains(user.getId())) {
            throw new AccessDeniedException("User is not a manager of this project");
        }
    }

    public Task createTask(CreateTaskDTO createTaskDTO, String creatorUsername) {
        User creator = getUserByUsername(creatorUsername);
        checkManagerPermission(createTaskDTO.getProjectId(), creator.getUsername());

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
        User editor = getUserByUsername(editorUsername);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        checkManagerPermission(existingTask.getProjectId(), editor.getUsername());

        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setDeadline(taskDetails.getDeadline());
        existingTask.setPriority(taskDetails.getPriority());
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    public void deleteTask(String taskId, String deleterUsername) {
        User deleter = getUserByUsername(deleterUsername);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        checkManagerPermission(existingTask.getProjectId(), deleter.getUsername());
        taskRepository.deleteById(taskId);
    }

    public Task assignTask(String taskId, String assigneeId, String assignerUsername) {
        User assigner = getUserByUsername(assignerUsername);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        checkManagerPermission(existingTask.getProjectId(), assigner.getUsername());

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

        if (updater.getRole() != Role.ADMIN) {
            Project project = projectRepository.findById(existingTask.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            boolean isManager = project.getOwnerId().equals(updater.getId()) || project.getManagerIds().contains(updater.getId());
            boolean isAssignee = existingTask.getAssigneeId() != null && existingTask.getAssigneeId().equals(updater.getId());

            if (!isManager && !isAssignee) {
                throw new AccessDeniedException("User does not have permission to update task status");
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

    private void checkMemberPermission(String projectId, String username) {
        User user = getUserByUsername(username);

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(user.getId()) && !project.getManagerIds().contains(user.getId()) && !project.getMemberIds().contains(user.getId())) {
            throw new AccessDeniedException("User is not a member of this project");
        }
    }

    public Optional<Task> findTaskById(String taskId, String viewerUsername) {
        User viewer = getUserByUsername(viewerUsername);
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        taskOpt.ifPresent(task -> checkMemberPermission(task.getProjectId(), viewer.getUsername()));
        return taskOpt;
    }

    public List<Task> findTasksByProjectId(String projectId, String viewerUsername) {
        User viewer = getUserByUsername(viewerUsername);
        checkMemberPermission(projectId, viewer.getUsername());
        return taskRepository.findByProjectId(projectId);
    }
    
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByProjectAndStatus(String projectId, TaskStatus filterStatus, String viewerUsername) {
        User viewer = getUserByUsername(viewerUsername);
        checkMemberPermission(projectId, viewer.getUsername());
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (filterStatus != null) {
            return tasks.stream()
                    .filter(t -> t.getStatus() == filterStatus)
                    .toList();
        }
        return tasks;
    }
}

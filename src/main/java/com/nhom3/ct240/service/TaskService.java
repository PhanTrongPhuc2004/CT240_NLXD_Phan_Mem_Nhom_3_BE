package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.TaskDTO;
import com.nhom3.ct240.entity.Task;

import java.util.List;

/**
 * Service cho quản lý công việc
 * - CN_17 đến CN_23
 */
public interface TaskService {

    Task createTask(TaskDTO dto, String projectId, String currentUserId);

    Task updateTask(String taskId, TaskDTO dto, String currentUserId);

    void deleteTask(String taskId, String currentUserId);

    Task updateTaskStatus(String taskId, String newStatus, String reason, String currentUserId);

    List<Task> getTasksByProject(String projectId, String filterStatus, String currentUserId);

    // Phân công, xem chi tiết,...
}
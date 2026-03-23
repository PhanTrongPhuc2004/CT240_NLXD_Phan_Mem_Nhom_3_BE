package com.nhom3.ct240.factory;

import com.nhom3.ct240.dto.task.CreateTaskDTO;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskFactory {

    public Task createTask(CreateTaskDTO dto) {
        Task task = new Task();
        task.setProjectId(dto.getProjectId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setAssigneeId(dto.getAssigneeId());
        task.setDeadline(dto.getDeadline());
        task.setPriority(dto.getPriority());
        task.setStatus(TaskStatus.TO_DO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }
}

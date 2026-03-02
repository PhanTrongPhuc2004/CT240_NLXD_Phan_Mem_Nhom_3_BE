package com.nhom3.ct240.entity;

import com.nhom3.ct240.entity.enums.TaskStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "task_history")
public class TaskHistory {
    @Id
    private String id;
    private String taskId;
    private String changedByUserId;
    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private String reason;
    private LocalDateTime changedAt = LocalDateTime.now();
}
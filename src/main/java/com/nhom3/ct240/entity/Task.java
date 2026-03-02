package com.nhom3.ct240.entity;

import com.nhom3.ct240.entity.enums.Priority;
import com.nhom3.ct240.entity.enums.TaskStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private String projectId;
    private String title;
    private String description;
    private String assigneeId;
    private LocalDateTime deadline;
    private Priority priority = Priority.MEDIUM;
    private TaskStatus status = TaskStatus.TODO;
    private String cancelReason;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
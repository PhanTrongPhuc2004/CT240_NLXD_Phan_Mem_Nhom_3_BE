package com.nhom3.ct240.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "activity_logs")
public class ActivityLog {
    @Id
    private String id;
    private String projectId;
    private String userId;
    private String action;
    private String details;
    private String icon;
    private String color;
    private LocalDateTime createdAt = LocalDateTime.now();
}

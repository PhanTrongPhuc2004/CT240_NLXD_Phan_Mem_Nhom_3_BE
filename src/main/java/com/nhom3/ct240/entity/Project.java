package com.nhom3.ct240.entity;

import com.nhom3.ct240.entity.enums.ProjectStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "projects")
public class Project {
    @Id
    private String id;
    private String name;
    private String description;
    private String ownerId;
    private List<String> managerIds = new ArrayList<>();
    private List<String> memberIds = new ArrayList<>();
    private List<String> pendingMemberIds = new ArrayList<>(); // Danh sách user chờ duyệt
    private ProjectStatus status = ProjectStatus.ACTIVE;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
package com.nhom3.ct240.dto;

import com.nhom3.ct240.entity.enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho dự án (dùng cho CN_11–CN_16, CN_29, CN_30, CN_31)
 */
@Data
public class ProjectDTO {
    private String id;
    private String name;
    private String description;
    private String ownerId;
    private List<String> managerIds;
    private List<String> memberIds;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // TODO: thêm fields như progress %, overdue tasks nếu cần cho CN_29
}
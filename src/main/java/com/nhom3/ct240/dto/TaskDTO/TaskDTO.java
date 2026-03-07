package com.nhom3.ct240.dto.TaskDTO;

import com.nhom3.ct240.entity.enums.TaskPriority; // Đã sửa thành TaskPriority
import com.nhom3.ct240.entity.enums.TaskStatus;
import lombok.Data; // Thêm Lombok Data để tự động tạo getters/setters/constructors
import java.time.LocalDateTime;

/**
 * DTO cho công việc/task (dùng cho CN_17–CN_23, CN_29, CN_30)
 */
@Data // Thêm annotation này
public class TaskDTO {
    private String id;
    private String projectId;
    private String title;
    private String description;
    private String assigneeId;
    private LocalDateTime deadline;
    private TaskPriority priority; // Đã sửa thành TaskPriority
    private TaskStatus status;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // TODO: thêm sub-tasks, attachments nếu cần sau
}
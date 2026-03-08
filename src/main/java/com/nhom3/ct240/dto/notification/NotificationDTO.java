package com.nhom3.ct240.dto.notification;

import com.nhom3.ct240.entity.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Data;
/**
 * DTO cho thông báo (dùng cho CN_27, CN_28)
 */
@Data
public class NotificationDTO {
    private String id;
    private String userId;
    private NotificationType type;
    private String message;
    private String relatedTaskId;
    private boolean read;
    private LocalDateTime createdAt;
}

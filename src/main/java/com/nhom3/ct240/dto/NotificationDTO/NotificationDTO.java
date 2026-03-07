package com.nhom3.ct240.dto.NotificationDTO;

import com.nhom3.ct240.entity.enums.NotificationType;
import java.time.LocalDateTime;

/**
 * DTO cho thông báo (dùng cho CN_27, CN_28)
 */
public class NotificationDTO {
    private String id;
    private String userId;
    private NotificationType type;
    private String message;
    private String relatedTaskId;
    private boolean read;
    private LocalDateTime createdAt;
}
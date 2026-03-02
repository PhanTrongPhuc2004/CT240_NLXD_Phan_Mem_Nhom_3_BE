package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.NotificationDTO;
import com.nhom3.ct240.entity.Notification;

import java.util.List;

/**
 * Service cho thông báo
 * - CN_27: Xem danh sách
 * - CN_28: Đánh dấu đã đọc
 * - Tự động tạo thông báo khi có thay đổi task/comment/project
 */
public interface NotificationService {

    List<NotificationDTO> getNotifications(String userId, boolean unreadOnly);

    void markAsRead(String notificationId, String userId);

    void markAllAsRead(String userId);

    // TODO: method tạo thông báo tự động (gọi từ các service khác)
}
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

    /**
     * Tạo một thông báo mới cho một người dùng cụ thể.
     * @param userId Người nhận thông báo.
     * @param message Nội dung thông báo.
     * @param link (Tùy chọn) Đường dẫn liên quan đến thông báo.
     */
    void createNotification(String userId, String message, String link);
}
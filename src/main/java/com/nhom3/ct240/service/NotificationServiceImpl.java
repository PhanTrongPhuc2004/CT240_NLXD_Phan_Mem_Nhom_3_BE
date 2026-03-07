package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.NotificationDTO.NotificationDTO;
import com.nhom3.ct240.entity.Notification;
import com.nhom3.ct240.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void createNotification(String userId, String message, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        // TODO: Gửi thông báo real-time qua WebSocket nếu có
    }

    @Override
    public List<NotificationDTO> getNotifications(String userId, boolean unreadOnly) {
        // Logic này sẽ được triển khai sau
        return List.of();
    }

    @Override
    public void markAsRead(String notificationId, String userId) {
        // Logic này sẽ được triển khai sau
    }

    @Override
    public void markAllAsRead(String userId) {
        // Logic này sẽ được triển khai sau
    }
}
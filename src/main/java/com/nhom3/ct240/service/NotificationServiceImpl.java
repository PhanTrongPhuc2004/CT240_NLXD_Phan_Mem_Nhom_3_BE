package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.notification.NotificationDTO;
import com.nhom3.ct240.entity.Notification;
import com.nhom3.ct240.entity.enums.NotificationType;
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
    public void createNotification(String userId, String message, NotificationType type, String projectId, String taskId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setProjectId(projectId);
        notification.setTaskId(taskId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // TODO: Gửi thông báo real-time qua WebSocket nếu có
    }

    @Override
    public List<NotificationDTO> getNotifications(String userId, boolean unreadOnly) {
        List<Notification> notifications;

        if (unreadOnly) {
            notifications = notificationRepository.findByUserIdAndReadFalse(userId);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return notifications.stream().map(n -> {

            NotificationDTO dto = new NotificationDTO();

            dto.setId(n.getId());
            dto.setUserId(n.getUserId());
            if (n.getType() != null) {
                dto.setType(n.getType());
            }
            if (n.getRelatedTaskId() != null) {
                dto.setRelatedTaskId(n.getRelatedTaskId());
            }
            dto.setMessage(n.getMessage());
            dto.setRead(n.isRead());
            dto.setCreatedAt(n.getCreatedAt());

            return dto;
        }).toList();
    }

    @Override
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow();

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndReadFalse(userId);

        for (Notification n : notifications) {
            n.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }
}

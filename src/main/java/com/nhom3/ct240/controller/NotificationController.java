package com.nhom3.ct240.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom3.ct240.dto.notification.NotificationDTO;
import com.nhom3.ct240.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controller thông báo
 * - CN_27: Xem danh sách thông báo
 * - CN_28: Đánh dấu đã đọc
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    // CN_27: Lấy danh sách thông báo
    @GetMapping
    public List<NotificationDTO> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        return notificationService.getNotifications(userId, unreadOnly);
    }
    // CN_28: Đánh dấu 1 thông báo đã đọc
    @PatchMapping("/{id}/read")
    public void markAsRead(
            @PathVariable String id,
            @RequestParam String userId
    ) {
        notificationService.markAsRead(id, userId);
    }

    // CN_28: Đánh dấu tất cả đã đọc
    @PatchMapping("/read-all")
    public void markAllAsRead(
            @RequestParam String userId
    ) {
        notificationService.markAllAsRead(userId);
    }
}

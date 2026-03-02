package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity Notification
 * - CN_27: Xem danh sách thông báo → findByUserId()
 * - CN_28: Đánh dấu đã đọc → save()
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Danh sách thông báo của user (CN_27)
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Thông báo chưa đọc (CN_27)
    List<Notification> findByUserIdAndReadFalse(String userId);
}
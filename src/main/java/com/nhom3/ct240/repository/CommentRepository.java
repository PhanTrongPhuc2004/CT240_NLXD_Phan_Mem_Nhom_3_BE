package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity Comment
 * - CN_24: Thêm bình luận → save()
 * - CN_25: Xem danh sách bình luận → findByTaskId()
 * - CN_26: Chỉnh sửa/xóa bình luận → save()/deleteById()
 */
@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    // Danh sách bình luận theo task (CN_25)
    List<Comment> findByTaskIdOrderByCreatedAtAsc(String taskId);

    // Bình luận của user trong task (nếu cần lọc)
    List<Comment> findByTaskIdAndUserId(String taskId, String userId);

    // Lấy danh sách bình luận chưa bị xóa, sắp xếp mới nhất trước
    List<Comment> findByTaskIdAndDeletedFalseOrderByCreatedAtDesc(String taskId);
}
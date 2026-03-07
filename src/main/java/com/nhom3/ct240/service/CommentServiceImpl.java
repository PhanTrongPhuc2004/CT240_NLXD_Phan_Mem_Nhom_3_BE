package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.CommentDTO.CommentDTO;
import com.nhom3.ct240.entity.Comment;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.repository.CommentRepository;
import com.nhom3.ct240.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository; // Dùng để lấy tên người dùng gắn vào DTO

    // --- CN_24: THÊM BÌNH LUẬN ---
    @Override
    public Comment addComment(String taskId, String content, String currentUserId) {
        // Ràng buộc Không cho phép gửi bình luận rỗng
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống!");
        }

        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setUserId(currentUserId);
        comment.setContent(content);
        // createdAt và updatedAt đã có giá trị mặc định là LocalDateTime.now() trong Entity

        return commentRepository.save(comment);
    }

    // --- CN_25: XEM DANH SÁCH BÌNH LUẬN ---
    @Override
    public List<CommentDTO> getCommentsByTask(String taskId) {
        // 1. Lấy danh sách từ DB (đã lọc bỏ bình luận xóa mềm và sắp xếp mới nhất lên đầu)
        List<Comment> comments = commentRepository.findByTaskIdAndIsDeletedFalseOrderByCreatedAtDesc(taskId);

        // 2. Chuyển đổi từ Entity sang DTO để gửi về Frontend
        List<CommentDTO> commentDTOs = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDTO dto = new CommentDTO();
            // Copy các thuộc tính giống nhau từ comment sang dto (id, content, createdAt...)
            BeanUtils.copyProperties(comment, dto);

            // 3. Tìm tên User để đắp vào DTO (Frontend cần tên để hiển thị)
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            if (user != null) {
                dto.setUserName(user.getFullName()); // Giả sử User entity của bạn có hàm getFullName()
            } else {
                dto.setUserName("Người dùng không xác định");
            }

            commentDTOs.add(dto);
        }
        return commentDTOs;
    }

    // --- CN_26: SỬA BÌNH LUẬN ---
    @Override
    public Comment updateComment(String commentId, String newContent, String currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận!"));

        // Ràng buộc Chỉ chủ nhân mới được sửa
        if (!comment.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền sửa bình luận của người khác!");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now()); // Cập nhật giờ sửa
        comment.setEdited(true);                   // Đánh dấu (Đã chỉnh sửa)

        return commentRepository.save(comment);
    }

    // --- CN_26: XÓA BÌNH LUẬN ---
    @Override
    public void deleteComment(String commentId, String currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận!"));

        // Ràng buộc Chỉ chủ nhân mới được xóa
        if (!comment.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận của người khác!");
        }

        // Xóa mềm: Bật cờ isDeleted lên true thay vì xóa mất khỏi DB
        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
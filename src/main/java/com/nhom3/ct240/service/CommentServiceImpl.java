package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.comment.CommentDTO;
import com.nhom3.ct240.entity.Comment;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.NotificationType;
import com.nhom3.ct240.repository.CommentRepository;
import com.nhom3.ct240.repository.ProjectRepository;
import com.nhom3.ct240.repository.TaskRepository;
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
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository; // Dùng để lấy tên người dùng gắn vào DTO
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskRepository taskRepository;
    // CN_24: THÊM BÌNH LUẬN
    @Override
    public Comment addComment(String taskId, String content, List<String> attachmentUrls, String currentUserId,String parentId){
        // Ràng buộc Không cho phép gửi bình luận rỗng
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống!");
        }

        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setUserId(currentUserId);
        comment.setContent(content);

        comment.setAttachmentUrls(attachmentUrls);

        comment.setParentId(parentId);

        Comment savedComment = commentRepository.save(comment);

        Task task = taskRepository.findById(taskId).orElseThrow();
        User creator = userRepository.findById(currentUserId).orElseThrow();

        // 1. Thông báo cho người thực hiện (Assignee) nếu họ không phải là người comment
        if (task.getAssigneeId() != null && !task.getAssigneeId().equals(currentUserId)) {
            notificationService.createNotification(
                    task.getAssigneeId(),
                    creator.getFullName() + " đã bình luận trong [" + task.getTitle() + "]",
                    NotificationType.COMMENT_ADDED, task.getProjectId(), taskId
            );
        }

        // 2. Logic xử lý Tag @username (Thông báo Tag @bạn)
        if (content.contains("@")) {
            // Giả sử bạn parse được list username từ content
            // findUserByUsername -> notificationService.createNotification(..., MENTIONED_IN_COMMENT, ...)
        }

        // 3. Thông báo cho Manager (Bình luận cần chú ý)
        if (content.toLowerCase().contains("lỗi") || content.toLowerCase().contains("block")) {
            Project project = projectRepository.findById(task.getProjectId()).orElseThrow();
            notificationService.createNotification(
                    project.getOwnerId(),
                    "Chú ý: Task [" + task.getTitle() + "] có bình luận về lỗi/block",
                    NotificationType.COMMENT_ADDED, project.getId(), taskId
            );
        }

        return savedComment;
    }

    // CN_25: XEM DANH SÁCH BÌNH LUẬN
    @Override
    public List<CommentDTO> getCommentsByTask(String taskId) {
        // 1. Lấy danh sách từ DB (đã lọc bỏ bình luận xóa mềm và sắp xếp mới nhất lên đầu)
        List<Comment> comments = commentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtDesc(taskId);

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

    //CN_26: SỬA BÌNH LUẬN
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

    //CN_26: XÓA BÌNH LUẬN
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

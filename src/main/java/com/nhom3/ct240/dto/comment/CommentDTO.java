package com.nhom3.ct240.dto.CommentDTO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO cho bình luận (dùng cho CN_24–CN_26)
 */
@Data
public class CommentDTO {
    private String id;
    private String taskId;
    private String userId;
    private String userName; // Tên người bình luận
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited;
}
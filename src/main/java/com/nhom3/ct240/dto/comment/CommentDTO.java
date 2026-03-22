package com.nhom3.ct240.dto.comment;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho bình luận (dùng cho CN_24–CN_26)
 */
@Data
public class CommentDTO {
    private String id;
    private String taskId;
    private String userId;
    private String userName;
    private String content;
    private String parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited;
    private List<String> attachmentUrls;
}

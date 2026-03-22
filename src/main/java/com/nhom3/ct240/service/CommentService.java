package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.comment.CommentDTO;
import com.nhom3.ct240.entity.Comment;

import java.util.List;

/**
 * Service cho bình luận
 * - CN_24 đến CN_26
 */
public interface CommentService {

    Comment addComment(String taskId, String content, List<String> attachmentUrls, String currentUserId, String parentId);

    List<CommentDTO> getCommentsByTask(String taskId);

    Comment updateComment(String commentId, String newContent, String currentUserId);

    void deleteComment(String commentId, String currentUserId);
}

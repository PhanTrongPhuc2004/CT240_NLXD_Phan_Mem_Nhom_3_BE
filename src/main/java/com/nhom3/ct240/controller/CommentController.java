package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.CommentDTO.CommentDTO;
import com.nhom3.ct240.entity.Comment;
import com.nhom3.ct240.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller bình luận
 * - CN_24: Thêm bình luận
 * - CN_25: Xem danh sách bình luận
 * - CN_26: Chỉnh sửa/xóa bình luận
 */
@RestController
@RequestMapping("/api/comments")
@CrossOrigin("*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // --- CN_24: Thêm bình luận ---
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("userId") String currentUserId) {

        String taskId = requestBody.get("taskId");
        String content = requestBody.get("content");

        Comment newComment = commentService.addComment(taskId, content, currentUserId);
        return ResponseEntity.ok(newComment);
    }

    // --- CN_25: Xem danh sách bình luận ---
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByTask(@PathVariable String taskId) {
        List<CommentDTO> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    // --- CN_26: Chỉnh sửa bình luận ---
    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("userId") String currentUserId) {

        String newContent = requestBody.get("content");
        Comment updatedComment = commentService.updateComment(commentId, newContent, currentUserId);
        return ResponseEntity.ok(updatedComment);
    }

    // --- CN_26: Xóa bình luận ---
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable String commentId,
            @RequestHeader("userId") String currentUserId) {

        commentService.deleteComment(commentId, currentUserId);
        return ResponseEntity.ok("Xóa bình luận thành công!");
    }
}
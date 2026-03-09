package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.comment.CommentDTO;
import com.nhom3.ct240.entity.Comment;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.CommentService;
import com.nhom3.ct240.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private UserService userService;

    // Hàm phụ để lấy UserID
    private String getUserId(UserDetails currentUser) {
        return userService.findByUsername(currentUser.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- CN_24: Thêm bình luận ---
    @PostMapping
    public ResponseEntity<?> addComment(
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");

        String taskId = requestBody.get("taskId");
        String content = requestBody.get("content");

        try {
            Comment newComment = commentService.addComment(taskId, content, getUserId(currentUser));
            return ResponseEntity.ok(newComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- CN_25: Xem danh sách bình luận ---
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByTask(@PathVariable String taskId) {
        List<CommentDTO> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    // --- CN_26: Chỉnh sửa bình luận ---
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");

        String newContent = requestBody.get("content");
        try {
            Comment updatedComment = commentService.updateComment(commentId, newContent, getUserId(currentUser));
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- CN_26: Xóa bình luận ---
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");

        try {
            commentService.deleteComment(commentId, getUserId(currentUser));
            return ResponseEntity.ok("Xóa bình luận thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

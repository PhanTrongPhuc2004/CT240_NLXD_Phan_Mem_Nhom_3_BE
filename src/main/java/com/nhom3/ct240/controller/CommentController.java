package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.comment.CommentDTO;
import com.nhom3.ct240.entity.Comment;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.CommentService;
import com.nhom3.ct240.service.FileStorageService;
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

    @Autowired
    private FileStorageService fileStorageService;

    // Hàm phụ để lấy UserID
    private String getUserId(UserDetails currentUser) {
        return userService.findByUsername(currentUser.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- CN_24: Thêm bình luận (hỗ trợ gửi file)---
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addComment(
            @RequestParam("taskId") String taskId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) List<org.springframework.web.multipart.MultipartFile> files,
            @RequestParam(value = "parentId", required = false) String parentId,

            @AuthenticationPrincipal UserDetails currentUser) {

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");

        try {
            // 1. Khởi tạo danh sách URL rỗng
            List<String> attachmentUrls = new java.util.ArrayList<>();

            // 2. Nếu user có đính kèm file thì đem đi cất vào ổ cứng và lấy link
            if (files != null && !files.isEmpty()) {
                attachmentUrls = fileStorageService.storeFiles(files);
            }

            // 3. Xử lý trường hợp người dùng gửi file mà không gõ chữ
            String finalContent = (content == null) ? "" : content;

            // 4. Lưu vào Database (Nhớ truyền thêm danh sách URL vào hàm)
            Comment newComment = commentService.addComment(taskId, finalContent, attachmentUrls, getUserId(currentUser), parentId);

            return ResponseEntity.ok(newComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @PathVariable String fileName,
            jakarta.servlet.http.HttpServletRequest request) {

        try {
            // 1. Gọi service để lấy file vật lý từ thư mục uploads
            org.springframework.core.io.Resource resource = fileStorageService.loadFileAsResource(fileName);

            // 2. Tự động nhận diện định dạng (ảnh, pdf, docx...)
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) contentType = "application/octet-stream";

            // 3. Trả về dữ liệu file cho người dùng
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
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

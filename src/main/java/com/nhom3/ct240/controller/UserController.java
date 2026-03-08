package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.*;
import com.nhom3.ct240.dto.auth.RegisterRequest;
import com.nhom3.ct240.dto.user.RoleUpdateDTO;
import com.nhom3.ct240.dto.user.UserResponseDTO;
import com.nhom3.ct240.dto.user.UserUpdateDTO;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- CÁC ENDPOINT TĨNH (PHẢI ĐẶsT TRƯỚC /{id}) ---

    // CN_05: Xem hồ sơ cá nhân (GET /users/me)
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponseDTO dto = new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getAvatarUrl(), user.getRole(), user.isActive()
        );
        return ResponseEntity.ok(dto);
    }

    // CN_04: Cập nhật hồ sơ cá nhân (PUT /users/me)
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestBody UserUpdateDTO updateDTO,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User updatedUser = userService.updateProfile(currentUser.getUsername(), updateDTO);
        UserResponseDTO dto = new UserResponseDTO(
                updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getFullName(), updatedUser.getAvatarUrl(), updatedUser.getRole(), updatedUser.isActive()
        );
        return ResponseEntity.ok(dto);
    }

    // API TÌM KIẾM USER (Cho mọi user đã đăng nhập) - GET /users/search
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Keyword cannot be empty");
        }

        List<User> users = userService.searchUsers(keyword);
        // Convert to DTO to hide sensitive info
        List<UserResponseDTO> dtos = users.stream().map(user -> new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getAvatarUrl(), user.getRole(), user.isActive()
        )).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // API LẤY DANH SÁCH USER THEO ID (Cho mọi user đã đăng nhập) - POST /users/list
    @PostMapping("/list")
    public ResponseEntity<?> getUsersByIds(@RequestBody UserIdsRequestDTO request, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("User IDs list cannot be empty");
        }

        List<User> users = userService.getUsersByIds(request.getUserIds());
        List<UserResponseDTO> dtos = users.stream().map(user -> new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getAvatarUrl(), user.getRole(), user.isActive()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // CN_06: Xem danh sách tất cả người dùng (Admin/Manager) - GET /users/all
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> dtos = userService.findAllUsers().stream()
                .map(user -> new UserResponseDTO(
                        user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                        user.getAvatarUrl(), user.getRole(), user.isActive()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Thêm endpoint GET /users (tương đương /all) để tương thích với Frontend
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return getAllUsers();
    }

    // --- CÁC ENDPOINT ĐỘNG (ĐẶT SAU CÙNG) ---

    // GET /users/{id} - Lấy thông tin 1 user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        UserResponseDTO dto = new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getAvatarUrl(), user.getRole(), user.isActive()
        );
        return ResponseEntity.ok(dto);
    }

    // CN_07: Thêm người dùng mới (Admin) - POST /users
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody RegisterRequest request) {
        User createdUser = userService.register(
                request.getUsername(), request.getEmail(), request.getPassword(), request.getFullName()
        );
        UserResponseDTO dto = new UserResponseDTO(
                createdUser.getId(), createdUser.getUsername(), createdUser.getEmail(),
                createdUser.getFullName(), createdUser.getAvatarUrl(), createdUser.getRole(), createdUser.isActive()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // CN_08: Chỉnh sửa thông tin người dùng (Admin) - PUT /users/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        User updatedUser = userService.updateUser(id, updateDTO);
        UserResponseDTO dto = new UserResponseDTO(
                updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getFullName(), updatedUser.getAvatarUrl(), updatedUser.getRole(), updatedUser.isActive()
        );
        return ResponseEntity.ok(dto);
    }

    // CN_09: Xóa/Khóa người dùng (Admin) - DELETE /users/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // CN_10: Phân vai trò cho người dùng (Admin) - PATCH /users/{id}/role
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateRole(
            @PathVariable String id,
            @RequestBody RoleUpdateDTO roleUpdate) {
        User updatedUser = userService.updateRole(id, roleUpdate.getRole());
        UserResponseDTO dto = new UserResponseDTO(
                updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(),
                updatedUser.getFullName(), updatedUser.getAvatarUrl(), updatedUser.getRole(), updatedUser.isActive()
        );
        return ResponseEntity.ok(dto);
    }
}
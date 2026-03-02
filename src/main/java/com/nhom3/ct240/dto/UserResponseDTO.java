package com.nhom3.ct240.dto;

import com.nhom3.ct240.entity.enums.Role;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin user (không chứa password)
 * Dùng cho: CN_05 (Xem hồ sơ cá nhân), CN_06 (Xem danh sách người dùng), CN_08 (Chỉnh sửa user), CN_10 (Phân vai trò)
 */
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor từ entity User
    public UserResponseDTO(/* User user */) {
        // TODO: map từ User entity
    }

    // Getters & Setters
}
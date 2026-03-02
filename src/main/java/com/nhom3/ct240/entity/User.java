package com.nhom3.ct240.entity;

import com.nhom3.ct240.entity.enums.Role;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho người dùng trong hệ thống quản lý công việc nhóm
 */
@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password; // hashed password (sử dụng BCrypt)

    private String fullName;

    private String avatarUrl;

    private Role role = Role.MEMBER; // Enum: ADMIN, MANAGER, MEMBER

    private boolean active = true; // Trạng thái tài khoản (active/inactive)

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // Danh sách dự án người dùng sở hữu (làm Project Owner)
    private List<String> ownedProjectIds = new ArrayList<>();

    // Danh sách dự án người dùng tham gia (Member hoặc Manager phụ)
    private List<String> participatingProjectIds = new ArrayList<>();

    // Nếu sau này cần thêm:
    // private String phoneNumber;
    // private String bio;
    // private List<String> notificationPreferences;



    public boolean isActive() {return active;}


    public boolean isEnabled() {
        return active;
    }

    // Cập nhật updatedAt tự động khi thay đổi entity (nếu dùng @PreUpdate)
    // Hoặc xử lý ở service layer
}
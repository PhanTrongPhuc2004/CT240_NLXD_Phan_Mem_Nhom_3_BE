package com.nhom3.ct240.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý quản lý người dùng
 * - CN_04: Cập nhật hồ sơ cá nhân
 * - CN_05: Xem hồ sơ cá nhân
 * - CN_06: Xem danh sách người dùng (Admin)
 * - CN_07: Thêm người dùng mới (Admin)
 * - CN_08: Chỉnh sửa thông tin người dùng (Admin)
 * - CN_09: Xóa/Khóa người dùng (Admin)
 * - CN_10: Phân vai trò cho người dùng (Admin)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    // TODO: Inject UserService

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // TODO: CN_05
        return null;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile() {
        // TODO: CN_04
        return null;
    }

    // Các endpoint Admin...
}
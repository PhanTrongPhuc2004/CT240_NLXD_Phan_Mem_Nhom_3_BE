package com.nhom3.ct240.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller quản lý dự án
 * - CN_11: Tạo dự án mới
 * - CN_12: Chỉnh sửa thông tin dự án
 * - CN_13: Xóa dự án
 * - CN_14: Xem chi tiết dự án
 * - CN_15: Phân quyền quản lý dự án
 * - CN_16: Tham gia/rời dự án
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    // TODO: Inject ProjectService

    @PostMapping
    public ResponseEntity<?> createProject() {
        // TODO: CN_11
        return null;
    }

    // Các endpoint khác...
}
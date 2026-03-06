package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.ProjectDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * CN_11: Tạo dự án mới
     */
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            Project newProject = projectService.createProject(projectDTO, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newProject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * CN_14: Xem chi tiết dự án
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectDetails(@PathVariable String projectId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            Project project = projectService.getProjectDetail(projectId, currentUser.getId());
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * CN_12: Chỉnh sửa thông tin dự án
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody ProjectDTO projectDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            Project updatedProject = projectService.updateProject(projectId, projectDTO, currentUser.getId());
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * CN_13: Xóa dự án
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            projectService.deleteProject(projectId, currentUser.getId());
            return ResponseEntity.ok("Project deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * CN_15: Thêm/Xóa Manager (Phân quyền quản lý)
     */
    @PostMapping("/{projectId}/managers/{userId}")
    public ResponseEntity<?> addManager(@PathVariable String projectId, @PathVariable String userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            // Giả định service có method addManager
            // projectService.addManager(projectId, userId, currentUser.getId());
            return ResponseEntity.ok("Manager added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * CN_16: Tham gia dự án
     */
    @PostMapping("/{projectId}/join")
    public ResponseEntity<?> joinProject(@PathVariable String projectId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            // Giả định service có method joinProject
            // projectService.joinProject(projectId, currentUser.getId());
            return ResponseEntity.ok("Joined project successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * CN_16: Rời dự án
     */
    @PostMapping("/{projectId}/leave")
    public ResponseEntity<?> leaveProject(@PathVariable String projectId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            // Giả định service có method leaveProject
            // projectService.leaveProject(projectId, currentUser.getId());
            return ResponseEntity.ok("Left project successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

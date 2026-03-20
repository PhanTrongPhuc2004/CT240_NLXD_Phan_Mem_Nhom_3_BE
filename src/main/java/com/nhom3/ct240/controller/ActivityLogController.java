package com.nhom3.ct240.controller;

import com.nhom3.ct240.entity.ActivityLog;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.ActivityLogService;
import com.nhom3.ct240.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin("*")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserService userService;

    @Autowired
    public ActivityLogController(ActivityLogService activityLogService, UserService userService) {
        this.activityLogService = activityLogService;
        this.userService = userService;
    }

    private String getUserId(UserDetails currentUser) {
        return userService.findByUsername(currentUser.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET /api/projects/{projectId}/activities
    @GetMapping("/{projectId}/activities")
    public ResponseEntity<?> getProjectActivities(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");

        try {
            // Tạm thời cho phép mọi người đang đăng nhập xem (bạn có thể thêm logic kiểm tra quyền nếu cần)
            List<ActivityLog> activities = activityLogService.getRecentActivitiesByProject(projectId, limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

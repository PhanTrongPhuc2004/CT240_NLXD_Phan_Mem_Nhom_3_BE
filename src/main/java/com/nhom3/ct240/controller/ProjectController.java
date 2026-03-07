package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.ProjectDTO;
import com.nhom3.ct240.dto.UserIdRequestDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.ProjectService;
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
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @Autowired
    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    // Hàm phụ để lấy UserID nhanh
    private String getUserId(UserDetails currentUser) {
        return userService.findByUsername(currentUser.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // MỚI: GET /api/projects - Lấy danh sách dự án mà user đang tham gia
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            String currentUserId = getUserId(currentUser);
            List<Project> projects = projectService.getProjectsByUserId(currentUserId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project newProject = projectService.createProject(projectDTO, getUserId(currentUser));
            return ResponseEntity.status(HttpStatus.CREATED).body(newProject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectDetails(@PathVariable String projectId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project project = projectService.getProjectDetail(projectId, getUserId(currentUser));
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody ProjectDTO projectDTO, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.updateProject(projectId, projectDTO, getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            projectService.deleteProject(projectId, getUserId(currentUser));
            return ResponseEntity.ok("Project deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/managers")
    public ResponseEntity<?> assignManager(@PathVariable String projectId, @RequestBody UserIdRequestDTO userIdRequest, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.assignManager(projectId, userIdRequest.getUserId(), getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}/managers/{userId}")
    public ResponseEntity<?> removeManager(@PathVariable String projectId, @PathVariable String userId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.removeManager(projectId, userId, getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<?> assignMember(@PathVariable String projectId, @RequestBody UserIdRequestDTO userIdRequest, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.assignMember(projectId, userIdRequest.getUserId(), getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable String projectId, @PathVariable String userId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.removeMember(projectId, userId, getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/join")
    public ResponseEntity<?> requestToJoinProject(@PathVariable String projectId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            projectService.requestToJoinProject(projectId, getUserId(currentUser));
            return ResponseEntity.ok("Request to join project sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/join/approve")
    public ResponseEntity<?> approveJoinRequest(@PathVariable String projectId, @RequestBody UserIdRequestDTO userIdRequest, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.approveJoinRequest(projectId, userIdRequest.getUserId(), getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/join/reject")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable String projectId, @RequestBody UserIdRequestDTO userIdRequest, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            Project updatedProject = projectService.rejectJoinRequest(projectId, userIdRequest.getUserId(), getUserId(currentUser));
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/leave")
    public ResponseEntity<?> leaveProject(@PathVariable String projectId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            projectService.leaveProject(projectId, getUserId(currentUser));
            return ResponseEntity.ok("You have left the project.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
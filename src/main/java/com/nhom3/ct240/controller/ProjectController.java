package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.ProjectDTO.ProjectDTO;
import com.nhom3.ct240.dto.UserDTO.UserIdRequestDTO;
import com.nhom3.ct240.entity.Project;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.ProjectService;
import com.nhom3.ct240.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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

    private String getUserId(UserDetails currentUser) {
        return userService.findByUsername(currentUser.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            return ResponseEntity.ok(projectService.getAllProjects(getUserId(currentUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/all-system")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getAllSystemProjects() {
        try {
            return ResponseEntity.ok(projectService.getAllSystemProjects());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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
    // Bỏ @PreAuthorize để cho phép cả Admin và Owner (Member) gọi. Logic kiểm tra quyền nằm ở Service.
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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

    @PostMapping("/{projectId}/join/cancel")
    public ResponseEntity<?> cancelJoinRequest(@PathVariable String projectId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        try {
            projectService.cancelJoinRequest(projectId, getUserId(currentUser));
            return ResponseEntity.ok("Join request cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/join/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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
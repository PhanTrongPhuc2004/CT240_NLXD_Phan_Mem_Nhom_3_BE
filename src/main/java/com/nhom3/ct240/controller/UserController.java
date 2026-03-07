package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.UserIdsRequestDTO;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.findByUsername(currentUser.getUsername()).orElse(null));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile() {
        // TODO: CN_04
        return null;
    }

    // --- API CHO ADMIN/MANAGER ---
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // DEBUG LOG
        System.out.println("User: " + currentUser.getUsername());
        System.out.println("Authorities: " + currentUser.getAuthorities());

        // Kiểm tra quyền: Chỉ ADMIN hoặc MANAGER mới được xem danh sách user
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        boolean isAdminOrManager = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdminOrManager) {
            System.out.println("Access Denied: User does not have ROLE_ADMIN or ROLE_MANAGER");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }

        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // --- API TÌM KIẾM USER (Cho mọi user đã đăng nhập) ---
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Keyword cannot be empty");
        }

        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    // --- API LẤY DANH SÁCH USER THEO ID (Cho mọi user đã đăng nhập) ---
    @PostMapping("/list")
    public ResponseEntity<?> getUsersByIds(@RequestBody UserIdsRequestDTO request, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("User IDs list cannot be empty");
        }

        List<User> users = userService.getUsersByIds(request.getUserIds());
        return ResponseEntity.ok(users);
    }
}
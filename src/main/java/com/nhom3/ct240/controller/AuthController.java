package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.AuthDTO.AuthResponse;
import com.nhom3.ct240.dto.AuthDTO.LoginRequest;
import com.nhom3.ct240.dto.AuthDTO.RegisterRequest;
import com.nhom3.ct240.dto.UserDTO.UserResponseDTO;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.service.UserService;
import com.nhom3.ct240.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý xác thực: đăng ký, đăng nhập, lấy thông tin user hiện tại
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            );

            // Load UserDetails để tạo token
            UserDetails userDetails = userService.loadUserByUsername(registeredUser.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            // Chỉ trả về các trường cần thiết, không có password
            UserResponseDTO userResponse = new UserResponseDTO(
                    registeredUser.getId(),
                    registeredUser.getUsername(),
                    registeredUser.getEmail(),
                    registeredUser.getFullName(),
                    registeredUser.getAvatarUrl(),
                    registeredUser.getRole(),
                    registeredUser.isEnabled()
            );

            AuthResponse response = new AuthResponse(token, "Đăng ký thành công", userResponse);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Lỗi server: " + e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Lưu authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // Lấy user từ DB
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after login"));

            // Chỉ trả về các trường cần thiết, không có password
            UserResponseDTO userResponse = new UserResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.isEnabled()
            );

            AuthResponse response = new AuthResponse(token, "Đăng nhập thành công", userResponse);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Tài khoản hoặc mật khẩu không đúng", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Lỗi server: " + e.getMessage(), null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isEnabled()
        );

        return ResponseEntity.ok(userResponse);
    }
}
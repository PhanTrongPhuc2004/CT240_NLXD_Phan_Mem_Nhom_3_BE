package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.RegisterRequest;
import com.nhom3.ct240.dto.LoginRequest;
import com.nhom3.ct240.dto.AuthResponse;
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
import org.springframework.web.bind.annotation.*;

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
            // Đăng ký user vào DB
            User registeredUser = userService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            );

            // Load UserDetails từ UserService
            var userDetails = userService.loadUserByUsername(registeredUser.getUsername());

            // Kiểm tra null an toàn (dù thực tế loadUserByUsername sẽ ném exception nếu không tìm thấy)
//            if (userDetails == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(new AuthResponse(null, "Không thể tạo token: user details không hợp lệ"));
//            }

            // Tạo JWT token
            String token = jwtUtil.generateToken(userDetails);

            AuthResponse response = new AuthResponse(token, "Đăng ký thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Username hoặc email đã tồn tại
            return ResponseEntity.badRequest().body(new AuthResponse(null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Xác thực username/password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Lấy UserDetails từ principal
            var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            // Kiểm tra null an toàn
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AuthResponse(null, "Không thể tạo token: user details không hợp lệ"));
            }

            // Tạo JWT token
            String token = jwtUtil.generateToken(userDetails);

            AuthResponse response = new AuthResponse(token, "Đăng nhập thành công");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Tài khoản hoặc mật khẩu không đúng"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Lỗi server: " + e.getMessage()));
        }
    }
}
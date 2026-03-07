// src/main/java/com/nhom3/ct240/service/UserService.java - ĐÃ CẬP NHẬT: Đảm bảo tạo đầy đủ thuộc tính khi register
package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.UserDTO.*;
import com.nhom3.ct240.entity.User;
import com.nhom3.ct240.entity.enums.Role;
import com.nhom3.ct240.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User register(String username, String email, String password, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setAvatarUrl(null); // Mặc định null, FE có thể update sau
        user.setRole(Role.MEMBER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setOwnedProjectIds(new ArrayList<>());
        user.setParticipatingProjectIds(new ArrayList<>());

        return userRepository.save(user);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User updateProfile(String username, UserUpdateDTO updateDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        boolean hasChange = false;

        if (updateDTO.getFullName() != null && !updateDTO.getFullName().isBlank()) {
            user.setFullName(updateDTO.getFullName());
            hasChange = true;
        }

        if (updateDTO.getAvatarUrl() != null && !updateDTO.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
            hasChange = true;
        }

        if (hasChange) {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        // Nếu không thay đổi gì → trả về user hiện tại (không save lại)
        return user;
    }

    @Transactional
    public User updateUser(String id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        boolean hasChange = false;

        if (updateDTO.getFullName() != null && !updateDTO.getFullName().isBlank()) {
            user.setFullName(updateDTO.getFullName());
            hasChange = true;
        }

        if (updateDTO.getAvatarUrl() != null && !updateDTO.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
            hasChange = true;
        }

        if (hasChange) {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        return user;
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        userRepository.delete(user);
    }

    @Transactional
    public User updateRole(String id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
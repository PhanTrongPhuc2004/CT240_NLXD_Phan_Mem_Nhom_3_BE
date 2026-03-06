// src/main/java/com/nhom3/ct240/dto/AuthResponse.java
package com.nhom3.ct240.dto;

import com.nhom3.ct240.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String message;
    private User user;  // Thêm trường này để trả role và info user

    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public AuthResponse(String token, String message, User user) {
        this.token = token;
        this.message = message;
        this.user = user;
    }
}
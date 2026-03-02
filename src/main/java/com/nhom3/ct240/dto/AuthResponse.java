package com.nhom3.ct240.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String message;

    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}
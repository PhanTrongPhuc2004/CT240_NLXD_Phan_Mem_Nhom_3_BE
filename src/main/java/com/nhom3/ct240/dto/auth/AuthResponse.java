package com.nhom3.ct240.dto.AuthDTO;

import com.nhom3.ct240.dto.UserDTO.UserResponseDTO;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String message;
    private UserResponseDTO user;

    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public AuthResponse(String token, String message, UserResponseDTO user) {
        this.token = token;
        this.message = message;
        this.user = user;
    }
}
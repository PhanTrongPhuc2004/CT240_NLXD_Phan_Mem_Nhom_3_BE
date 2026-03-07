package com.nhom3.ct240.dto.UserDTO;

import com.nhom3.ct240.entity.enums.Role;
import lombok.Data;

@Data
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Role role;
    private boolean active;

    public UserResponseDTO(String id, String username, String email, String fullName, String avatarUrl, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.active = active;
    }
}
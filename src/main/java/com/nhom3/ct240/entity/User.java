package com.nhom3.ct240.entity;

import com.nhom3.ct240.entity.enums.Role;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fullName;

    private String avatarUrl;

    private Role role = Role.MEMBER;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private List<String> ownedProjectIds = new ArrayList<>();

    private List<String> participatingProjectIds = new ArrayList<>();

    // --- UserDetails Methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Hoặc logic kiểm tra riêng
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Hoặc logic kiểm tra riêng
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Hoặc logic kiểm tra riêng
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }
}
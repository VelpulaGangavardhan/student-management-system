package com.studentmanagement.dto.response;

import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public UserResponseDTO(Long id, String username, String email, String role, boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

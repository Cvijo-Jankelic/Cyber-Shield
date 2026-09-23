package com.project.cybershield.builders;

import com.project.cybershield.entities.User;
import com.project.cybershield.enums.Role;

import java.time.LocalDateTime;

public class UserBuilder {
    private Long id;
    private String email;
    private String username;
    private String password;
    private Role role;
    private LocalDateTime createdAt;

    public UserBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public UserBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder setRole(Role role) {
        this.role = role;
        return this;
    }

    public UserBuilder setCreated_at(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public User createUser() {
        return new User(id, email, username, password, role, createdAt);
    }
}

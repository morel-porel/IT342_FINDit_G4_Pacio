package com.example.findit.feature.user.dto;

import com.example.findit.feature.user.User;
import java.time.LocalDateTime;

public class UserResponse {
    public Long id;
    public String fullName;
    public String email;
    public String role;
    public LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.fullName = user.getFullName();
        r.email = user.getEmail();
        r.role = user.getRole();
        r.createdAt = user.getCreatedAt();
        return r;
    }
}

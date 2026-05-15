package com.example.findit.feature.auth.dto;

import com.example.findit.feature.user.User;
import com.example.findit.feature.user.dto.UserResponse;

public class AuthResponse {
    private String token;
    private UserResponse user;

    public AuthResponse(String token, UserResponse user){
        this.token = token;
        this.user = user;
    }
    public String getToken(){return token;}
    public UserResponse getUser(){return user;}
}


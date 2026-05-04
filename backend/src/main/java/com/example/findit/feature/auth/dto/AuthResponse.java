package com.example.findit.feature.auth.dto;

import com.example.findit.feature.user.User;

public class AuthResponse {
    private String token;
    private User user;

    public AuthResponse(String token, User user){
        this.token = token;
        this.user = user;
    }
    public String getToken(){return token;}
    public User getUser(){return user;}
}


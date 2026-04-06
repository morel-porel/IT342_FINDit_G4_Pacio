package com.example.findit.factory;

import com.example.findit.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createOAuthUser(String fullName, String email,
                                String oauthProvider, String oauthId) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setOauthProvider(oauthProvider);
        user.setOauthId(oauthId);
        user.setPasswordHash("OAUTH_NO_PASSWORD");
        return user;
    }

    public User createEmailUser(String fullName, String email, String passwordHash) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
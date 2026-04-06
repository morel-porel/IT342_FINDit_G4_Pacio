package com.example.findit.security;

import com.example.findit.entity.User;
import com.example.findit.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationProxy {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthenticationProxy(TokenProvider tokenProvider,
                               UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public User resolveAuthenticatedUser(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer "))
            return null;
        String token = bearerToken.substring(7);
        if (!tokenProvider.validateToken(token))
            return null;
        Long userId = tokenProvider.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .filter(User::getIsActive)
                .orElse(null);
    }
}
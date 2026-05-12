package com.example.findit;
import com.example.findit.feature.auth.AuthService;
import com.example.findit.feature.auth.dto.RegisterRequest;
import com.example.findit.feature.auth.dto.LoginRequest;
import com.example.findit.feature.auth.security.TokenProvider;
import com.example.findit.feature.user.User;
import com.example.findit.feature.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProvider tokenProvider;

    @InjectMocks private AuthService authService;

    @Test
    void register_ShouldHashPasswordAndSaveUser() {
        RegisterRequest req = new RegisterRequest();
        req.email = "test@test.com";
        req.fullName = "Test User";
        req.password = "password123";
        req.confirmPassword = "password123";

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_pass");
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        User result = authService.register(req);

        assertEquals("hashed_pass", result.getPasswordHash());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowIfEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.email = "duplicate@test.com";
        when(userRepository.existsByEmail(req.email)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(req));
    }

    @Test
    void login_ShouldReturnTokenOnValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.email = "test@test.com";
        req.password = "pass123";

        User user = new User();
        user.setEmail(req.email);
        user.setPasswordHash("hashed_pass");
        user.setActive(true);

        when(userRepository.findByEmail(req.email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password, "hashed_pass")).thenReturn(true);
        when(tokenProvider.createToken(user)).thenReturn("mock_token");

        var response = authService.login(req);

        assertEquals("mock_token", response.getToken());
    }
}
package com.example.findit.feature.auth;

import com.example.findit.feature.auth.dto.AuthResponse;
import com.example.findit.feature.auth.dto.LoginRequest;
import com.example.findit.feature.auth.dto.RegisterRequest;
import com.example.findit.feature.user.User;
import com.example.findit.feature.user.UserRepository;
import com.example.findit.feature.auth.security.TokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }
    public User register(RegisterRequest request){
        if(userRepository.existsByEmail(request.email))throw new RuntimeException("Email already registered");
        if (!request.password.equals(request.confirmPassword)) throw new RuntimeException("Passwords do not match");
        User user = new User();
        user.setFullName(request.fullName);
        user.setEmail(request.email);
        user.setPasswordHash(passwordEncoder.encode(request.password));
        return  userRepository.save(user);
    }
    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email).orElseThrow(()-> new RuntimeException("Invalid email or password"));
        if(!passwordEncoder.matches(request.password, user.getPasswordHash())){
            throw new RuntimeException("Invalid email or password");
        }
        if (!user.getIsActive()) throw new RuntimeException("Account has been deactivated");
        String token = tokenProvider.createToken(user);
        return  new AuthResponse(token,user);
    }
    public boolean validateToken (String token){
        return tokenProvider.validateToken(token);
    }
    public User getUserFromToken(String token){
        if(!validateToken(token)){throw new RuntimeException("Invalid or Expired Token");}
        return userRepository.findById(tokenProvider.getUserIdFromToken(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

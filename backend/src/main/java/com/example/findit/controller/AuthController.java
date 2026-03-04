package com.example.findit.controller;

import com.example.findit.dto.LoginRequest;
import com.example.findit.dto.RegisterRequest;
import com.example.findit.entity.User;
import com.example.findit.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<?>login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token){
        return ResponseEntity.ok("Logged out successfully");
    }
    @GetMapping("/me")
    public ResponseEntity<?>getProfile(@RequestHeader("Authorization") String token){
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            String actualToken = token.substring(7);
            User user = authService.getUserFromToken(actualToken);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error: " + e.getMessage());
        }
    }
}

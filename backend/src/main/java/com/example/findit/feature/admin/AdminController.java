package com.example.findit.feature.admin;

import com.example.findit.feature.admin.dto.UpdateUserRequest;
import com.example.findit.feature.user.User;
import com.example.findit.feature.user.UserRepository;
import com.example.findit.feature.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminController — manages user accounts from the admin panel.
 *
 * SDD Admin Panel feature:
 *   GET /api/admin/users       — list all users
 *   PUT /api/admin/users/{id}  — update user (e.g., deactivate account)
 *
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /api/admin/users — list all user accounts
     * SDD: Admin can view all users; ADMIN-role accounts cannot be deactivated from UI
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/admin/users/{id} — update user account status
     * SDD: Admin can deactivate users. Body: { isActive: false }
     * SDD WEB-13: Deactivate button absent for ADMIN accounts (enforced here)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER-001: User not found"));

        // SDD: Deactivate is absent for ADMIN accounts — admins cannot deactivate other admins
        if ("ADMIN".equals(user.getRole()) && Boolean.FALSE.equals(request.isActive)) {
            return ResponseEntity.badRequest().body("AUTH-003: Cannot deactivate an admin account");
        }

        if (request.isActive != null) {
            user.setActive(request.isActive);
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(saved));
    }
}

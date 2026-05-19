package com.example.findit.feature.admin.dto;

/**
 * Request body for PUT /api/admin/users/{id}
 * SDD: Update user account status (e.g., deactivate). Body: { isActive: false }
 */
public class UpdateUserRequest {
    public Boolean isActive;
}

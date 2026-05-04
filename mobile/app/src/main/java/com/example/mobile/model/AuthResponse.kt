package com.example.mobile.model

data class AuthResponse(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val createdAt: String
)
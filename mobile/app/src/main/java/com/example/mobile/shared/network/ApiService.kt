package com.example.mobile.shared.network

import com.example.mobile.feature.auth.model.AuthResponse
import com.example.mobile.feature.auth.model.LoginRequest
import com.example.mobile.feature.auth.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
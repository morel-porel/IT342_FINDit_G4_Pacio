package com.example.mobile.network

import com.example.mobile.model.AuthResponse
import com.example.mobile.model.LoginRequest
import com.example.mobile.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
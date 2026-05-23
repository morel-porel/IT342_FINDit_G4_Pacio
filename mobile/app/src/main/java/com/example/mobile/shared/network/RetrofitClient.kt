package com.example.mobile.shared.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Use 10.0.2.2 for Android emulator → maps to host machine localhost
    private const val BASE_URL = "http://192.168.1.14:8080/api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

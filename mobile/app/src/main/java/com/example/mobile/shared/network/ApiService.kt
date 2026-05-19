package com.example.mobile.shared.network

import com.example.mobile.feature.auth.model.AuthResponse
import com.example.mobile.feature.auth.model.LoginRequest
import com.example.mobile.feature.auth.model.RegisterRequest
import com.example.mobile.feature.claim.model.ClaimResponse
import com.example.mobile.feature.item.model.ItemResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Any>

    // ── Items ───────────────────────────────────────────────────────────────

    /**
     * GET /api/items  — public feed.
     * Optional query params: type=LOST|FOUND, status=OPEN, category=
     */
    @GET("items")
    suspend fun getItems(
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("category") category: String? = null
    ): Response<List<ItemResponse>>

    /** GET /api/items/{id} — full detail including weatherContext */
    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: Long): Response<ItemResponse>

    /** GET /api/items/my — items belonging to the authenticated user */
    @GET("items/my")
    suspend fun getMyItems(
        @Header("Authorization") token: String
    ): Response<List<ItemResponse>>

    /**
     * POST /api/items — multipart form-data.
     * photo is required for FOUND items, optional for LOST.
     */
    @Multipart
    @POST("items")
    suspend fun reportItem(
        @Header("Authorization") token: String,
        @Part("type") type: RequestBody,
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("dateLostFound") dateLostFound: RequestBody,
        @Part("location") location: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<ItemResponse>

    /** PATCH /api/items/{id}/resolve — owner marks their own lost item as resolved */
    @PATCH("items/{id}/resolve")
    suspend fun resolveItem(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ItemResponse>

    // ── Claims ──────────────────────────────────────────────────────────────

    /**
     * POST /api/claims — multipart form-data.
     * proofImage is optional.
     */
    @Multipart
    @POST("claims")
    suspend fun submitClaim(
        @Header("Authorization") token: String,
        @Part("itemId") itemId: RequestBody,
        @Part("proofDescription") proofDescription: RequestBody,
        @Part proofImage: MultipartBody.Part?
    ): Response<ClaimResponse>

    /**
     * GET /api/claims — USER sees own claims; ADMIN sees all.
     */
    @GET("claims")
    suspend fun getClaims(
        @Header("Authorization") token: String
    ): Response<List<ClaimResponse>>
}

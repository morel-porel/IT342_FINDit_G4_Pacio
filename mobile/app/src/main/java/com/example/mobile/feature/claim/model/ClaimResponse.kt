package com.example.mobile.feature.claim.model

data class ClaimResponse(
    val id: Long,
    val itemId: Long,
    val itemName: String,
    val itemLocation: String,
    val claimantId: Long,
    val claimantName: String,
    val proofDescription: String,
    val proofImageUrl: String?,
    val status: String,     // PENDING | APPROVED | REJECTED
    val createdAt: String
)

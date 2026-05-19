package com.example.mobile.feature.item.model

data class ItemResponse(
    val id: Long,
    val type: String,           // LOST | FOUND
    val name: String,
    val category: String,
    val description: String?,
    val dateLostFound: String,
    val location: String,
    val imageUrl: String?,
    val weatherContext: String?,
    val status: String,         // OPEN | PENDING | APPROVED | REJECTED | RESOLVED
    val createdAt: String,
    val reporter: ReporterInfo
)

data class ReporterInfo(
    val id: Long,
    val fullName: String
)

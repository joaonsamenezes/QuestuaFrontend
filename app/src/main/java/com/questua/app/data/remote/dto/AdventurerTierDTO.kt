package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdventurerTierRequestDTO(
    val keyName: String,
    val nameDisplay: String,
    val iconUrl: String? = null,
    val colorHex: String? = null,
    val orderIndex: Int,
    val levelRequired: Int
)

@Serializable
data class AdventurerTierResponseDTO(
    val id: String,
    val keyName: String,
    val nameDisplay: String,
    val iconUrl: String?,
    val colorHex: String?,
    val orderIndex: Int,
    val levelRequired: Int,
    val createdAt: String
)
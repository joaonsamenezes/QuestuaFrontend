package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AdventurerTier(
    val id: String,
    val keyName: String,
    val nameDisplay: String,
    val iconUrl: String?,
    val colorHex: String?,
    val orderIndex: Int,
    val levelRequired: Int,
    val createdAt: String
)
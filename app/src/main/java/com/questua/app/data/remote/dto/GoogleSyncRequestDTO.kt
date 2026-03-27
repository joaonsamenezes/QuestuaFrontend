package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSyncRequestDTO(
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val nativeLanguageId: String? = null,
    val cefrLevel: String? = null
)
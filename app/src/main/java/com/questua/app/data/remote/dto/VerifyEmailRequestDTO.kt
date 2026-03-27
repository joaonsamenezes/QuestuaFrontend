package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequestDTO(
    val email: String,
    val code: String
)
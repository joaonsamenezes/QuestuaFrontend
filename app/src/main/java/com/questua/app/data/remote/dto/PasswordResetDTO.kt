package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDTO(
    val email: String
)

@Serializable
data class ResetPasswordRequestDTO(
    val code: String,
    val newPassword: String
)
package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.ForgotPasswordRequestDTO
import com.questua.app.data.remote.dto.GoogleSyncRequestDTO
import com.questua.app.data.remote.dto.LoginRequestDTO
import com.questua.app.data.remote.dto.LoginResponseDTO
import com.questua.app.data.remote.dto.RegisterRequestDTO
import com.questua.app.data.remote.dto.RegisterResponseDTO
import com.questua.app.data.remote.dto.ResetPasswordRequestDTO
import com.questua.app.data.remote.dto.VerifyEmailRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body dto: LoginRequestDTO): Response<LoginResponseDTO>
    @POST("auth/register/init")
    suspend fun registerInit(@Body request: RegisterRequestDTO): Response<Unit>

    @POST("auth/register/verify")
    suspend fun registerVerify(@Body request: VerifyEmailRequestDTO): Response<LoginResponseDTO>
    @POST("auth/google-sync")
    suspend fun syncGoogleUser(@Body dto: GoogleSyncRequestDTO): Response<LoginResponseDTO>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body dto: ForgotPasswordRequestDTO): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body dto: ResetPasswordRequestDTO): Response<Unit>
}
package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.data.remote.dto.RegisterRequestDTO
import com.questua.app.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<String>>
    fun syncGoogleUser(email: String, displayName: String, avatarUrl: String?, nativeLanguageId: String?, cefrLevel: String?): Flow<Resource<String>>
    fun registerInit(request: RegisterRequestDTO): Flow<Resource<Unit>>

    fun registerVerify(email: String, code: String): Flow<Resource<String>>

    fun logout(): Flow<Resource<Unit>>
    suspend fun forgotPassword(email: String)
    suspend fun resetPassword(code: String, newPassword: String)
}
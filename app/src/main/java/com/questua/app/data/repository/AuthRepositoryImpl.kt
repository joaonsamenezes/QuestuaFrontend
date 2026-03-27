package com.questua.app.data.repository

import android.util.Base64
import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.core.network.TokenManager
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.AuthApi
import com.questua.app.data.remote.dto.ForgotPasswordRequestDTO
import com.questua.app.data.remote.dto.GoogleSyncRequestDTO
import com.questua.app.data.remote.dto.LoginRequestDTO
import com.questua.app.data.remote.dto.RegisterRequestDTO
import com.questua.app.data.remote.dto.ResetPasswordRequestDTO
import com.questua.app.data.remote.dto.VerifyEmailRequestDTO
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository, SafeApiCall() {

    override fun login(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.login(LoginRequestDTO(email, password)) }

        if (result is Resource.Success) {
            val token = result.data!!.token
            val userId = extractUserIdFromToken(token)
            if (userId != null) {
                tokenManager.saveAuthData(token, userId)
                emit(Resource.Success(token))
            } else {
                emit(Resource.Error("Token inválido"))
            }
        } else if (result is Resource.Error) {
            emit(Resource.Error(result.message ?: "Erro no login"))
        }
    }

    override fun syncGoogleUser(
        email: String,
        displayName: String,
        avatarUrl: String?,
        nativeLanguageId: String?,
        cefrLevel: String?
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val request = GoogleSyncRequestDTO(email, displayName, avatarUrl, nativeLanguageId, cefrLevel)
        val result = safeApiCall { api.syncGoogleUser(request) }

        if (result is Resource.Success) {
            val token = result.data!!.token
            val userId = extractUserIdFromToken(token)
            if (userId != null) {
                tokenManager.saveAuthData(token, userId)
                emit(Resource.Success(token))
            } else {
                emit(Resource.Error("Identidade do servidor não encontrada no token"))
            }
        } else if (result is Resource.Error) {
            emit(Resource.Error(result.message ?: "Erro na sincronização"))
        }
    }

    private fun extractUserIdFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            val json = JSONObject(payload)
            json.optString("sub", null)
        } catch (e: Exception) {
            null
        }
    }

    override fun registerInit(request: RegisterRequestDTO): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.registerInit(request) }

        if (result is Resource.Success) {
            emit(Resource.Success(Unit))
        } else if (result is Resource.Error) {
            emit(Resource.Error(result.message ?: "Erro na inicialização do registro"))
        }
    }

    override fun registerVerify(email: String, code: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val request = VerifyEmailRequestDTO(email, code)
        val result = safeApiCall { api.registerVerify(request) }

        if (result is Resource.Success) {
            val token = result.data!!.token
            val userId = extractUserIdFromToken(token)
            if (userId != null) {
                tokenManager.saveAuthData(token, userId)
                emit(Resource.Success(token))
            } else {
                emit(Resource.Error("Token inválido"))
            }
        } else if (result is Resource.Error) {
            emit(Resource.Error(result.message ?: "Erro na verificação do registro"))
        }
    }
    override fun logout(): Flow<Resource<Unit>> = flow {
        tokenManager.clearData()
        emit(Resource.Success(Unit))
    }

    override suspend fun forgotPassword(email: String) {
        api.forgotPassword(ForgotPasswordRequestDTO(email))
    }

    override suspend fun resetPassword(code: String, newPassword: String) {
        api.resetPassword(ResetPasswordRequestDTO(code, newPassword))
    }
}
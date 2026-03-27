package com.questua.app.domain.usecase.auth

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(code: String, newPassword: String) = flow {
        try {
            emit(Resource.Loading())
            repository.resetPassword(code, newPassword)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Código inválido ou expirado"))
        }
    }
}
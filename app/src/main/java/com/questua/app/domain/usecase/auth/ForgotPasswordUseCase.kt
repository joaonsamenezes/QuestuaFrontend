package com.questua.app.domain.usecase.auth

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(email: String) = flow {
        try {
            emit(Resource.Loading())
            repository.forgotPassword(email)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro ao enviar e-mail"))
        }
    }
}
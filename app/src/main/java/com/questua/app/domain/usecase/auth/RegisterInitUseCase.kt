package com.questua.app.domain.usecase.auth

import com.questua.app.core.common.Resource
import com.questua.app.data.remote.dto.RegisterRequestDTO
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterInitUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String,
        cefrLevel: String
    ): Flow<Resource<Unit>> {
        val request = RegisterRequestDTO(email, password, displayName, null, nativeLanguageId, cefrLevel)
        return repository.registerInit(request)
    }
}
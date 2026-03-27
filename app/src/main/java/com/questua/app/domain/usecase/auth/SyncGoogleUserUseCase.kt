package com.questua.app.domain.usecase.auth

import com.questua.app.domain.repository.AuthRepository
import javax.inject.Inject

class SyncGoogleUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, displayName: String, avatarUrl: String?, languageId: String?, cefrLevel: String?) =
        repository.syncGoogleUser(email, displayName, avatarUrl, languageId, cefrLevel)
}
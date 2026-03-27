package com.questua.app.domain.usecase.language_learning

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AbandonLanguageUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userLanguageId: String): Flow<Resource<Boolean>> {
        return repository.abandonLanguage(userLanguageId)
    }
}
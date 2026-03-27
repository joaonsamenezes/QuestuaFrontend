package com.questua.app.domain.usecase.admin.users

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(page: Int = 0, size: Int = 20): Flow<Resource<List<UserAccount>>> {
        return repository.getAllUsers(page, size)
    }
}
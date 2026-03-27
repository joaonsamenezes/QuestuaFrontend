package com.questua.app.presentation.usertier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.AdventurerTier
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.LanguageRepository
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardItemUiModel(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val xpTotal: Int,
    val isCurrentUser: Boolean
)

data class UserTierState(
    val currentTier: AdventurerTier? = null,
    val nextTier: AdventurerTier? = null,
    val currentLevel: Int = 0,
    val leaderboardList: List<LeaderboardItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class UserTierViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val adminRepository: AdminRepository,
    private val languageRepository: LanguageRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(UserTierState())
    val state = _state.asStateFlow()

    init {
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    fetchTierData(userId)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Usuário não logado")
                }
            }
        }
    }

    private suspend fun fetchTierData(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        try {
            val statsRes = getUserStatsUseCase(userId).filter { it !is Resource.Loading }.first()
            val activeUserLang = statsRes.data

            val tiersRes = adminRepository.getAdventurerTiers(0, 100).filter { it !is Resource.Loading }.first()
            val allTiers = tiersRes.data?.sortedBy { it.levelRequired } ?: emptyList()

            var currentTier: AdventurerTier? = null
            var nextTier: AdventurerTier? = null
            var currentLevel = 0
            var leaderboardItems: List<LeaderboardItemUiModel> = emptyList()

            if (activeUserLang != null && allTiers.isNotEmpty()) {
                currentLevel = activeUserLang.gamificationLevel
                currentTier = allTiers.find { it.id == activeUserLang.adventurerTierId }
                    ?: allTiers.lastOrNull { currentLevel >= it.levelRequired }
                nextTier = allTiers.firstOrNull { it.levelRequired > currentLevel }

                if (activeUserLang.adventurerTierId != null) {
                    val boardRes = languageRepository.getLeaderboard(activeUserLang.adventurerTierId, activeUserLang.cefrLevel, 0, 50)
                        .filter { it !is Resource.Loading }.first()

                    val rawBoardList = boardRes.data ?: emptyList()

                    leaderboardItems = coroutineScope {
                        rawBoardList.mapIndexed { index, ul ->
                            async {
                                val userAccRes = adminRepository.getUserById(ul.userId)
                                    .filter { it !is Resource.Loading }.first()
                                LeaderboardItemUiModel(
                                    rank = index + 1,
                                    userId = ul.userId,
                                    displayName = userAccRes.data?.displayName ?: "Aventureiro",
                                    avatarUrl = userAccRes.data?.avatarUrl,
                                    xpTotal = ul.xpTotal,
                                    isCurrentUser = ul.userId == userId
                                )
                            }
                        }.awaitAll()
                    }.sortedByDescending { it.xpTotal }.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
                }

                _state.value = UserTierState(
                    currentTier = currentTier,
                    nextTier = nextTier,
                    currentLevel = currentLevel,
                    leaderboardList = leaderboardItems,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Estatísticas ou Tiers não encontrados")
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}
package com.questua.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.usecase.quest.GetQuestIntroUseCase
import com.questua.app.domain.usecase.quest.StartQuestUseCase
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuestIntroUiEvent {
    data class NavigateToGame(val questId: String) : QuestIntroUiEvent()
    data class ShowError(val message: String) : QuestIntroUiEvent()
}

data class QuestIntroState(
    val isLoading: Boolean = false,
    val quest: Quest? = null,
    val userQuest: UserQuest? = null,
    val questPoint: QuestPoint? = null,
    val pendingAchievements: List<Achievement> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class QuestIntroViewModel @Inject constructor(
    private val getQuestIntroUseCase: GetQuestIntroUseCase,
    private val startQuestUseCase: StartQuestUseCase,
    private val adminRepository: AdminRepository,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuestIntroState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<QuestIntroUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val questId: String? = savedStateHandle["questId"]

    init {
        loadData()
    }

    fun loadData() {
        if (questId == null) return

        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            _state.value = _state.value.copy(isLoading = true)

            val introDeferred = async { getQuestIntroUseCase(questId, userId) }
            val achievementsDeferred = async { loadRelatedAchievements(questId, userId) }

            val introFlow = introDeferred.await()
            val achievementsList = achievementsDeferred.await()

            introFlow.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            quest = result.data?.quest,
                            userQuest = result.data?.userQuest,
                            questPoint = result.data?.questPoint,
                            pendingAchievements = achievementsList
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> { }
                }
            }
        }
    }

    private suspend fun loadRelatedAchievements(questId: String, userId: String): List<Achievement> {
        return try {
            val allAchievementsJob = viewModelScope.async {
                adminRepository.getAchievements(null)
                    .filter { it !is Resource.Loading }
                    .first().data ?: emptyList()
            }

            val userAchievementsJob = viewModelScope.async {
                getUserAchievementsUseCase(userId)
                    .filter { it !is Resource.Loading }
                    .first().data ?: emptyList()
            }

            val allAchievements = allAchievementsJob.await()
            val userAchievements = userAchievementsJob.await()
            val userAchievementIds = userAchievements.map { it.achievementId }.toSet()

            allAchievements.filter { achievement ->
                val isTargeted = achievement.targetId == questId

                val isSpecificType = achievement.conditionType.name == "COMPLETE_SPECIFIC_QUEST" ||
                        achievement.conditionType.name == "PERFECT_QUEST_COMPLETION" ||
                        achievement.conditionType.name == "FAST_QUEST_COMPLETION"

                (isTargeted && isSpecificType) && !userAchievementIds.contains(achievement.id)
            }.take(2)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun onStartQuestClicked() {
        val quest = state.value.quest ?: return

        if (quest.isLocked) {
            viewModelScope.launch {
                _uiEvent.send(QuestIntroUiEvent.ShowError(quest.lockMessage ?: "Você não tem o nível necessário para iniciar esta missão."))
            }
            return
        }

        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            if (state.value.userQuest != null) {
                _uiEvent.send(QuestIntroUiEvent.NavigateToGame(quest.id))
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            startQuestUseCase(userId, quest.id).collect { result ->
                when(result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false)
                        _uiEvent.send(QuestIntroUiEvent.NavigateToGame(quest.id))
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                        _uiEvent.send(QuestIntroUiEvent.ShowError(result.message ?: "Erro desconhecido"))
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
}
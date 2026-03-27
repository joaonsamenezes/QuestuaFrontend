package com.questua.app.presentation.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.repository.GameRepository
import com.questua.app.domain.repository.LanguageRepository
import com.questua.app.domain.usecase.user.GetUserProfileUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

data class LevelProgress(
    val currentCefr: String,
    val nextCefrLabel: String,
    val currentQuests: Int,
    val targetQuests: Int,
    val progress: Float,
    val questsToNextCefr: Int
)

data class ResumeQuestItem(
    val userQuest: UserQuest,
    val questTitle: String,
    val questPointTitle: String,
    val imageUrl: String?
)

data class HubContentItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val badgeText: String,
    val type: String
)

data class HubState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val activeLanguage: UserLanguage? = null,
    val activeLanguageDetails: Language? = null,
    val levelProgress: LevelProgress? = null,
    val continueJourneyQuests: List<ResumeQuestItem> = emptyList(),
    val latestCities: List<HubContentItem> = emptyList(),
    val latestQuestPoints: List<HubContentItem> = emptyList(),
    val latestQuests: List<HubContentItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val contentRepository: ContentRepository,
    private val gameRepository: GameRepository,
    private val languageRepository: LanguageRepository,
    private val tokenManager: TokenManager,
    private val achievementMonitor: AchievementMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(HubState())
    val state = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeUserSession()
    }

    fun refreshData() {
        currentUserId?.let { loadHubData(it) }
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    currentUserId = userId
                    loadHubData(userId)
                }
            }
        }
    }

    private suspend fun <T> Flow<Resource<T>>.collectSuccess(): T? {
        return this.filter { it !is Resource.Loading }.firstOrNull()?.data
    }

    private fun loadHubData(userId: String) {
        _state.update { it.copy(isLoading = true) }

        getUserProfileUseCase(userId).onEach { result ->
            if (result is Resource.Success) {
                _state.update { it.copy(user = result.data) }
                achievementMonitor.check()
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val userLanguage = getUserStatsUseCase(userId).collectSuccess()

            if (userLanguage != null) {
                val languageDetails = languageRepository.getLanguageById(userLanguage.languageId).collectSuccess()
                val progress = calculateLevelProgress(userLanguage)
                val userCefr = userLanguage.cefrLevel.uppercase()

                val cefrOrder = listOf("A1", "A2", "B1", "B2", "C1", "C2")
                val userCefrIndex = cefrOrder.indexOf(userCefr).coerceAtLeast(0)

                // Mostra se não tiver requerimento OU se o nível do usuário for MAIOR OU IGUAL ao requerido
                fun isEligible(required: String?): Boolean {
                    if (required.isNullOrBlank()) return true
                    val reqIndex = cefrOrder.indexOf(required.uppercase())
                    return userCefrIndex >= reqIndex
                }

                val unlockedQuestsIds = userLanguage.unlockedContent?.quests ?: emptyList()

                val activeQuestsData = unlockedQuestsIds.map { questId ->
                    async {
                        val status = gameRepository.getUserQuestStatus(questId, userId).collectSuccess()
                        if (status != null && status.status == ProgressStatus.IN_PROGRESS) {
                            val questDetails = gameRepository.getQuestById(questId).collectSuccess()
                            if (questDetails != null) {
                                val questPoint = contentRepository.getQuestPointDetails(questDetails.questPointId).collectSuccess()
                                ResumeQuestItem(
                                    userQuest = status,
                                    questTitle = questDetails.title,
                                    questPointTitle = questPoint?.title ?: "Local",
                                    imageUrl = questPoint?.imageUrl
                                )
                            } else null
                        } else null
                    }
                }.awaitAll().filterNotNull().take(5)

                val allCities = contentRepository.getCities(userLanguage.languageId).collectSuccess() ?: emptyList()
                val eligibleCities = allCities.filter { isEligible(it.unlockRequirement?.requiredCefrLevel) }.reversed()

                val eligiblePoints = mutableListOf<QuestPoint>()
                val eligibleQuests = mutableListOf<Quest>()
                val pointImagesMap = mutableMapOf<String, String?>()

                allCities.forEach { city ->
                    val points = contentRepository.getQuestPoints(city.id).collectSuccess() ?: emptyList()
                    val filteredPointsForCity = points.filter { isEligible(it.unlockRequirement?.requiredCefrLevel) }
                    eligiblePoints.addAll(filteredPointsForCity)

                    filteredPointsForCity.forEach { point ->
                        pointImagesMap[point.id] = point.imageUrl
                        val quests = contentRepository.getQuests(point.id).collectSuccess() ?: emptyList()
                        eligibleQuests.addAll(quests.filter { isEligible(it.unlockRequirement?.requiredCefrLevel) })
                    }
                }

                val cityItems = eligibleCities.take(5).map {
                    HubContentItem(
                        id = it.id,
                        title = it.name,
                        description = it.description,
                        imageUrl = it.imageUrl,
                        badgeText = "Nível ${it.unlockRequirement?.requiredCefrLevel ?: "Livre"}",
                        type = "CITY"
                    )
                }

                val pointItems = eligiblePoints.distinctBy { it.id }.reversed().take(5).map {
                    HubContentItem(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        imageUrl = it.imageUrl,
                        badgeText = "Nível ${it.unlockRequirement?.requiredCefrLevel ?: "Livre"}",
                        type = "QUEST_POINT"
                    )
                }

                val questItems = eligibleQuests.distinctBy { it.id }.reversed().take(5).map {
                    HubContentItem(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        imageUrl = pointImagesMap[it.questPointId],
                        badgeText = "+${it.xpValue} XP",
                        type = "QUEST"
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        activeLanguage = userLanguage,
                        activeLanguageDetails = languageDetails,
                        levelProgress = progress,
                        continueJourneyQuests = activeQuestsData,
                        latestCities = cityItems,
                        latestQuestPoints = pointItems,
                        latestQuests = questItems
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateLevelProgress(userLang: UserLanguage): LevelProgress {
        val cefrLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        val currentCefr = userLang.cefrLevel.uppercase()
        val currentCefrIndex = cefrLevels.indexOf(currentCefr).coerceAtLeast(0)
        val nextCefrIndex = (currentCefrIndex + 1).coerceAtMost(cefrLevels.lastIndex)
        val nextCefrLabel = cefrLevels[nextCefrIndex]

        val threshold = when (currentCefr) {
            "A1" -> 20
            "A2" -> 30
            "B1" -> 50
            "B2" -> 50
            "C1" -> 50
            else -> 50
        }

        val currentQuests = userLang.questsTowardsNextLevel
        val questsToNextCefr = max(0, threshold - currentQuests)
        val progressPercent = if (currentCefr == "C2") 1f else (currentQuests.toFloat() / threshold.toFloat()).coerceIn(0f, 1f)

        return LevelProgress(
            currentCefr = currentCefr,
            nextCefrLabel = nextCefrLabel,
            currentQuests = currentQuests,
            targetQuests = threshold,
            progress = progressPercent,
            questsToNextCefr = questsToNextCefr
        )
    }
}
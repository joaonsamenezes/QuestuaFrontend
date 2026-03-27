package com.questua.app.presentation.exploration.worldmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.AdventurerTier
import com.questua.app.domain.model.City
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.repository.GameRepository
import com.questua.app.domain.repository.LanguageRepository
import com.questua.app.domain.usecase.exploration.GetWorldMapUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityUiModel(
    val city: City,
    val isUnlocked: Boolean
)

data class WorldMapState(
    val isLoading: Boolean = false,
    val cities: List<CityUiModel> = emptyList(),
    val activeCefrLevel: String = "A1",
    val activeLanguage: Language? = null,
    val completedQuestsCount: Int = 0,
    val adventurerTier: AdventurerTier? = null,
    val error: String? = null
)

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val getWorldMapUseCase: GetWorldMapUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val languageRepository: LanguageRepository,
    private val contentRepository: ContentRepository,
    private val gameRepository: GameRepository,
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(WorldMapState())
    val state = _state.asStateFlow()

    private var mapJob: Job? = null
    private var dataJob: Job? = null

    init {
        loadMapData()
    }

    fun refreshData() {
        loadMapData()
    }

    private fun loadMapData() {
        mapJob?.cancel()
        mapJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = tokenManager.userId.firstOrNull()

            if (userId.isNullOrEmpty()) {
                _state.update { it.copy(isLoading = false, error = "Usuário não autenticado.") }
                return@launch
            }

            getUserStatsUseCase(userId).collectLatest { statsResult ->
                when (statsResult) {
                    is Resource.Success -> {
                        val userLang = statsResult.data
                        if (userLang != null) {
                            fetchSecondaryData(
                                userId = userId,
                                languageId = userLang.languageId,
                                cefrLevel = userLang.cefrLevel,
                                tierId = userLang.adventurerTierId,
                                unlockedCities = userLang.unlockedContent?.cities ?: emptyList(),
                                unlockedQuests = userLang.unlockedContent?.quests ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = statsResult.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun fetchSecondaryData(
        userId: String,
        languageId: String,
        cefrLevel: String,
        tierId: String?,
        unlockedCities: List<String>,
        unlockedQuests: List<String>
    ) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            val tierDeferred = async {
                if (tierId != null) {
                    adminRepository.getAdventurerTiers(0, 100).filter { it !is Resource.Loading }.firstOrNull()?.data?.find { it.id == tierId }
                } else null
            }

            val languageDeferred = async {
                languageRepository.getLanguageById(languageId).filter { it !is Resource.Loading }.firstOrNull()?.data
            }

            val mapDeferred = async {
                getWorldMapUseCase(languageId).filter { it !is Resource.Loading }.firstOrNull()
            }

            val currentTier = tierDeferred.await()
            val languageData = languageDeferred.await()
            val mapResult = mapDeferred.await()

            if (mapResult is Resource.Success) {
                val cityList = mapResult.data ?: emptyList()

                val pointDeferreds = cityList.map { city ->
                    async { contentRepository.getQuestPoints(city.id).filter { it !is Resource.Loading }.firstOrNull()?.data ?: emptyList() }
                }
                val allPoints = pointDeferreds.awaitAll().flatten()

                val questDeferreds = allPoints.map { point ->
                    async { contentRepository.getQuests(point.id).filter { it !is Resource.Loading }.firstOrNull()?.data ?: emptyList() }
                }
                val allQuestsForLanguage = questDeferreds.awaitAll().flatten().map { it.id }.toSet()

                val relevantUnlockedQuests = unlockedQuests.filter { allQuestsForLanguage.contains(it) }

                val statusDeferreds = relevantUnlockedQuests.map { questId ->
                    async {
                        gameRepository.getUserQuestStatus(questId, userId).filter { it !is Resource.Loading }.firstOrNull()?.data
                    }
                }

                val calculatedQuestsCompleted = statusDeferreds.awaitAll().count { it?.status == ProgressStatus.COMPLETED }

                val uiList = cityList.map { city ->
                    CityUiModel(city, isUnlocked = unlockedCities.contains(city.id))
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        activeCefrLevel = cefrLevel,
                        activeLanguage = languageData,
                        completedQuestsCount = calculatedQuestsCompleted,
                        adventurerTier = currentTier,
                        cities = uiList
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Falha ao carregar destinos.") }
            }
        }
    }
}
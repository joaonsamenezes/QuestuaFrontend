package com.questua.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminHomeState(
    val isLoading: Boolean = false,
    val counts: Map<String, Int> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class AdminGeneralManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val languageRepository: LanguageRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AdminHomeState>(AdminHomeState())
    val state = _state.asStateFlow()

    fun refreshStats() {
        _state.value = _state.value.copy(isLoading = true)

        val currentCounts = mutableMapOf<String, Int>()

        // 1. Idiomas (LanguageRepository como no AdminLanguageViewModel)
        languageRepository.getAvailableLanguages(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("languages", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 2. Ranks (AdminRepository como no AdminAdventurerTierViewModel)
        adminRepository.getAdventurerTiers(0, 100).onEach { result ->
            if (result is Resource.Success) {
                updateCount("adventurer_tiers", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 3. Cidades (AdminRepository como no AdminCityViewModel)
        adminRepository.getCities(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("cities", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 4. Quests (AdminRepository como no AdminQuestViewModel)
        adminRepository.getQuests(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("quests", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 5. Locais (AdminRepository como no AdminQuestPointViewModel)
        adminRepository.getQuestPoints(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("quest_points", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 6. Diálogos (AdminRepository como no AdminDialogueViewModel)
        adminRepository.getDialogues(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("dialogues", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 7. NPCs (AdminRepository como no AdminCharacterViewModel)
        adminRepository.getCharacters(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("characters", result.data?.size ?: 0)
            }
        }.launchIn(viewModelScope)

        // 8. Conquistas (AdminRepository como no AdminAchievementViewModel)
        adminRepository.getAchievements(null).onEach { result ->
            if (result is Resource.Success) {
                updateCount("achievements", result.data?.size ?: 0)
                _state.value = _state.value.copy(isLoading = false)
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    private fun updateCount(id: String, count: Int) {
        val newCounts = _state.value.counts.toMutableMap()
        newCounts[id] = count
        _state.value = _state.value.copy(counts = newCounts)
    }
}
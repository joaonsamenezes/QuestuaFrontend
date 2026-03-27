package com.questua.app.presentation.admin.content.adventurertiers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.AdventurerTier
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminAdventurerTierDetailState(
    val isLoading: Boolean = false,
    val tier: AdventurerTier? = null,
    val error: String? = null,
    val deleteSuccess: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AdminAdventurerTierDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(AdminAdventurerTierDetailState())
    val state = _state.asStateFlow()

    val tierId: String? = savedStateHandle.get<String>("tierId").let { if (it == "new") null else it }

    init {
        tierId?.let { loadTier(it) }
    }

    fun loadTier(id: String) {
        viewModelScope.launch {
            adminRepository.getAdventurerTierById(id).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, tier = result.data)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun saveTier(keyName: String, nameDisplay: String, iconFile: File?, colorHex: String?, orderIndex: Int, levelRequired: Int) {
        viewModelScope.launch {
            adminRepository.saveAdventurerTier(tierId, keyName, nameDisplay, iconFile, colorHex, orderIndex, levelRequired).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, saveSuccess = true)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun deleteTier() {
        tierId?.let { id ->
            viewModelScope.launch {
                adminRepository.deleteAdventurerTier(id).collect { result ->
                    when (result) {
                        is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                        is Resource.Success -> _state.value = _state.value.copy(isLoading = false, deleteSuccess = true)
                        is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
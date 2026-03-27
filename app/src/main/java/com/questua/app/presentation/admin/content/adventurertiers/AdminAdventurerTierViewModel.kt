package com.questua.app.presentation.admin.content.adventurertiers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.ui.managers.SnackbarManager
import com.questua.app.domain.model.AdventurerTier
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminAdventurerTierState(
    val isLoading: Boolean = false,
    val tiers: List<AdventurerTier> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminAdventurerTierViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminAdventurerTierState())
    val state = _state.asStateFlow()

    init {
        loadTiers()
    }

    fun loadTiers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            adminRepository.getAdventurerTiers(0, 100).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, tiers = result.data?.sortedBy { it.orderIndex } ?: emptyList(), error = null)
                    is Resource.Error -> {
                        SnackbarManager.showError(result.message ?: "Erro ao carregar ranks")
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }
}
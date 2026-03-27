package com.questua.app.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.components.MascotMood
import com.questua.app.domain.model.TutorialStep
import com.questua.app.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    val tutorialSteps = listOf(
        TutorialStep("Bem-vindo ao acampamento base! Explore o mundo através do Mapa.", MascotMood.IDLE, "map_tab"),
        TutorialStep("Aqui no Progresso, você verá suas conquistas e medalhas.", MascotMood.HAPPY, "progress_tab"),
        TutorialStep("No Perfil, você ajusta seus equipamentos de aventureiro.", MascotMood.SURPRISED, "profile_tab"),
        // Texto atualizado para a finalização no Mapa
        TutorialStep("Sua jornada começa agora! Escolha uma cidade no mapa para explorar seu interior e enfrentar desafios.", MascotMood.HAPPY, "map_area")
    )

    var currentTutorialStep by mutableIntStateOf(-1)
        private set

    val targetPositions = mutableStateMapOf<String, Pair<Offset, Size>>()

    init {
        tokenManager.token.onEach { token ->
            _startDestination.value = if (token.isNullOrBlank()) Screen.Initial.route else Screen.Home.route
        }.launchIn(viewModelScope)
    }

    fun startTutorialForNewUser() {
        currentTutorialStep = 0
    }

    fun nextTutorialStep() {
        if (currentTutorialStep < tutorialSteps.size - 1) {
            currentTutorialStep++
        } else {
            currentTutorialStep = -1
        }
    }

    fun updatePosition(key: String, offset: Offset, size: Size) {
        targetPositions[key] = offset to size
    }
}
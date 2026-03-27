package com.questua.app.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AssessmentOption(
    val id: String,
    val text: String,
    val description: String,
    val cefrLevel: String
)

@HiltViewModel
class PlacementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val languageId: String = checkNotNull(savedStateHandle["languageId"])
    val languageName: String = checkNotNull(savedStateHandle["languageName"])

    val options = listOf(
        AssessmentOption(
            id = "a1",
            text = "Sou iniciante",
            description = "Sei apenas algumas palavras e frases como \"Olá\" e \"Obrigado\".",
            cefrLevel = "A1"
        ),
        AssessmentOption(
            id = "a2",
            text = "Sei o básico",
            description = "Consigo me apresentar, pedir comida e entender conversas simples.",
            cefrLevel = "A2"
        ),
        AssessmentOption(
            id = "b1",
            text = "Consigo me virar",
            description = "Posso viajar, conversar sobre minha rotina e entender textos.",
            cefrLevel = "B1"
        ),
        AssessmentOption(
            id = "b2",
            text = "Tenho nível intermediário",
            description = "Consigo manter conversas fluídas e assistir filmes com esforço.",
            cefrLevel = "B2"
        ),
        AssessmentOption(
            id = "c1",
            text = "Tenho nível avançado",
            description = "Leio livros, trabalho na língua e compreendo com naturalidade.",
            cefrLevel = "C1"
        )
    )

    private val _selectedLevel = MutableStateFlow<String?>(null)
    val selectedLevel = _selectedLevel.asStateFlow()

    fun selectLevel(cefrLevel: String, onFinished: (String) -> Unit) {
        _selectedLevel.update { cefrLevel }
        onFinished(cefrLevel)
    }
}
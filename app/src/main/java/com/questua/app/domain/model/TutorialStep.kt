package com.questua.app.domain.model

import com.questua.app.core.ui.components.MascotMood

data class TutorialStep(
    val text: String,
    val mood: MascotMood,
    val targetKey: String
)
package com.questua.app.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.questua.app.core.ui.components.ExplorerSpeechBubble
import com.questua.app.core.ui.components.MascotMood
import com.questua.app.core.ui.components.QuestuaButton

@Composable
fun IntroExplorationScreen(
    onContinue: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val steps = listOf(
        "Saudações, Recruta! Eu serei seu guia. No Questua, o mundo real é sua sala de aula." to MascotMood.IDLE,
        "Aqui, você não apenas estuda gramática. Você explora cidades reais e resolve mistérios usando o idioma local!" to MascotMood.THINKING,
        "Pronto para transformar sua curiosidade em fluência e se tornar um mestre explorador?" to MascotMood.SURPRISED
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "speech_bubble_anim"
                ) { step ->
                    ExplorerSpeechBubble(
                        text = steps[step].first,
                        mood = steps[step].second
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuestuaButton(
                    text = if (currentStep < steps.size - 1) "PRÓXIMO" else "VAMOS NESSA!",
                    onClick = {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            onContinue()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuestuaButton(
                    text = if (currentStep == 0) "AGORA NÃO" else "VOLTAR",
                    onClick = {
                        if (currentStep > 0) {
                            currentStep--
                        } else {
                            onNavigateBack()
                        }
                    },
                    isSecondary = true
                )
            }
        }
    }
}
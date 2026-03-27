package com.questua.app.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.*

@Composable
fun PlacementScreen(
    viewModel: PlacementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onFinished: (String) -> Unit
) {
    val selectedLevel by viewModel.selectedLevel.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ExplorerSpeechBubble(
                text = "Bravo! E qual seu domínio atual sobre as artes de ${viewModel.languageName}?",
                mood = if (selectedLevel != null) MascotMood.HAPPY else MascotMood.IDLE
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                itemsIndexed(viewModel.options) { _, option ->
                    val isSelected = selectedLevel == option.cefrLevel
                    val containerColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)

                    Card(
                        onClick = { viewModel.selectLevel(option.cefrLevel, onFinished) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = option.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text(text = option.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            QuestuaButton(
                text = "OUTRO IDIOMA",
                onClick = onNavigateBack,
                isSecondary = true,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
package com.questua.app.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExplorerSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.IDLE,
    displayMascot: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (displayMascot) {
            ExplorerMascotAnimation(mood = mood)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
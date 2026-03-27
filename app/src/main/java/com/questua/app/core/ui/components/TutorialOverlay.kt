package com.questua.app.core.ui.components

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun TutorialSpotlightOverlay(
    targetOffset: Offset,
    targetSize: Size,
    onTargetClick: () -> Unit
) {
    val animatedOffset by animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = tween(500),
        label = "offset"
    )
    val animatedSize by animateSizeAsState(
        targetValue = targetSize,
        animationSpec = tween(500),
        label = "size"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
            .pointerInput(Unit) {
                detectTapGestures { onTargetClick() }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Fundo escurecido
            drawRect(color = Color.Black.copy(alpha = 0.7f))

            // "Buraco" transparente que revela o componente abaixo
            drawRoundRect(
                color = Color.Transparent,
                topLeft = animatedOffset,
                size = animatedSize,
                cornerRadius = CornerRadius(16.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }
    }
}
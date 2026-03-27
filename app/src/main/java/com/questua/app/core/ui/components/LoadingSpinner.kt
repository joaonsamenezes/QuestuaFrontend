package com.questua.app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    transparentBackground: Boolean = false
) {
    val bgColor = if (transparentBackground) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        ExplorerMascotAnimation()
    }
}

@Composable
fun ExplorerMascotAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val shadowWidth = width * 0.5f * shadowScale
            val shadowHeight = height * 0.1f * shadowScale

            drawOval(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset((width - shadowWidth) / 2, height - shadowHeight),
                size = Size(shadowWidth, shadowHeight)
            )

            rotate(degrees = rotation, pivot = Offset(width / 2, height / 2 + bounce)) {
                val mascotY = height / 2 + bounce
                val faceRadius = width * 0.22f

                drawPath(
                    path = Path().apply {
                        moveTo(width / 2 + faceRadius * 0.3f, mascotY + faceRadius * 0.8f)
                        lineTo(width / 2 + faceRadius * 0.9f, mascotY + faceRadius * 1.6f)
                        lineTo(width / 2 + faceRadius * 0.1f, mascotY + faceRadius * 1.3f)
                        close()
                    },
                    color = Color(0xFFB71C1C)
                )

                drawRoundRect(
                    color = Color(0xFFE53935),
                    topLeft = Offset(width / 2 - faceRadius * 0.9f, mascotY + faceRadius * 0.5f),
                    size = Size(faceRadius * 1.8f, faceRadius * 0.8f),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                drawCircle(
                    color = primaryColor,
                    radius = faceRadius,
                    center = Offset(width / 2, mascotY + faceRadius * 0.2f)
                )

                drawCircle(
                    color = Color.Red.copy(alpha = 0.2f),
                    radius = faceRadius * 0.2f,
                    center = Offset(width / 2 - faceRadius * 0.5f, mascotY + faceRadius * 0.4f)
                )

                drawCircle(
                    color = Color.Red.copy(alpha = 0.2f),
                    radius = faceRadius * 0.2f,
                    center = Offset(width / 2 + faceRadius * 0.5f, mascotY + faceRadius * 0.4f)
                )

                val eyeRadius = faceRadius * 0.12f

                drawCircle(
                    color = onSurfaceColor,
                    radius = eyeRadius,
                    center = Offset(width / 2 - faceRadius * 0.35f, mascotY + faceRadius * 0.15f)
                )

                drawCircle(
                    color = onSurfaceColor,
                    radius = eyeRadius,
                    center = Offset(width / 2 + faceRadius * 0.35f, mascotY + faceRadius * 0.15f)
                )

                drawOval(
                    color = tertiaryColor,
                    topLeft = Offset(width / 2 - faceRadius * 1.5f, mascotY - faceRadius * 0.8f),
                    size = Size(faceRadius * 3f, faceRadius * 1.2f)
                )

                drawArc(
                    color = tertiaryColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(width / 2 - faceRadius * 1.05f, mascotY - faceRadius * 1.7f),
                    size = Size(faceRadius * 2.1f, faceRadius * 2.1f),
                    style = Fill
                )

                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(width / 2 - faceRadius * 1.05f, mascotY - faceRadius * 0.85f),
                    size = Size(faceRadius * 2.1f, faceRadius * 0.25f)
                )

                drawRect(
                    color = Color(0xFF37474F),
                    topLeft = Offset(width / 2 - faceRadius * 1.0f, mascotY - faceRadius * 1.2f),
                    size = Size(faceRadius * 2.0f, faceRadius * 0.15f)
                )

                val goggleRadius = faceRadius * 0.35f

                drawCircle(
                    color = Color(0xFF78909C),
                    radius = goggleRadius,
                    center = Offset(width / 2 - faceRadius * 0.4f, mascotY - faceRadius * 1.1f)
                )

                drawCircle(
                    color = Color(0xFFB3E5FC),
                    radius = goggleRadius * 0.7f,
                    center = Offset(width / 2 - faceRadius * 0.4f, mascotY - faceRadius * 1.1f)
                )

                drawCircle(
                    color = Color(0xFF78909C),
                    radius = goggleRadius,
                    center = Offset(width / 2 + faceRadius * 0.4f, mascotY - faceRadius * 1.1f)
                )

                drawCircle(
                    color = Color(0xFFB3E5FC),
                    radius = goggleRadius * 0.7f,
                    center = Offset(width / 2 + faceRadius * 0.4f, mascotY - faceRadius * 1.1f)
                )
            }
        }
    }
}
package com.questua.app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

// Mantemos o enum de humores
enum class MascotMood {
    IDLE, HAPPY, THINKING, SURPRISED
}

@Composable
fun ExplorerMascotAnimation(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.IDLE
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")

    // Animação de pulo baseada no humor
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (mood == MascotMood.HAPPY) -35f else -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mood == MascotMood.HAPPY) 400 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bounce"
    )

    // Cores do Tema
    val primaryColor = MaterialTheme.colorScheme.primary // Rosto
    val tertiaryColor = MaterialTheme.colorScheme.tertiary // Chapéu
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface // Detalhes da boca

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            val mascotY = centerY + 20f + bounce
            val faceRadius = width * 0.18f

            // === 1. Sombra (Estática no chão) ===
            drawOval(
                color = Color.Black.copy(alpha = 0.1f),
                topLeft = Offset(centerX - faceRadius, height - 20f),
                size = Size(faceRadius * 2, 10f)
            )

            // === 2. Grupo de Rotação (Aplica inclinação para THINKING) ===
            rotate(degrees = if (mood == MascotMood.THINKING) -10f else 0f, pivot = Offset(centerX, mascotY)) {

                // === A. Geometria Original do Corpo/Acessórios (Preservada) ===

                // Lenço Vermelho (O "Triângulo" atrás)
                drawPath(
                    path = Path().apply {
                        moveTo(centerX + faceRadius * 0.3f, mascotY + faceRadius * 0.8f)
                        lineTo(centerX + faceRadius * 0.9f, mascotY + faceRadius * 1.4f)
                        lineTo(centerX + faceRadius * 0.1f, mascotY + faceRadius * 1.1f)
                        close()
                    },
                    color = Color(0xFFB71C1C) // Cor exata da referência
                )

                // Lenço Vermelho (A "Faixa" na frente)
                drawRoundRect(
                    color = Color(0xFFE53935), // Cor exata da referência
                    topLeft = Offset(centerX - faceRadius * 0.9f, mascotY + faceRadius * 0.5f),
                    size = Size(faceRadius * 1.8f, faceRadius * 0.8f),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                // Rosto Principal (Círculo Amarelo/Primary)
                drawCircle(
                    color = primaryColor,
                    radius = faceRadius,
                    center = Offset(centerX, mascotY + faceRadius * 0.2f)
                )

                // Chapéu Original (Aba e Topo)
                drawOval(
                    color = tertiaryColor,
                    topLeft = Offset(centerX - faceRadius * 1.5f, mascotY - faceRadius * 0.8f),
                    size = Size(faceRadius * 3f, faceRadius * 1.2f)
                )
                drawArc(
                    color = tertiaryColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(centerX - faceRadius * 1.05f, mascotY - faceRadius * 1.7f),
                    size = Size(faceRadius * 2.1f, faceRadius * 2.1f),
                    style = Fill
                )

                // Faixa do Chapéu Marrom
                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(centerX - faceRadius * 1.05f, mascotY - faceRadius * 0.85f),
                    size = Size(faceRadius * 2.1f, faceRadius * 0.25f)
                )

                // Óculos de Proteção (Goggles) - COMPLETAMENTE PRESERVADOS
                val goggleRadius = faceRadius * 0.35f
                val goggleY = mascotY - faceRadius * 1.1f

                // Armação Cinza
                drawCircle(color = Color(0xFF78909C), radius = goggleRadius, center = Offset(centerX - faceRadius * 0.4f, goggleY))
                drawCircle(color = Color(0xFF78909C), radius = goggleRadius, center = Offset(centerX + faceRadius * 0.4f, goggleY))

                // Vidro Azul
                drawCircle(color = Color(0xFFB3E5FC), radius = goggleRadius * 0.7f, center = Offset(centerX - faceRadius * 0.4f, goggleY))
                drawCircle(color = Color(0xFFB3E5FC), radius = goggleRadius * 0.7f, center = Offset(centerX + faceRadius * 0.4f, goggleY))

                // === B. EXPRESSÕES EXCLUSIVAMENTE NA BOCA (Área Amarela do Rosto) ===

                val mouthY = mascotY + faceRadius * 0.45f // Posicionada abaixo dos óculos
                val mouthWidth = faceRadius * 0.5f

                when (mood) {
                    MascotMood.HAPPY -> {
                        // Boca Sorridente (Arco para baixo)
                        drawArc(
                            color = onSurfaceColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(centerX - mouthWidth / 2, mouthY - mouthWidth / 4),
                            size = Size(mouthWidth, mouthWidth / 2),
                            style = Stroke(width = 4f)
                        )
                    }
                    MascotMood.SURPRISED -> {
                        // Boca Surpresa (Círculo pequeno)
                        drawCircle(
                            color = onSurfaceColor,
                            radius = faceRadius * 0.12f,
                            center = Offset(centerX, mouthY)
                        )
                    }
                    MascotMood.THINKING -> {
                        // Boca de Dúvida (Linha reta)
                        drawRect(
                            color = onSurfaceColor,
                            topLeft = Offset(centerX - mouthWidth / 2, mouthY),
                            size = Size(mouthWidth, 4f)
                        )
                    }
                    MascotMood.IDLE -> {
                        // Boca Padrão (Linha reta mais curta)
                        drawRect(
                            color = onSurfaceColor,
                            topLeft = Offset(centerX - mouthWidth / 4, mouthY),
                            size = Size(mouthWidth / 2, 4f)
                        )
                    }
                }
            }
        }
    }
}
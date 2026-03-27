package com.questua.app.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.questua.app.R
import com.questua.app.core.ui.components.QuestuaButton

@Composable
fun InitialScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
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
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                MascoteComLogoFlutuante()
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "QUESTUA",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )

            Text(
                text = "Aprenda idiomas explorando o mundo real.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            QuestuaButton(
                text = "COMEÇAR AVENTURA",
                onClick = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuestuaButton(
                text = "JÁ SOU UM AVENTUREIRO",
                onClick = onNavigateToLogin,
                isSecondary = true
            )
        }
    }
}

@Composable
fun MascoteComLogoFlutuante() {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val logoRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val logoPainter = painterResource(id = R.drawable.ic_questua_logo)

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2

            val mascotY = centerY + 20f + (floatAnim * 0.5f)
            val faceRadius = canvasWidth * 0.18f

            drawOval(
                color = Color.Black.copy(alpha = 0.1f),
                topLeft = Offset(centerX - (faceRadius), canvasHeight - 20f),
                size = Size(faceRadius * 2, 10f)
            )

            drawPath(
                path = Path().apply {
                    moveTo(centerX + faceRadius * 0.3f, mascotY + faceRadius * 0.8f)
                    lineTo(centerX + faceRadius * 1.2f, mascotY - faceRadius * 0.5f)
                    lineTo(centerX + faceRadius * 0.9f, mascotY + faceRadius * 0.2f)
                    close()
                },
                color = Color(0xFFB71C1C)
            )

            drawRoundRect(
                color = Color(0xFFE53935),
                topLeft = Offset(centerX - faceRadius * 0.9f, mascotY + faceRadius * 0.5f),
                size = Size(faceRadius * 1.8f, faceRadius * 0.8f),
                cornerRadius = CornerRadius(16f, 16f)
            )

            drawCircle(
                color = primaryColor,
                radius = faceRadius,
                center = Offset(centerX, mascotY + faceRadius * 0.2f)
            )

            val eyeRadius = faceRadius * 0.12f
            drawCircle(
                color = onSurfaceColor,
                radius = eyeRadius,
                center = Offset(centerX - faceRadius * 0.35f, mascotY + faceRadius * 0.15f)
            )
            drawCircle(
                color = onSurfaceColor,
                radius = eyeRadius,
                center = Offset(centerX + faceRadius * 0.35f, mascotY + faceRadius * 0.15f)
            )

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

            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(centerX - faceRadius * 1.05f, mascotY - faceRadius * 0.85f),
                size = Size(faceRadius * 2.1f, faceRadius * 0.25f)
            )

            val goggleRadius = faceRadius * 0.35f
            drawCircle(
                color = Color(0xFF78909C),
                radius = goggleRadius,
                center = Offset(centerX - faceRadius * 0.4f, mascotY - faceRadius * 1.1f)
            )
            drawCircle(
                color = Color(0xFF78909C),
                radius = goggleRadius,
                center = Offset(centerX + faceRadius * 0.4f, mascotY - faceRadius * 1.1f)
            )
        }

        Box(
            modifier = Modifier
                .offset(y = (-80).dp + floatAnim.dp, x = 40.dp)
                .size(70.dp)
        ) {
            Icon(
                painter = logoPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = Color.Unspecified
            )
        }
    }
}
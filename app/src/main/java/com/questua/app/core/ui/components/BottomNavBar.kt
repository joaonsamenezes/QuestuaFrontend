package com.questua.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.questua.app.core.common.capturePosition

enum class HubTab(val icon: ImageVector, val label: String) {
    HOME(Icons.Default.Home, "Home"),
    MAP(Icons.Default.Map, "Mapa"),
    PROGRESS(Icons.Default.EmojiEvents, "Progresso"),
    PROFILE(Icons.Default.Person, "Perfil")
}

@Composable
fun BottomNavBar(
    selectedTab: HubTab,
    onTabSelected: (HubTab) -> Unit,
    hasNotificationsOnProgress: Boolean = false,
    modifier: Modifier = Modifier,
    onMapTabPositioned: ((Offset, Size) -> Unit)? = null,
    onProgressTabPositioned: ((Offset, Size) -> Unit)? = null,
    onProfileTabPositioned: ((Offset, Size) -> Unit)? = null
) {
    val containerColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .shadow(elevation = 15.dp, spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        color = containerColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val interactionSource = remember { MutableInteractionSource() }

                val itemModifier = when(tab) {
                    HubTab.MAP -> Modifier.capturePosition { offset, size -> onMapTabPositioned?.invoke(offset, size) }
                    HubTab.PROGRESS -> Modifier.capturePosition { offset, size -> onProgressTabPositioned?.invoke(offset, size) }
                    HubTab.PROFILE -> Modifier.capturePosition { offset, size -> onProfileTabPositioned?.invoke(offset, size) }
                    else -> Modifier
                }

                val offsetY by animateDpAsState(targetValue = if (isSelected) (-8).dp else 0.dp, label = "offset")
                val iconColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "iconColor")
                val bgColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "bgColor")
                val labelColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "labelColor")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .then(itemModifier)
                        .offset(y = offsetY)
                        .clickable(interactionSource = interactionSource, indication = null) { onTabSelected(tab) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                if (tab == HubTab.PROGRESS && hasNotificationsOnProgress && !isSelected) {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                            }
                            .background(bgColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = labelColor)
                    )
                }
            }
        }
    }
}
package com.questua.app.core.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize

fun Modifier.capturePosition(
    onPositionCaptured: (Offset, Size) -> Unit
): Modifier = this.onGloballyPositioned { coordinates ->
    onPositionCaptured(
        coordinates.positionInRoot(),
        coordinates.size.toSize()
    )
}
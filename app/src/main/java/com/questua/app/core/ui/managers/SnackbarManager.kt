package com.questua.app.core.ui.managers

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SnackbarMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isError: Boolean = false,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

object SnackbarManager {
    private val _messages = MutableStateFlow<List<SnackbarMessage>>(emptyList())
    val messages: StateFlow<List<SnackbarMessage>> = _messages.asStateFlow()

    fun showMessage(message: String, isError: Boolean = false, duration: SnackbarDuration = SnackbarDuration.Short) {
        _messages.update { currentMessages ->
            currentMessages + SnackbarMessage(message = message, isError = isError, duration = duration)
        }
    }

    fun showError(message: String, duration: SnackbarDuration = SnackbarDuration.Long) {
        showMessage(message, isError = true, duration = duration)
    }

    fun showSuccess(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        showMessage(message, isError = false, duration = duration)
    }

    fun dismissMessage(id: String) {
        _messages.update { currentMessages ->
            currentMessages.filterNot { it.id == id }
        }
    }
}
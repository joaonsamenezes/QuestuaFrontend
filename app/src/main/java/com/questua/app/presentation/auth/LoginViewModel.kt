package com.questua.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.ui.managers.SnackbarManager
import com.questua.app.domain.usecase.auth.LoginUseCase
import com.questua.app.domain.usecase.auth.SyncGoogleUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val recoveryEmailSent: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val syncGoogleUserUseCase: SyncGoogleUserUseCase,
    val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val recoveryEmail = MutableStateFlow("")

    private fun validateForm(): Boolean {
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        if (email.value.isBlank()) {
            emailError = "O e-mail não pode estar vazio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            emailError = "Formato de e-mail inválido"
            isValid = false
        }

        if (password.value.isBlank()) {
            passwordError = "A senha não pode estar vazia"
            isValid = false
        } else if (password.value.length < 6) {
            passwordError = "A senha deve ter pelo menos 6 caracteres"
            isValid = false
        }

        _state.value = _state.value.copy(emailError = emailError, passwordError = passwordError)
        return isValid
    }

    fun login() {
        if (!validateForm()) return

        viewModelScope.launch {
            loginUseCase(email.value, password.value).onEach { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                    is Resource.Success -> {
                        SnackbarManager.showSuccess("Login realizado com sucesso!")
                        _state.value = LoginState(isLoggedIn = true)
                    }
                    is Resource.Error -> {
                        SnackbarManager.showError(result.message ?: "Falha no login")
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }.launchIn(this)
        }
    }

    fun handleGoogleSuccess() {
        viewModelScope.launch {
            val sessionUser = supabaseClient.auth.currentUserOrNull() ?: return@launch
            val userEmail = sessionUser.email ?: return@launch
            val userName = sessionUser.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "Usuário"
            val userAvatar = sessionUser.userMetadata?.get("avatar_url")?.jsonPrimitive?.content

            syncGoogleUserUseCase(userEmail, userName, userAvatar, null, null).onEach { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                    is Resource.Success -> {
                        SnackbarManager.showSuccess("Login com Google realizado com sucesso!")
                        _state.value = LoginState(isLoggedIn = true)
                    }
                    is Resource.Error -> {
                        SnackbarManager.showError(result.message ?: "Falha no login com Google")
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }.launchIn(this)
        }
    }

    fun handleGoogleError(message: String) {
        SnackbarManager.showError(message)
        _state.value = _state.value.copy(error = message)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun sendPasswordRecovery() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                supabaseClient.auth.resetPasswordForEmail(email = recoveryEmail.value)
                SnackbarManager.showSuccess("E-mail de recuperação enviado para ${recoveryEmail.value}")
                _state.value = _state.value.copy(isLoading = false, recoveryEmailSent = true)
            } catch (e: Exception) {
                SnackbarManager.showError("Falha ao enviar e-mail: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun dismissRecoverySuccess() {
        _state.value = _state.value.copy(recoveryEmailSent = false)
    }
}
package com.questua.app.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.ui.managers.SnackbarManager
import com.questua.app.domain.usecase.auth.RegisterInitUseCase
import com.questua.app.domain.usecase.auth.RegisterVerifyUseCase
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

data class RegisterState(
    val isLoading: Boolean = false,
    val isInitSuccess: Boolean = false,
    val isRegistered: Boolean = false,
    val error: String? = null,
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val codeError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerInitUseCase: RegisterInitUseCase,
    private val registerVerifyUseCase: RegisterVerifyUseCase,
    private val syncGoogleUserUseCase: SyncGoogleUserUseCase,
    val supabaseClient: SupabaseClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    val displayName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val verificationCode = MutableStateFlow("")

    private val languageId: String = savedStateHandle["languageId"] ?: ""
    private val cefrLevel: String = savedStateHandle["cefrLevel"] ?: "A1"

    private fun validateForm(): Boolean {
        var isValid = true
        var dError: String? = null
        var eError: String? = null
        var pError: String? = null

        val cleanName = displayName.value.trim()
        val cleanEmail = email.value.trim()

        if (cleanName.isBlank()) {
            dError = "O nome não pode estar vazio"
            isValid = false
        } else if (cleanName.length < 3) {
            dError = "O nome deve ter pelo menos 3 caracteres"
            isValid = false
        }

        if (cleanEmail.isBlank()) {
            eError = "O e-mail não pode estar vazio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            eError = "Formato de e-mail inválido"
            isValid = false
        }

        if (password.value.isBlank()) {
            pError = "A senha não pode estar vazia"
            isValid = false
        } else if (password.value.length < 6) {
            pError = "A senha deve ter pelo menos 6 caracteres"
            isValid = false
        }

        _state.value = _state.value.copy(
            displayNameError = dError,
            emailError = eError,
            passwordError = pError
        )
        return isValid
    }

    fun onEmailChange(newValue: String) {
        email.value = newValue
        if (_state.value.emailError != null) {
            _state.value = _state.value.copy(emailError = null)
        }
    }

    fun onCodeChange(newValue: String) {
        verificationCode.value = newValue
        if (_state.value.codeError != null) {
            _state.value = _state.value.copy(codeError = null)
        }
    }

    fun registerInit() {
        if (!validateForm()) return

        registerInitUseCase(email.value.trim(), displayName.value.trim(), password.value, languageId, cefrLevel).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null, isInitSuccess = false)
                is Resource.Success -> {
                    SnackbarManager.showSuccess("Código enviado para o seu e-mail!")
                    _state.value = _state.value.copy(isLoading = false, isInitSuccess = true)
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Falha ao registrar"
                    _state.value = _state.value.copy(isLoading = false, error = msg)
                    SnackbarManager.showError(msg)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun registerVerify() {
        val code = verificationCode.value.trim()
        if (code.length != 6) {
            _state.value = _state.value.copy(codeError = "O código deve ter 6 dígitos")
            return
        }

        registerVerifyUseCase(email.value.trim(), code).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                is Resource.Success -> {
                    SnackbarManager.showSuccess("Conta ativada!")
                    _state.value = RegisterState(isRegistered = true)
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Código inválido ou expirado"
                    SnackbarManager.showError(msg)
                    _state.value = _state.value.copy(isLoading = false, codeError = msg)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetInitState() {
        _state.value = _state.value.copy(isInitSuccess = false)
    }

    fun handleGoogleSuccess() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val sessionUser = supabaseClient.auth.currentUserOrNull() ?: return@launch
            val userEmail = sessionUser.email ?: ""
            val userName = sessionUser.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "Aventureiro"
            val userAvatar = sessionUser.userMetadata?.get("avatar_url")?.jsonPrimitive?.content

            syncGoogleUserUseCase(userEmail, userName, userAvatar, languageId, cefrLevel).onEach { result ->
                if (result is Resource.Success) _state.value = RegisterState(isRegistered = true)
                else if (result is Resource.Error) _state.value = _state.value.copy(isLoading = false)
            }.launchIn(this)
        }
    }

    fun handleGoogleError(message: String) {
        SnackbarManager.showError(message)
    }
}
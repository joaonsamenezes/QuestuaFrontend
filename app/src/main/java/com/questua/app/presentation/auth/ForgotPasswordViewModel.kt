package com.questua.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.usecase.auth.ForgotPasswordUseCase
import com.questua.app.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false,
    val isPasswordReset: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    val email = MutableStateFlow("")
    val code = MutableStateFlow("")
    val newPassword = MutableStateFlow("")

    fun onSendCode() {
        forgotPasswordUseCase(email.value).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                is Resource.Success -> _state.value = _state.value.copy(isLoading = false, isCodeSent = true)
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun onResetPassword() {
        resetPasswordUseCase(code.value, newPassword.value).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                is Resource.Success -> _state.value = _state.value.copy(isLoading = false, isPasswordReset = true)
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }
}
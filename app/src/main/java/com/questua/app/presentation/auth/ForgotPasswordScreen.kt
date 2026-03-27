package com.questua.app.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val code by viewModel.code.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()

    LaunchedEffect(state.isPasswordReset) {
        if (state.isPasswordReset) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                IconButton(
                    onClick = if (state.isCodeSent) { { viewModel.onSendCode() } } else onNavigateBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (!state.isCodeSent) "Esqueci a Senha" else "Nova Senha",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (!state.isCodeSent)
                        "Insira seu e-mail para receber um código de recuperação."
                    else "Insira o código enviado ao seu e-mail e sua nova senha de acesso.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (!state.isCodeSent) {
                    QuestuaTextField(
                        value = email,
                        onValueChange = { viewModel.email.value = it },
                        label = "E-mail de cadastro",
                        placeholder = "aventureiro@questua.com",
                        leadingIcon = Icons.Default.Email
                    )

                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    QuestuaButton(
                        text = "Enviar Código",
                        onClick = { viewModel.onSendCode() },
                        isLoading = state.isLoading,
                        enabled = email.isNotBlank()
                    )
                } else {
                    QuestuaTextField(
                        value = code,
                        onValueChange = { viewModel.code.value = it },
                        label = "Código de Recuperação",
                        placeholder = "123456",
                        leadingIcon = Icons.Default.VpnKey
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    QuestuaTextField(
                        value = newPassword,
                        onValueChange = { viewModel.newPassword.value = it },
                        label = "Nova Senha",
                        placeholder = "••••••••",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock
                    )

                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    QuestuaButton(
                        text = "Redefinir Senha",
                        onClick = { viewModel.onResetPassword() },
                        isLoading = state.isLoading,
                        enabled = code.isNotBlank() && newPassword.length >= 6
                    )
                }
            }
        }
    }
}
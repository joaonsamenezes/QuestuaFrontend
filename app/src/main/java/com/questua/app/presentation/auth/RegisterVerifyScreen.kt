package com.questua.app.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField

@Composable
fun RegisterVerifyScreen(
    onNavigateToHome: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onNavigateToHome(true)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ative sua conta",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Insira o código de 6 dígitos enviado para $email",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                QuestuaTextField(
                    value = verificationCode,
                    onValueChange = { if (it.length <= 6) viewModel.onCodeChange(it) },
                    label = "Código de Verificação",
                    placeholder = "000000",
                    leadingIcon = Icons.Default.VpnKey,
                    errorMessage = state.codeError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )

                Spacer(modifier = Modifier.height(32.dp))

                QuestuaButton(
                    text = "Verificar e Entrar",
                    onClick = { viewModel.registerVerify() },
                    isLoading = state.isLoading,
                    enabled = verificationCode.length == 6
                )
            }
        }
    }
}
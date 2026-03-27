package com.questua.app.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaGoogleButton
import com.questua.app.core.ui.components.QuestuaTextField
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth

@Composable
fun RegisterScreen(
    onNavigateToVerify: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToTerms: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var termsAccepted by remember { mutableStateOf(false) }

    val googleLogin = viewModel.supabaseClient.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> viewModel.handleGoogleSuccess()
                is NativeSignInResult.Error -> viewModel.handleGoogleError(result.message)
                else -> {}
            }
        }
    )

    LaunchedEffect(state.isInitSuccess) {
        if (state.isInitSuccess) {
            viewModel.resetInitState()
            onNavigateToVerify(viewModel.email.value)
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
                IconButton(onClick = onNavigateBack, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Criar Conta",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                QuestuaTextField(
                    value = displayName,
                    onValueChange = { viewModel.displayName.value = it },
                    label = "Nome de aventureiro",
                    placeholder = "Seu nome heróico",
                    leadingIcon = Icons.Default.Person,
                    errorMessage = state.displayNameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestuaTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = "E-mail",
                    placeholder = "aventureiro@questua.com",
                    leadingIcon = Icons.Default.Email,
                    errorMessage = state.emailError
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestuaTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    label = "Senha",
                    placeholder = "••••••••",
                    isPassword = true,
                    leadingIcon = Icons.Default.Lock,
                    errorMessage = state.passwordError
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = "Eu aceito os ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToTerms, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            text = "Termos e Condições",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                QuestuaButton(
                    text = "Registrar-se",
                    onClick = { viewModel.registerInit() },
                    isLoading = state.isLoading,
                    enabled = termsAccepted
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestuaGoogleButton(
                    text = "Registrar-se com Google",
                    onClick = {
                        if (termsAccepted) {
                            googleLogin.startFlow()
                        } else {
                            viewModel.handleGoogleError("Você precisa aceitar os termos para continuar.")
                        }
                    }
                )
            }
        }
    }
}
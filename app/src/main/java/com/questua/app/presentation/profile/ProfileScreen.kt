package com.questua.app.presentation.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.common.uriToFile
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.core.ui.components.SuccessDialog
import com.questua.app.domain.enums.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToFeedback: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    navController: NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.editName.collectAsState()
    val email by viewModel.editEmail.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val selectedAvatarUri by viewModel.selectedAvatarUri.collectAsState()

    val context = LocalContext.current

    val navBackStackEntry = navController?.currentBackStackEntryAsState()?.value
    LaunchedEffect(navBackStackEntry) {
        val message = navBackStackEntry?.savedStateHandle?.get<String>("feedback_message")
        if (message != null) {
            viewModel.setSuccessMessage(message)
            navBackStackEntry.savedStateHandle.remove<String>("feedback_message")
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val file = context.uriToFile(it)
            file?.let { f ->
                viewModel.onImageSelected(f, it.toString())
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Meu Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.user == null) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(contentAlignment = Alignment.Center) {
                    val avatarModel = selectedAvatarUri ?: state.user?.avatarUrl?.toFullImageUrl()

                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    )

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarModel ?: "https://via.placeholder.com/150")
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = state.isEditing) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentScale = ContentScale.Crop
                    )

                    if (state.isEditing) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Alterar",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (state.isEditing) {
                        QuestuaTextField(
                            value = name,
                            onValueChange = { newName -> viewModel.editName.value = newName },
                            label = "Nome",
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        QuestuaTextField(
                            value = email,
                            onValueChange = { },
                            label = "E-mail (Fixo)",
                            leadingIcon = Icons.Default.Email,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        QuestuaTextField(
                            value = newPassword,
                            onValueChange = { newPass -> viewModel.newPassword.value = newPass },
                            label = "Nova Senha",
                            placeholder = "Deixe vazio para manter",
                            isPassword = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.user?.displayName ?: "Explorador",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = state.user?.email ?: "carregando...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    QuestuaButton(
                        text = if (state.isEditing) "SALVAR ALTERAÇÕES" else "EDITAR PERFIL",
                        onClick = { viewModel.toggleEditMode() },
                        isSecondary = !state.isEditing,
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!state.isEditing) {
                        Spacer(modifier = Modifier.height(32.dp))

                        SettingsGroup(title = "Preferências") {
                            SettingsItemSwitch(
                                label = "Notificações",
                                icon = Icons.Outlined.Notifications,
                                checked = state.notificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotifications(it) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            SettingsItemSwitch(
                                label = "Tema Escuro",
                                icon = Icons.Outlined.DarkMode,
                                checked = state.darkThemeEnabled,
                                onCheckedChange = { viewModel.toggleTheme(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        SettingsGroup(title = "Conta e Suporte") {
                            SettingsActionItem(
                                label = "Enviar Sugestão",
                                icon = Icons.Default.RateReview,
                                onClick = onNavigateToFeedback
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            SettingsActionItem(
                                label = "Ajuda e Suporte",
                                icon = Icons.Outlined.Help,
                                onClick = onNavigateToHelp
                            )

                            if (state.user?.role == UserRole.ADMIN) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                SettingsActionItem(
                                    label = "Painel do Administrador",
                                    icon = Icons.Default.AdminPanelSettings,
                                    onClick = onNavigateToAdmin,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    textColor = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            SettingsActionItem(
                                label = "Sair",
                                icon = Icons.Outlined.Logout,
                                onClick = { viewModel.logout(onLogoutSuccess = onNavigateToLogin) },
                                iconTint = MaterialTheme.colorScheme.error,
                                textColor = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }

            state.error?.let {
                ErrorDialog(message = it, onDismiss = { viewModel.clearError() })
            }

            state.successMessage?.let { message ->
                SuccessDialog(
                    message = message,
                    onDismiss = { viewModel.clearSuccessMessage() }
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItemSwitch(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
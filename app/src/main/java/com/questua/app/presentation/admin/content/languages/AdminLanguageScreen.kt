package com.questua.app.presentation.admin.content.languages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.common.uriToFile
import com.questua.app.core.ui.components.*
import com.questua.app.domain.model.Language
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLanguageScreen(
    navController: NavController,
    viewModel: AdminLanguageViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFormDialog by remember { mutableStateOf<Language?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var languageToDelete by remember { mutableStateOf<Language?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchLanguages()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        LanguageFormDialog(
            language = showFormDialog,
            onDismiss = { isCreating = false; showFormDialog = null },
            onConfirm = { name, code, imageFile ->
                viewModel.saveLanguage(showFormDialog?.id, name, code, imageFile)
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (languageToDelete != null) {
        AlertDialog(
            onDismissRequest = { languageToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Excluir Idioma", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir '${languageToDelete?.name}'? Esta ação pode impactar conteúdos vinculados.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLanguage(languageToDelete!!.id)
                        languageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("EXCLUIR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { languageToDelete = null }) {
                    Text("CANCELAR")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Gerenciar Idiomas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreating = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Idioma")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar idioma...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    LoadingSpinner(modifier = Modifier.align(Alignment.Center))
                } else if (state.languages.isEmpty()) {
                    EmptyLanguagesState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 0.dp,
                            end = 16.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.languages) { lang ->
                            LanguageCardItem(
                                language = lang,
                                onEdit = { showFormDialog = lang },
                                onDelete = { languageToDelete = lang }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageCardItem(
    language: Language,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (language.iconUrl != null) {
                    AsyncImage(
                        model = language.iconUrl.toFullImageUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Código: ${language.code.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun LanguageFormDialog(
    language: Language?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, File?) -> Unit
) {
    var name by remember { mutableStateOf(language?.name ?: "") }
    var code by remember { mutableStateOf(language?.code ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> selectedImageUri = uri }

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) { nameError = "Obrigatório"; isValid = false } else nameError = null
        if (code.isBlank()) { codeError = "Obrigatório"; isValid = false } else codeError = null
        return isValid
    }

    QuestuaBaseFormDialog(
        title = if (language == null) "Novo Idioma" else "Editar Idioma",
        onDismiss = onDismiss,
        onConfirm = {
            if (validate()) {
                val file = selectedImageUri?.let { context.uriToFile(it) }
                onConfirm(name, code, file)
            }
        },
        confirmEnabled = name.isNotBlank() && code.isNotBlank()
    ) {
        FormSectionTitle("Identidade Visual")

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentAlignment = Alignment.Center
        ) {
            val model = selectedImageUri ?: language?.iconUrl?.toFullImageUrl()

            if (model != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(model).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        QuestuaTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = "Nome do Idioma",
            modifier = Modifier.fillMaxWidth(),
            errorMessage = nameError
        )

        QuestuaTextField(
            value = code,
            onValueChange = { code = it; codeError = null },
            label = "Código (ex: en-US)",
            modifier = Modifier.fillMaxWidth(),
            errorMessage = codeError
        )
    }
}

@Composable
fun EmptyLanguagesState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Translate,
            null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nenhum idioma encontrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
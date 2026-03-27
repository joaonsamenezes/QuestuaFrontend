package com.questua.app.presentation.admin.content.ai

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.City
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiContentGenerationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: AiContentGenerationViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AiContentGenerationViewModel.NavigationEvent.Success -> {
                    Toast.makeText(context, "Conteúdo gerado com sucesso!", Toast.LENGTH_SHORT).show()
                    onNavigateToDetail(event.route)
                }
                is AiContentGenerationViewModel.NavigationEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Gerar com IA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Assistente Criativo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Selecione o tipo de conteúdo para gerar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tipo de Conteúdo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.selectedType.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    AiContentType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = type.name,
                                    fontWeight = if(type == state.selectedType) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            onClick = {
                                viewModel.onTypeSelected(type)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PARÂMETROS DE GERAÇÃO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )

                DynamicFormFields(
                    type = state.selectedType,
                    fields = state.fields,
                    cities = state.cities,
                    questPoints = state.questPoints,
                    quests = state.quests,
                    characters = state.characters,
                    onUpdate = { field, value -> viewModel.onFieldUpdate(field, value) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            QuestuaButton(
                text = "GERAR CONTEÚDO",
                onClick = { viewModel.generate() },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DynamicFormFields(
    type: AiContentType,
    fields: Map<String, String>,
    cities: List<City>,
    questPoints: List<QuestPoint>,
    quests: List<Quest>,
    characters: List<CharacterEntity>,
    onUpdate: (String, String) -> Unit
) {
    var showCitySelector by remember { mutableStateOf(false) }
    var showQuestPointSelector by remember { mutableStateOf(false) }
    var showQuestSelector by remember { mutableStateOf(false) }
    var showCharacterSelector by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when (type) {
            AiContentType.QUEST_POINT -> {
                SelectorField(
                    label = "Cidade",
                    value = cities.find { it.id == fields["cityId"] }?.name ?: "Selecionar Cidade",
                    onClick = { showCitySelector = true },
                    isSelected = fields["cityId"] != null
                )

                if (showCitySelector) {
                    SelectorDialog(
                        title = "Selecione a Cidade",
                        items = cities,
                        itemContent = { Text(it.name, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("cityId", it.id); showCitySelector = false },
                        onDismiss = { showCitySelector = false }
                    )
                }

                QuestuaTextField(
                    value = fields["theme"] ?: "",
                    onValueChange = { onUpdate("theme", it) },
                    label = "Tema (Ex: Histórico, Moderno)"
                )
            }
            AiContentType.QUEST -> {
                SelectorField(
                    label = "Quest Point",
                    value = questPoints.find { it.id == fields["questPointId"] }?.title ?: "Selecionar Quest Point",
                    onClick = { showQuestPointSelector = true },
                    isSelected = fields["questPointId"] != null
                )

                if (showQuestPointSelector) {
                    SelectorDialog(
                        title = "Selecione o Quest Point",
                        items = questPoints,
                        itemContent = { Text(it.title, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("questPointId", it.id); showQuestPointSelector = false },
                        onDismiss = { showQuestPointSelector = false }
                    )
                }

                QuestuaTextField(
                    value = fields["context"] ?: "",
                    onValueChange = { onUpdate("context", it) },
                    label = "Contexto da Missão"
                )

                Column {
                    Text(
                        text = "Dificuldade (${fields["difficulty"] ?: "1"})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = (fields["difficulty"]?.toFloatOrNull() ?: 1f),
                        onValueChange = { onUpdate("difficulty", it.toInt().toString()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
            AiContentType.CHARACTER -> {
                QuestuaTextField(
                    value = fields["archetype"] ?: "",
                    onValueChange = { onUpdate("archetype", it) },
                    label = "Arquétipo (Ex: Guia Turístico)"
                )
            }
            AiContentType.SCENE_DIALOGUE -> {
                SelectorField(
                    label = "Personagem",
                    value = characters.find { it.id == fields["speakerId"] }?.name ?: "Selecionar Personagem",
                    onClick = { showCharacterSelector = true },
                    isSelected = fields["speakerId"] != null
                )

                if (showCharacterSelector) {
                    SelectorDialog(
                        title = "Selecione o Personagem",
                        items = characters,
                        itemContent = { Text(it.name, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("speakerId", it.id); showCharacterSelector = false },
                        onDismiss = { showCharacterSelector = false }
                    )
                }

                SelectorField(
                    label = "Quest (Opcional)",
                    value = quests.find { it.id == fields["questId"] }?.title ?: "Vincular a uma Quest",
                    onClick = { showQuestSelector = true },
                    isSelected = fields["questId"] != null
                )

                if (showQuestSelector) {
                    SelectorDialog(
                        title = "Selecione a Quest",
                        items = quests,
                        itemContent = { Text(it.title, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("questId", it.id); showQuestSelector = false },
                        onDismiss = { showQuestSelector = false },
                        canClear = true,
                        onClear = { onUpdate("questId", ""); showQuestSelector = false }
                    )
                }

                QuestuaTextField(
                    value = fields["context"] ?: "",
                    onValueChange = { onUpdate("context", it) },
                    label = "Contexto do Diálogo"
                )
            }
            AiContentType.ACHIEVEMENT -> {
                QuestuaTextField(
                    value = fields["trigger"] ?: "",
                    onValueChange = { onUpdate("trigger", it) },
                    label = "Ação de Gatilho (Ex: Completar 5 quests)"
                )
                QuestuaTextField(
                    value = fields["difficulty"] ?: "EASY",
                    onValueChange = { onUpdate("difficulty", it) },
                    label = "Dificuldade (EASY, MEDIUM, HARD)"
                )
            }
        }
    }
}

@Composable
fun SelectorField(
    label: String,
    value: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium
                )
            },
            overlineContent = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun <T> SelectorDialog(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    canClear: Boolean = false,
    onClear: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (canClear) {
                        item {
                            ListItem(
                                modifier = Modifier.clickable { onClear() }.clip(RoundedCornerShape(12.dp)),
                                headlineContent = { Text("Nenhum (Remover Seleção)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                            )
                        }
                    }
                    items(items) { item ->
                        ListItem(
                            modifier = Modifier.clickable { onSelect(item) }.clip(RoundedCornerShape(12.dp)),
                            headlineContent = { itemContent(item) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("FECHAR") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
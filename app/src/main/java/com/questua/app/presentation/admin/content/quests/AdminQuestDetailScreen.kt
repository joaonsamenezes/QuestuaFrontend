package com.questua.app.presentation.admin.content.quests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.LoadingSpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestDetailScreen(
    navController: NavController,
    viewModel: AdminQuestDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.quest?.title ?: "Detalhes da Quest",
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
        bottomBar = {
            if (state.quest != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDelete = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EXCLUIR", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showEdit = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EDITAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else if (state.quest != null) {
                val qpName = state.questPoints.find { it.id == state.quest.questPointId }?.title ?: state.quest.questPointId
                val dialogueName = state.dialogues.find { it.id == state.quest.firstDialogueId }?.textContent?.take(30)?.plus("...") ?: "Nenhum"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailCard(
                        title = "FLUXO NARRATIVO",
                        items = listOf(
                            "Título" to state.quest.title,
                            "Quest Point" to qpName,
                            "Cena Inicial" to dialogueName,
                            "Ordem de Exibição" to state.quest.orderIndex.toString()
                        )
                    )

                    DetailCard(
                        title = "CONFIGURAÇÕES TÉCNICAS",
                        items = listOf(
                            "Dificuldade" to "${state.quest.difficulty}/5",
                            "XP de Conclusão" to "${state.quest.xpValue} XP",
                            "XP por Acerto" to "${state.quest.xpPerQuestion} XP",
                            "Criado em" to state.quest.createdAt.take(10)
                        )
                    )

                    if (state.quest.description.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    "DESCRIÇÃO DA MISSÃO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    state.quest.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    state.quest.unlockRequirement?.let { unlock ->
                        DetailCard(
                            title = "REQUISITOS DE DESBLOQUEIO",
                            items = listOf(
                                "Acesso Premium" to if (unlock.premiumAccess) "Sim" else "Livre",
                                "Nível Necessário" to (unlock.requiredGamificationLevel?.toString() ?: "1"),
                                "Proficiência CEFR" to (unlock.requiredCefrLevel ?: "Livre")
                            )
                        )
                    }

                    state.quest.learningFocus?.let { focus ->
                        DetailCard(
                            title = "FOCO PEDAGÓGICO",
                            items = listOf(
                                "Gramática" to (focus.grammarTopics?.joinToString(", ") ?: "-"),
                                "Vocabulário" to (focus.vocabularyThemes?.joinToString(", ") ?: "-"),
                                "Habilidades" to (focus.skills?.joinToString(", ") ?: "-")
                            )
                        )
                    }

                    DetailCard(
                        title = "STATUS DE VISIBILIDADE",
                        items = listOf(
                            "Publicado" to if (state.quest.isPublished) "Público" else "Rascunho",
                            "Tipo de Acesso" to if (state.quest.isPremium) "Premium" else "Gratuito",
                            "Conteúdo AI" to if (state.quest.isAiGenerated) "Gerado por IA" else "Manual"
                        )
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }

        if (showEdit && state.quest != null) {
            QuestFormDialog(
                quest = state.quest,
                questPoints = state.questPoints,
                dialogues = state.dialogues,
                onDismiss = { showEdit = false },
                onConfirm = { title, qpId, dial, desc, diff, ord, xpValue, xpPerQuestion, unl, foc, prem, ai, pub ->
                    viewModel.saveQuest(qpId, dial, title, desc, diff, ord, xpValue, xpPerQuestion, unl, foc, prem, ai, pub)
                    showEdit = false
                }
            )
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Quest", fontWeight = FontWeight.Bold) },
                text = { Text("Tem certeza que deseja excluir esta missão? Todas as tentativas dos usuários e registros vinculados serão perdidos.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteQuest(); showDelete = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EXCLUIR", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDelete = false }) {
                        Text("CANCELAR")
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

@Composable
private fun DetailCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            items.forEachIndexed { index, (label, value) ->
                Column(modifier = Modifier.padding(bottom = if (index == items.lastIndex) 0.dp else 16.dp)) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
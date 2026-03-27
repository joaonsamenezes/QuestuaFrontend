package com.questua.app.presentation.admin.content.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.domain.enums.RarityType
import com.questua.app.presentation.admin.content.cities.DetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAchievementDetailScreen(
    navController: NavController,
    viewModel: AdminAchievementDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.achievement != null) {
        AchievementFormDialog(
            achievement = state.achievement,
            cities = state.cities,
            quests = state.quests,
            questPoints = state.questPoints,
            onDismiss = { showEdit = false },
            onConfirm = { key, name, desc, icon, rar, xp, hidden, global, cat, cond, targ, req ->
                viewModel.saveAchievement(key, name, desc, icon, rar, xp, hidden, global, cat, cond, targ, req)
                showEdit = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.achievement?.name ?: "Detalhes",
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
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (state.achievement != null) {
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
        if (state.achievement != null) {
            val rarityColor = when (state.achievement.rarity) {
                RarityType.LEGENDARY -> Color(0xFFFFD700)
                RarityType.EPIC -> Color(0xFF9C27B0)
                RarityType.RARE -> Color(0xFF2196F3)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val targetName = remember(state.achievement.targetId, state.cities, state.quests, state.questPoints) {
                val id = state.achievement.targetId
                if (id.isNullOrBlank()) "Global / Nenhum"
                else {
                    state.cities.find { it.id == id }?.name
                        ?: state.quests.find { it.id == id }?.title
                        ?: state.questPoints.find { it.id == id }?.title
                        ?: id
                }
            }

            val languageName = remember(state.achievement.languageId, state.languages) {
                state.languages.find { it.id == state.achievement.languageId }?.name ?: "Todos"
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(32.dp))
                        .background(rarityColor.copy(alpha = 0.1f))
                        .border(3.dp, rarityColor.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.achievement.iconUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.achievement.iconUrl.toFullImageUrl(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(20.dp).fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = rarityColor,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailCard("INFORMAÇÕES PRINCIPAIS", listOf(
                        "Nome" to state.achievement.name,
                        "Chave (ID)" to (state.achievement.keyName ?: "N/A"),
                        "Descrição" to state.achievement.description,
                        "Raridade" to state.achievement.rarity.name,
                        "XP Reward" to "${state.achievement.xpReward} XP"
                    ))

                    DetailCard("REGRAS E ESCOPO", listOf(
                        "Condição" to state.achievement.conditionType.name,
                        "Qtd. Necessária" to state.achievement.requiredAmount.toString(),
                        "Alvo (Target)" to targetName,
                        "Idioma" to languageName,
                        "Categoria" to (state.achievement.category ?: "Geral"),
                        "Visibilidade" to if (state.achievement.isHidden) "Oculta (Secreta)" else "Visível",
                        "Global" to if (state.achievement.isGlobal) "Sim" else "Não (Específico)"
                    ))
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Conquista", fontWeight = FontWeight.Bold) },
                text = { Text("Deseja realmente excluir esta conquista? Esta ação removerá o progresso de todos os usuários vinculados.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteAchievement(); showDelete = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EXCLUIR DEFINITIVAMENTE", fontWeight = FontWeight.Bold)
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
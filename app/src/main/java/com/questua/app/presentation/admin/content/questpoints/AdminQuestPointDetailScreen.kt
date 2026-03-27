package com.questua.app.presentation.admin.content.questpoints

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestPointDetailScreen(
    navController: NavController,
    viewModel: AdminQuestPointDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.questPoint?.title ?: "Detalhes do Ponto",
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
            if (state.questPoint != null) {
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
                            onClick = { showDeleteDialog = true },
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
                            onClick = { showEditDialog = true },
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
            } else {
                state.questPoint?.let { point ->
                    val cityName = state.cities.find { it.id == point.cityId }?.name ?: point.cityId

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        if (!point.imageUrl.isNullOrBlank()) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                AsyncImage(
                                    model = point.imageUrl.toFullImageUrl(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        DetailCard(
                            title = "IDENTIFICAÇÃO PRINCIPAL",
                            items = listOf(
                                "ID" to point.id,
                                "Título do Ponto" to point.title,
                                "Cidade Vinculada" to cityName,
                                "Dificuldade Técnica" to "${point.difficulty}/5"
                            )
                        )

                        DetailCard(
                            title = "COORDENADAS GEOGRÁFICAS",
                            items = listOf(
                                "Latitude" to point.lat.toString(),
                                "Longitude" to point.lon.toString(),
                            )
                        )

                        if (!point.iconUrl.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text("Ícone de Mapa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.weight(1f))
                                    AsyncImage(
                                        model = point.iconUrl.toFullImageUrl(),
                                        contentDescription = null,
                                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }

                        DetailCard(
                            title = "REQUISITOS DE DESBLOQUEIO",
                            items = listOf(
                                "Acesso Premium" to if (point.unlockRequirement?.premiumAccess == true) "Obrigatório" else "Livre",
                                "Nível de Gamificação" to (point.unlockRequirement?.requiredGamificationLevel?.toString() ?: "Nível 1"),
                                "Proficiência CEFR" to (point.unlockRequirement?.requiredCefrLevel ?: "Livre")
                            )
                        )

                        DetailCard(
                            title = "STATUS E ATRIBUTOS",
                            items = listOf(
                                "Status de Publicação" to if (point.isPublished) "Público" else "Rascunho",
                                "Tipo de Conteúdo" to if (point.isPremium) "Premium" else "Gratuito",
                                "Origem do Conteúdo" to if (point.isAiGenerated) "IA Generated" else "Curadoria Humana"
                            )
                        )

                        if (point.description.isNotBlank()) {
                            DetailCard(title = "DESCRIÇÃO DETALHADA", items = listOf("" to point.description))
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }

        if (showEditDialog && state.questPoint != null) {
            QuestPointFormDialog(
                questPoint = state.questPoint,
                cities = state.cities,
                onDismiss = { showEditDialog = false },
                onConfirm = { title, cId, desc, diff, lat, lon, img, ico, unl, prem, ai, pub ->
                    viewModel.saveQuestPoint(title, cId, desc, diff, lat, lon, img, ico, unl, prem, ai, pub)
                    showEditDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Ponto", fontWeight = FontWeight.Bold) },
                text = { Text("Deseja realmente excluir este Quest Point? Missões vinculadas a este local poderão parar de funcionar.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteQuestPoint()
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EXCLUIR", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("CANCELAR")
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

@Composable
fun DetailCard(title: String, items: List<Pair<String, String>>) {
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
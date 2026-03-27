package com.questua.app.presentation.admin.content.dialogues

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.presentation.admin.content.cities.DetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDialogueDetailScreen(
    navController: NavController,
    viewModel: AdminDialogueDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.dialogue != null) {
        SceneDialogueFormDialog(
            dialogue = state.dialogue,
            characters = state.characters,
            allDialogues = state.allDialogues,
            onDismiss = { showEdit = false },
            onConfirm = { txt, desc, bg, mus, st, eff, spk, aud, exp, mod, er, ch, nxt, ai ->
                viewModel.saveDialogue(txt, desc, bg, mus, st, eff, spk, aud, exp, mod, er, ch, nxt, ai)
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
                        text = "Detalhes do Diálogo",
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
            if (state.dialogue != null) {
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
                LoadingSpinner(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (state.dialogue != null) {
                val speakerName = state.characters.find { it.id == state.dialogue.speakerCharacterId }?.name ?: "Narrador"
                val nextName = state.allDialogues.find { it.id == state.dialogue.nextDialogueId }?.textContent?.take(20) ?: "Nenhum"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailCard("IDENTIFICAÇÃO E NARRATIVA", listOf(
                        "Identificador" to state.dialogue.description,
                        "Texto da Cena" to state.dialogue.textContent,
                        "Personagem" to speakerName,
                        "Data de Criação" to state.dialogue.createdAt.take(10)
                    ))

                    DetailCard("MÍDIA E AMBIENTE", listOf(
                        "Cenário (Background)" to (state.dialogue.backgroundUrl ?: "Padrão"),
                        "Trilha Sonora" to (state.dialogue.bgMusicUrl ?: "Nenhuma"),
                        "Áudio de Voz" to (state.dialogue.audioUrl ?: "Não configurado")
                    ))

                    DetailCard("REGRAS DE FLUXO", listOf(
                        "Modo de Entrada" to state.dialogue.inputMode.name,
                        "Aguarda Resposta" to if(state.dialogue.expectsUserResponse) "Sim" else "Não",
                        "Resposta Esperada" to (state.dialogue.expectedResponse ?: "N/A"),
                        "Sequência Direta" to nextName
                    ))

                    if (!state.dialogue.choices.isNullOrEmpty()) {
                        DetailCard("RAMIFICAÇÕES (${state.dialogue.choices.size})", state.dialogue.choices.mapIndexed { i, c ->
                            "Opção ${i+1}" to "${c.text} ➜ Destino: ${c.nextDialogueId?.take(8) ?: "Final da Cena"}"
                        })
                    }

                    if (!state.dialogue.sceneEffects.isNullOrEmpty()) {
                        DetailCard("EFEITOS DE CENA (${state.dialogue.sceneEffects.size})", state.dialogue.sceneEffects.map {
                            it.type to "Intensidade: ${it.intensity ?: "Normal"}, Duração: ${it.duration ?: "Instantâneo"}"
                        })
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Diálogo", fontWeight = FontWeight.Bold) },
                text = { Text("Deseja realmente excluir este diálogo? Esta ação pode quebrar o fluxo de missões que dependem desta cena.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteDialogue(); showDelete = false },
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
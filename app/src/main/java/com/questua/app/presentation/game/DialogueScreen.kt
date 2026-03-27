package com.questua.app.presentation.game

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.Choice

@Composable
fun DialogueScreen(
    onNavigateBack: () -> Unit,
    onQuestCompleted: (String, Int, Int, Int) -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var audioReplayTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.navigateToResult) {
        if (state.navigateToResult) {
            state.userQuestId?.let { id ->
                onQuestCompleted(id, state.xpEarned, state.correctAnswers, state.totalQuestions)
                viewModel.onResultNavigationHandled()
            }
        }
    }

    AudioHandler(
        bgMusicUrl = state.currentDialogue?.bgMusicUrl,
        voiceUrl = state.currentDialogue?.audioUrl,
        replayTrigger = audioReplayTrigger
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            state.currentDialogue?.backgroundUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url.toFullImageUrl())
                        .crossfade(1500)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 200.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                state.speaker?.avatarUrl?.let { avatarUrl ->
                    AnimatedContent(
                        targetState = avatarUrl,
                        transitionSpec = {
                            fadeIn(tween(600)) + slideInHorizontally { it / 4 } togetherWith
                                    fadeOut(tween(400))
                        },
                        label = "CharacterTransition"
                    ) { targetUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(targetUrl.toFullImageUrl())
                                .crossfade(true)
                                .build(),
                            contentDescription = state.speaker?.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxHeight(0.75f)
                                .widthIn(max = 600.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                DialogueHUD(
                    progress = state.questProgress,
                    onClose = onNavigateBack
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LoadingSpinner()
                    }
                } else {
                    state.currentDialogue?.let { dialogue ->
                        VNTextBox(
                            speakerName = state.speaker?.name ?: "Narrador",
                            text = dialogue.textContent,
                            inputMode = dialogue.inputMode,
                            userInput = state.userInput,
                            choices = dialogue.choices,
                            isSubmitting = state.isSubmitting,
                            hasAudio = !dialogue.audioUrl.isNullOrBlank(),
                            onInputChange = viewModel::onUserInputChange,
                            onTextSubmit = viewModel::onSubmitText,
                            onChoiceClick = viewModel::onChoiceSelected,
                            onContinueClick = viewModel::onContinue,
                            onReplayAudio = { audioReplayTrigger++ }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                FeedbackOverlay(state = state.feedbackState)
            }
        }
    }
}

@Composable
fun VNTextBox(
    speakerName: String,
    text: String,
    inputMode: InputMode,
    userInput: String,
    choices: List<Choice>?,
    isSubmitting: Boolean,
    hasAudio: Boolean,
    onInputChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onChoiceClick: (Choice) -> Unit,
    onContinueClick: () -> Unit,
    onReplayAudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = speakerName.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (hasAudio) {
                    FilledTonalIconButton(
                        onClick = onReplayAudio,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Ouvir",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = text,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label = "TextAnimation"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            InteractionArea(
                inputMode, userInput, choices, isSubmitting,
                onInputChange, onTextSubmit, onChoiceClick, onContinueClick
            )
        }
    }
}

@Composable
fun InteractionArea(
    inputMode: InputMode,
    userInput: String,
    choices: List<Choice>?,
    isSubmitting: Boolean,
    onInputChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onChoiceClick: (Choice) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (inputMode) {
            InputMode.CHOICE -> {
                choices?.forEach { choice ->
                    Button(
                        onClick = { onChoiceClick(choice) },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = choice.text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            InputMode.TEXT -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuestuaTextField(
                        value = userInput,
                        onValueChange = onInputChange,
                        placeholder = "Escreva sua resposta...",
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    FloatingActionButton(
                        onClick = onTextSubmit,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        }
                    }
                }
            }
            InputMode.NONE -> {
                QuestuaButton(
                    text = "CONTINUAR",
                    onClick = onContinueClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackOverlay(state: FeedbackState) {
    AnimatedVisibility(
        visible = state !is FeedbackState.None,
        enter = scaleIn(tween(300)) + fadeIn(),
        exit = scaleOut(tween(300)) + fadeOut()
    ) {
        val (bgColor, icon, msg) = when (state) {
            is FeedbackState.Success -> Triple(
                MaterialTheme.colorScheme.primaryContainer,
                "✨",
                state.message ?: "Correto!"
            )
            is FeedbackState.Error -> Triple(
                MaterialTheme.colorScheme.errorContainer,
                "⚠️",
                state.message ?: "Ops, algo está errado."
            )
            else -> Triple(Color.Transparent, "", "")
        }

        Surface(
            modifier = Modifier.padding(32.dp).widthIn(max = 400.dp),
            color = bgColor,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 16.dp,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = icon, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = if (state is FeedbackState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun DialogueHUD(progress: Float, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Close, "Sair", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(20.dp))

        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun AudioHandler(
    bgMusicUrl: String?,
    voiceUrl: String?,
    replayTrigger: Int
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val bgmPlayer = remember { MediaPlayer() }
    val voicePlayer = remember { MediaPlayer() }

    var currentBgmUrl by remember { mutableStateOf<String?>(null) }
    var currentVoiceUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bgMusicUrl) {
        if (bgMusicUrl != null && bgMusicUrl != currentBgmUrl) {
            try {
                if (bgmPlayer.isPlaying) bgmPlayer.stop()
                bgmPlayer.reset()
                bgmPlayer.setDataSource(bgMusicUrl.toFullImageUrl())
                bgmPlayer.isLooping = true
                bgmPlayer.setVolume(0.12f, 0.12f)
                bgmPlayer.prepareAsync()
                bgmPlayer.setOnPreparedListener { it.start() }
                currentBgmUrl = bgMusicUrl
            } catch (e: Exception) { e.printStackTrace() }
        } else if (bgMusicUrl == null) {
            if (bgmPlayer.isPlaying) bgmPlayer.stop()
            currentBgmUrl = null
        }
    }

    fun playVoice(url: String?) {
        if (url == null) return
        try {
            if (voicePlayer.isPlaying) voicePlayer.stop()
            voicePlayer.reset()
            voicePlayer.setDataSource(url.toFullImageUrl())
            voicePlayer.setVolume(1.0f, 1.0f)
            voicePlayer.prepareAsync()
            voicePlayer.setOnPreparedListener { it.start() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    LaunchedEffect(voiceUrl) {
        if (voiceUrl != null) {
            currentVoiceUrl = voiceUrl
            playVoice(voiceUrl)
        }
    }

    LaunchedEffect(replayTrigger) {
        if (replayTrigger > 0 && currentVoiceUrl != null) {
            playVoice(currentVoiceUrl)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (bgmPlayer.isPlaying) bgmPlayer.pause()
                    if (voicePlayer.isPlaying) voicePlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (currentBgmUrl != null && !bgmPlayer.isPlaying) bgmPlayer.start()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bgmPlayer.release()
            voicePlayer.release()
        }
    }
}
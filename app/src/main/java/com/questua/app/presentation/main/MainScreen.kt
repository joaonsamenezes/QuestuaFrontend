package com.questua.app.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.common.capturePosition
import com.questua.app.core.ui.components.*
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.core.ui.managers.SnackbarManager
import com.questua.app.domain.enums.ReportType
import com.questua.app.presentation.exploration.worldmap.WorldMapScreen
import com.questua.app.presentation.hub.HubScreen
import com.questua.app.presentation.navigation.Screen
import com.questua.app.presentation.profile.ProfileScreen
import com.questua.app.presentation.progress.ProgressScreen

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToCity: (String) -> Unit,
    onNavigateToUnlock: (String, String) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFeedback: (ReportType) -> Unit,
    onNavigateToTier: () -> Unit,
    navController: NavController,
    achievementMonitor: AchievementMonitor,
    isNewUser: Boolean = false,
    viewModel: MainViewModel = hiltViewModel()
) {
    var currentTab by rememberSaveable { mutableStateOf(HubTab.HOME) }
    val unseenAchievements by achievementMonitor.unseenAchievementIds.collectAsState()
    val tutorialStepIndex = viewModel.currentTutorialStep

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessages by SnackbarManager.messages.collectAsState()

    LaunchedEffect(Unit) {
        if (isNewUser) viewModel.startTutorialForNewUser()
    }

    LaunchedEffect(tutorialStepIndex) {
        if (tutorialStepIndex == 3) {
            currentTab = HubTab.MAP
        }
    }

    LaunchedEffect(snackbarMessages) {
        if (snackbarMessages.isNotEmpty()) {
            val message = snackbarMessages.first()
            snackbarHostState.showSnackbar(
                message = message.message,
                duration = message.duration
            )
            SnackbarManager.dismissMessage(message.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    val isError = snackbarMessages.firstOrNull()?.isError == true
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { currentTab = it },
                    onMapTabPositioned = { o, s -> viewModel.updatePosition("map_tab", o, s) },
                    onProgressTabPositioned = { o, s -> viewModel.updatePosition("progress_tab", o, s) },
                    onProfileTabPositioned = { o, s -> viewModel.updatePosition("profile_tab", o, s) }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AchievementOverlay(monitor = achievementMonitor)

                when (currentTab) {
                    HubTab.HOME -> HubScreen(
                        onNavigateToLanguages = onNavigateToLanguages,
                        onNavigateToQuest = { navController.navigate(Screen.QuestIntro.passId(it)) },
                        onNavigateToUnlock = onNavigateToUnlock,
                        onNavigateToContent = { id, type ->
                            when (type) {
                                "CITY" -> onNavigateToCity(id)
                                "QUEST" -> navController.navigate(Screen.QuestIntro.passId(id))
                                "QUEST_POINT" -> navController.navigate(Screen.QuestPoint.passId(id))
                            }
                        }
                    )
                    HubTab.MAP -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .capturePosition { offset, size ->
                                    viewModel.updatePosition("map_area", offset, size)
                                }
                        ) {
                            WorldMapScreen(
                                onNavigateBack = null,
                                onNavigateToCity = onNavigateToCity,
                                onNavigateToUnlock = onNavigateToUnlock,
                                onNavigateToTier = onNavigateToTier
                            )
                        }
                    }
                    HubTab.PROFILE -> ProfileScreen(onNavigateToLogin, onNavigateToHelp, onNavigateToAdmin, { onNavigateToFeedback(ReportType.FEEDBACK) }, null, navController)
                    HubTab.PROGRESS -> ProgressScreen(achievementMonitor = achievementMonitor)
                }
            }
        }

        if (tutorialStepIndex != -1) {
            val stepData = viewModel.tutorialSteps[tutorialStepIndex]
            val targetPos = viewModel.targetPositions[stepData.targetKey]

            if (targetPos != null) {
                TutorialSpotlightOverlay(
                    targetOffset = targetPos.first,
                    targetSize = targetPos.second,
                    onTargetClick = {
                        if (tutorialStepIndex == 2) {
                            currentTab = HubTab.MAP
                            viewModel.nextTutorialStep()
                        } else {
                            viewModel.nextTutorialStep()
                        }
                    }
                )

                val isBottomTarget = stepData.targetKey.contains("tab")

                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = if (isBottomTarget) Alignment.BottomCenter else Alignment.Center
                ) {
                    ExplorerSpeechBubble(
                        text = stepData.text,
                        mood = stepData.mood,
                        modifier = Modifier.padding(
                            bottom = if (isBottomTarget) targetPos.second.height.dp + 80.dp else 0.dp
                        )
                    )
                }
            }
        }
    }
}
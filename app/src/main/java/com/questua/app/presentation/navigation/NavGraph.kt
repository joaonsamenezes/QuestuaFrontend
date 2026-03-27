// app/src/main/java/com/questua/app/presentation/navigation/NavGraph.kt
package com.questua.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.presentation.admin.feedback.AdminReportDetailScreen
import com.questua.app.presentation.auth.ForgotPasswordScreen
import com.questua.app.presentation.auth.ForgotPasswordViewModel
import com.questua.app.presentation.auth.LoginScreen
import com.questua.app.presentation.auth.RegisterScreen
import com.questua.app.presentation.auth.RegisterVerifyScreen
import com.questua.app.presentation.auth.RegisterViewModel
import com.questua.app.presentation.auth.ResetPasswordScreen
import com.questua.app.presentation.common.InitialScreen
import com.questua.app.presentation.common.TermsAndConditionsScreen
import com.questua.app.presentation.exploration.city.CityDetailScreen
import com.questua.app.presentation.exploration.questpoint.QuestPointScreen
import com.questua.app.presentation.game.DialogueScreen
import com.questua.app.presentation.game.QuestIntroScreen
import com.questua.app.presentation.game.QuestResultScreen
import com.questua.app.presentation.languages.LanguagesListScreen
import com.questua.app.presentation.main.MainScreen
import com.questua.app.presentation.monetization.UnlockPreviewScreen
import com.questua.app.presentation.onboarding.IntroExplorationScreen
import com.questua.app.presentation.onboarding.LanguageSelectionScreen
import com.questua.app.presentation.onboarding.PlacementScreen
import com.questua.app.presentation.profile.FeedbackScreen
import com.questua.app.presentation.profile.HelpScreen
import com.questua.app.presentation.usertier.UserTierScreen
import com.questua.app.presentation.navigation.adminNavGraph

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String,
    achievementMonitor: AchievementMonitor
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Screen.Initial.route) {
            InitialScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.IntroOnboarding.route) }
            )
        }

        composable(route = Screen.IntroOnboarding.route) {
            IntroExplorationScreen(
                onContinue = { navController.navigate(Screen.LanguageSelection.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = { languageId, languageName ->
                    navController.navigate(Screen.Placement.passArgs(languageId, languageName))
                },
                onNavigateToInitial = {
                    navController.navigate(Screen.Initial.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Placement.route,
            arguments = listOf(
                navArgument("languageId") { type = NavType.StringType },
                navArgument("languageName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val languageId = backStackEntry.arguments?.getString("languageId") ?: ""
            PlacementScreen(
                onNavigateBack = { navController.popBackStack() },
                onFinished = { cefrLevel ->
                    navController.navigate(Screen.Register.passArgs(languageId, cefrLevel))
                }
            )
        }

        composable(
            route = Screen.Register.route,
            arguments = listOf(
                navArgument("languageId") { type = NavType.StringType },
                navArgument("cefrLevel") { type = NavType.StringType }
            )
        ) {
            RegisterScreen(
                onNavigateToVerify = {
                    navController.navigate(Screen.RegisterVerify.route)
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate("terms_and_conditions") }
            )
        }

        composable(route = Screen.RegisterVerify.route) { backStackEntry ->
            val registerBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Register.route)
            }
            val viewModel: RegisterViewModel = hiltViewModel(registerBackStackEntry)

            RegisterVerifyScreen(
                onNavigateToHome = { isNewUser ->
                    navController.navigate(Screen.Home.passArgs(isNewUser)) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.passArgs(false)) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.IntroOnboarding.route) },
                onNavigateToTerms = { navController.navigate("terms_and_conditions") },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable("terms_and_conditions") {
            TermsAndConditionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("isNewUser") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isNewUser = backStackEntry.arguments?.getBoolean("isNewUser") ?: false

            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onNavigateToLanguages = {
                    navController.navigate(Screen.LanguagesList.route)
                },
                onNavigateToCity = { cityId ->
                    navController.navigate(Screen.CityDetail.passId(cityId))
                },
                onNavigateToUnlock = { contentId, contentType ->
                    navController.navigate(Screen.UnlockPreview.passArgs(contentId, contentType))
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminHome.route)
                },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToFeedback = { type ->
                    navController.navigate(Screen.Feedback.passReportType(type.name))
                },
                onNavigateToTier = { navController.navigate(Screen.UserTier.route) },
                navController = navController,
                achievementMonitor = achievementMonitor,
                isNewUser = isNewUser
            )
        }

        composable(route = Screen.UserTier.route) {
            UserTierScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.LanguagesList.route) {
            LanguagesListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.passArgs(false)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Help.route) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReport = { type ->
                    navController.navigate(Screen.Feedback.passReportType(type.name))
                }
            )
        }

        composable(
            route = Screen.Feedback.route,
            arguments = listOf(navArgument("reportType") { type = NavType.StringType })
        ) {
            val previousBackStackEntry = navController.previousBackStackEntry

            FeedbackScreen(
                onNavigateBack = { navController.popBackStack() },
                onReportSent = { successMessage ->
                    previousBackStackEntry?.savedStateHandle?.set("feedback_message", successMessage)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CityDetail.route,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) {
            CityDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestPoint = { pointId ->
                    navController.navigate(Screen.QuestPoint.passId(pointId))
                },
                onNavigateToUnlock = { contentId, contentType ->
                    navController.navigate(Screen.UnlockPreview.passArgs(contentId, contentType))
                }
            )
        }

        composable(
            route = Screen.QuestPoint.route,
            arguments = listOf(navArgument("pointId") { type = NavType.StringType })
        ) {
            QuestPointScreen(
                onNavigateBack = { navController.popBackStack() },
                onQuestClick = { questId ->
                    navController.navigate(Screen.QuestIntro.passId(questId))
                },
                onNavigateToUnlock = { contentId, contentType ->
                    navController.navigate(Screen.UnlockPreview.passArgs(contentId, contentType))
                }
            )
        }

        adminNavGraph(navController)

        composable(
            route = Screen.AdminReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            AdminReportDetailScreen(navController = navController)
        }

        composable(
            route = Screen.QuestIntro.route,
            arguments = listOf(navArgument("questId") { type = NavType.StringType })
        ) {
            QuestIntroScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartGameplay = { questId ->
                    navController.navigate(Screen.Dialogue.passId(questId))
                }
            )
        }

        composable(
            route = Screen.UnlockPreview.route,
            arguments = listOf(
                navArgument("contentId") { type = NavType.StringType },
                navArgument("contentType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            val contentType = backStackEntry.arguments?.getString("contentType") ?: ""

            UnlockPreviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onContentUnlocked = { id, type ->
                    when (type) {
                        "QUEST" -> {
                            navController.navigate(Screen.QuestIntro.passId(id)) {
                                popUpTo(Screen.UnlockPreview.route) { inclusive = true }
                            }
                        }
                        "CITY" -> {
                            navController.navigate(Screen.CityDetail.passId(id)) {
                                popUpTo(Screen.UnlockPreview.route) { inclusive = true }
                            }
                        }
                        "QUEST_POINT" -> {
                            navController.navigate(Screen.QuestPoint.passId(id)) {
                                popUpTo(Screen.UnlockPreview.route) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.Dialogue.route,
            arguments = listOf(navArgument("questId") { type = NavType.StringType })
        ) {
            DialogueScreen(
                onNavigateBack = { navController.popBackStack() },
                onQuestCompleted = { questId, xp, correct, total ->
                    navController.navigate(
                        Screen.QuestResult.createRoute(questId, xp, correct, total)
                    ) {
                        popUpTo(Screen.QuestPoint.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.QuestResult.route,
            arguments = listOf(
                navArgument("questId") { type = NavType.StringType },
                navArgument("xpEarned") { type = NavType.IntType },
                navArgument("correctAnswers") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType }
            )
        ) {
            QuestResultScreen(
                onNavigateToQuest = { nextQuestId ->
                    navController.navigate(Screen.QuestIntro.passId(nextQuestId)) {
                        popUpTo(Screen.QuestResult.route) { inclusive = true }
                    }
                },
                onNavigateBackToPoint = { _ ->
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ResetPassword.route) {
            val backStackEntry = remember(it) { navController.getBackStackEntry(Screen.ForgotPassword.route) }
            val viewModel: ForgotPasswordViewModel = hiltViewModel(backStackEntry)

            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}
package com.example.prtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.prtracker.ui.screens.AddExerciseScreen
import com.example.prtracker.ui.screens.AddGoalScreen
import com.example.prtracker.ui.screens.AppearanceScreen
import com.example.prtracker.ui.screens.HomeScreen
import com.example.prtracker.ui.screens.CalendarScreen
import com.example.prtracker.ui.screens.DashboardScreen
import com.example.prtracker.ui.screens.ExerciseDetailScreen
import com.example.prtracker.ui.screens.ExerciseHistoryScreen
import com.example.prtracker.ui.screens.WorkoutHistoryScreen
import com.example.prtracker.ui.screens.GoalsScreen
import com.example.prtracker.ui.screens.HoldTimerScreen
import com.example.prtracker.ui.screens.LiveRunScreen
import com.example.prtracker.ui.screens.LogEntryScreen
import com.example.prtracker.ui.screens.LogRunScreen
import com.example.prtracker.ui.screens.RankScreen
import com.example.prtracker.ui.screens.LogWeightScreen
import com.example.prtracker.ui.screens.NotificationSettingsScreen
import com.example.prtracker.ui.screens.RunHistoryScreen
import com.example.prtracker.ui.screens.SettingsScreen
import com.example.prtracker.ui.screens.SyncExportScreen
import com.example.prtracker.ui.screens.SyncImportScreen
import com.example.prtracker.ui.screens.WeightScreen
import com.example.prtracker.ui.screens.WorkoutPresetDetailScreen
import com.example.prtracker.ui.screens.WorkoutPresetsScreen
import com.example.prtracker.ui.screens.WorkoutSessionScreen
import com.example.prtracker.ui.screens.PresetAnalysisScreen
import com.example.prtracker.viewmodel.PRViewModel

object Routes {
    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    const val GOALS = "goals"
    const val ADD_GOAL = "add_goal"
    const val ADD_EXERCISE = "add_exercise"
    const val LOG_ENTRY = "log_entry/{exerciseId}"
    const val DETAIL = "detail/{exerciseId}"
    const val SETTINGS = "settings"
    const val WEIGHT = "weight"
    const val LOG_WEIGHT = "log_weight"
    const val CALENDAR = "calendar"
    const val SYNC_EXPORT = "sync_export"
    const val SYNC_IMPORT = "sync_import"
    const val RANK = "rank"
    const val HOLD_TIMER = "hold_timer/{exerciseId}"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val APPEARANCE = "appearance"
    const val LOG_RUN = "log_run"
    const val LIVE_RUN = "live_run"
    const val RUN_HISTORY = "run_history"
    const val PRESETS = "presets"
    const val PRESET_DETAIL = "preset_detail"
    const val PRESET_ANALYSIS = "preset_analysis/{presetId}"
    const val WORKOUT_SESSION = "workout_session/{presetId}"
    const val EXERCISE_HISTORY = "exercise_history"
    const val WORKOUT_HISTORY = "workout_history"

    fun logEntry(exerciseId: String) = "log_entry/$exerciseId"
    fun detail(exerciseId: String) = "detail/$exerciseId"
    fun holdTimer(exerciseId: String) = "hold_timer/$exerciseId"
    fun logRun() = "log_run"
    fun runHistory() = "run_history"
    fun presetDetail(presetId: String) = "preset_detail/$presetId"
    fun presetAnalysis(presetId: String) = "preset_analysis/$presetId"
    fun workoutSession(presetId: String) = "workout_session/$presetId"
}

@Composable
fun PrTrackerNavGraph(
    navController: NavHostController,
    viewModel: PRViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.ADD_EXERCISE) {
            AddExerciseScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = Routes.LOG_ENTRY,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            LogEntryScreen(
                viewModel = viewModel,
                navController = navController,
                exerciseId = exerciseId
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            ExerciseDetailScreen(
                viewModel = viewModel,
                navController = navController,
                exerciseId = exerciseId
            )
        }
        composable(Routes.GOALS) {
            GoalsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.ADD_GOAL) {
            AddGoalScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.APPEARANCE) {
            AppearanceScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.WEIGHT) {
            WeightScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.LOG_WEIGHT) {
            LogWeightScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.SYNC_EXPORT) {
            SyncExportScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.SYNC_IMPORT) {
            SyncImportScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.RANK) {
            RankScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(
            route = Routes.HOLD_TIMER,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            HoldTimerScreen(
                viewModel = viewModel,
                navController = navController,
                exerciseId = exerciseId
            )
        }
        composable(Routes.LOG_RUN) {
            LogRunScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.LIVE_RUN) {
            LiveRunScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(Routes.RUN_HISTORY) {
            RunHistoryScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.PRESETS) {
            WorkoutPresetsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = "${Routes.PRESETS}?editId={editId}",
            arguments = listOf(navArgument("editId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val editId = backStackEntry.arguments?.getString("editId")
            WorkoutPresetsScreen(
                viewModel = viewModel,
                navController = navController,
                initialEditId = editId
            )
        }
        composable(
            route = "${Routes.PRESET_DETAIL}/{presetId}",
            arguments = listOf(navArgument("presetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getString("presetId") ?: return@composable
            WorkoutPresetDetailScreen(
                presetId = presetId,
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = Routes.PRESET_ANALYSIS,
            arguments = listOf(navArgument("presetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getString("presetId") ?: return@composable
            PresetAnalysisScreen(
                presetId = presetId,
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = Routes.WORKOUT_SESSION,
            arguments = listOf(navArgument("presetId") { type = NavType.StringType })
        ) {
            WorkoutSessionScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.EXERCISE_HISTORY) {
            ExerciseHistoryScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(Routes.WORKOUT_HISTORY) {
            WorkoutHistoryScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

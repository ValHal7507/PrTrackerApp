package com.example.prtracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.prtracker.data.AppearanceSettings
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.StorageManager
import com.example.prtracker.navigation.PrTrackerNavGraph
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.Surface
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.viewmodel.PRViewModel
import com.example.prtracker.work.EveningReviewWorker
import com.example.prtracker.work.GoalNotificationWorker
import com.example.prtracker.work.MorningReminderWorker
import com.example.prtracker.work.WeeklySummaryWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var pendingImportJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannels()
        scheduleAllNotifications()

        handleImportIntent(intent)

        setContent {
            com.example.prtracker.ui.theme.PrTrackerTheme {
                val viewModel: PRViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val appSettings by viewModel.appSettings.collectAsState()
                val appearance = appSettings.appearance

                LaunchedEffect(pendingImportJson) {
                    val json = pendingImportJson
                    if (json != null) {
                        pendingImportJson = null
                        viewModel.setPendingImportJson(json)
                        navController.navigate(Routes.SYNC_IMPORT)
                    }
                }

                val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.DASHBOARD, Routes.PRESETS, Routes.GOALS, Routes.WEIGHT, Routes.CALENDAR, Routes.SETTINGS, "${Routes.PRESETS}?editId={editId}", "${Routes.PRESET_DETAIL}/{presetId}") && currentRoute !in listOf(Routes.LIVE_RUN, Routes.WORKOUT_SESSION)
                val showFab = currentRoute == Routes.DASHBOARD

                NotificationPermissionRequester()

                Scaffold(
                    modifier = Modifier.fillMaxSize().background(Background),
                    containerColor = Background,
                    bottomBar = {
                        if (showBottomBar) {
                            PRBottomNavigationBar(
                                navController = navController,
                                currentRoute = currentRoute,
                                soundEnabled = appSettings.soundEnabled,
                                appearance = appearance
                            )
                        }
                    },
                    floatingActionButton = {
                        if (showFab) {
                            FloatingActionButton(
                                onClick = { navController.navigate(Routes.ADD_EXERCISE) },
                                containerColor = appearance.systemAccentColor.copy(alpha = 0.15f),
                                contentColor = appearance.systemAccentColor,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add exercise",
                                    tint = appearance.systemAccentColor
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        CompositionLocalProvider(LocalAppearance provides appearance) {
                            PrTrackerNavGraph(
                                navController = navController,
                                viewModel = viewModel,
                                startDestination = Routes.HOME
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleImportIntent(intent)
    }

    private fun handleImportIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            try {
                val inputStream = contentResolver.openInputStream(intent.data!!)
                val json = inputStream?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    pendingImportJson = json
                }
            } catch (_: Exception) {
            }
            intent.data = null
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val morningChannel = NotificationChannel(
            "pr_tracker_morning",
            "Morning Training Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily 8AM reminder to log your training session"
            enableVibration(true)
            enableLights(true)
        }
        manager.createNotificationChannel(morningChannel)

        val eveningChannel = NotificationChannel(
            "pr_tracker_evening",
            "Evening Daily Review",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily 9:30PM summary of what you logged today"
            enableVibration(false)
        }
        manager.createNotificationChannel(eveningChannel)
    }

    private fun scheduleAllNotifications() {
        scheduleMorningReminder()
        scheduleGoalReminders()
        scheduleEveningReview()
        scheduleWeeklySummary()
    }

    private fun scheduleMorningReminder() {
        val workManager = WorkManager.getInstance(this)
        val existingWork = workManager.getWorkInfosByTag(MorningReminderWorker.WORK_TAG).get()
        if (existingWork.isNotEmpty()) return

        val settings = StorageManager(this).loadFullData().settings
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.morningReminderHour)
            set(Calendar.MINUTE, settings.morningReminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelay = calendar.timeInMillis - now

        val workRequest = PeriodicWorkRequestBuilder<MorningReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(MorningReminderWorker.WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "morning_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleGoalReminders() {
        val workManager = WorkManager.getInstance(this)

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelay = calendar.timeInMillis - now

        val workRequest = PeriodicWorkRequestBuilder<GoalNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("goal_notification")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_goal_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleEveningReview() {
        val workManager = WorkManager.getInstance(this)
        val existingWork = workManager.getWorkInfosByTag(EveningReviewWorker.WORK_TAG).get()
        if (existingWork.isNotEmpty()) return

        val settings = StorageManager(this).loadFullData().settings
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.eveningReviewHour)
            set(Calendar.MINUTE, settings.eveningReviewMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelay = calendar.timeInMillis - now

        val workRequest = PeriodicWorkRequestBuilder<EveningReviewWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(EveningReviewWorker.WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "evening_review",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleWeeklySummary() {
        val workManager = WorkManager.getInstance(this)
        val existingWork = workManager.getWorkInfosByTag(WeeklySummaryWorker.WORK_TAG).get()
        if (existingWork.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        val initialDelay = calendar.timeInMillis - now

        val workRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WeeklySummaryWorker.WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "weekly_summary",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        fun rescheduleNotifications(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("morning_reminder")
            workManager.cancelUniqueWork("evening_review")

            val settings = StorageManager(context).loadFullData().settings
            val now = System.currentTimeMillis()

            val morningCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, settings.morningReminderHour)
                set(Calendar.MINUTE, settings.morningReminderMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val morningDelay = morningCal.timeInMillis - now
            val morningRequest = PeriodicWorkRequestBuilder<MorningReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(morningDelay, TimeUnit.MILLISECONDS)
                .addTag(MorningReminderWorker.WORK_TAG)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "morning_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                morningRequest
            )

            val eveningCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, settings.eveningReviewHour)
                set(Calendar.MINUTE, settings.eveningReviewMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val eveningDelay = eveningCal.timeInMillis - now
            val eveningRequest = PeriodicWorkRequestBuilder<EveningReviewWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(eveningDelay, TimeUnit.MILLISECONDS)
                .addTag(EveningReviewWorker.WORK_TAG)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "evening_review",
                ExistingPeriodicWorkPolicy.REPLACE,
                eveningRequest
            )
        }
    }
}

@Composable
private fun NotificationPermissionRequester() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showDialog = true
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Daily Goal Reminders", color = TextPrimary) },
            text = {
                Text(
                    "PRTracker can send you daily reminders at 9:00 AM to help you stay on track with your fitness goals.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Allow", color = PrimaryAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Not Now", color = TextSecondary)
                }
            },
            containerColor = Background
        )
    }
}

@Composable
fun PRBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?,
    soundEnabled: Boolean = false,
    appearance: AppearanceSettings = AppearanceSettings()
) {
    val accent = appearance.systemAccentColor
    val secondary = appearance.systemSecondaryColor
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple(Routes.HOME, Icons.Default.Home, "Home"),
            Triple(Routes.DASHBOARD, Icons.Default.FitnessCenter, "Dashboard"),
            Triple(Routes.PRESETS, Icons.Default.List, "Presets"),
            Triple(Routes.GOALS, Icons.Default.TrackChanges, "Goals"),
            Triple(Routes.WEIGHT, Icons.Default.MonitorWeight, "Weight"),
            Triple(Routes.CALENDAR, Icons.Default.CalendarMonth, "Calendar"),
            Triple(Routes.SETTINGS, Icons.Default.Settings, "Settings")
        )

        items.forEach { (route, icon, label) ->
            val selected = currentRoute == route || (route == Routes.PRESETS && currentRoute == "${Routes.PRESETS}?editId={editId}")
            NavigationBarItem(
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) accent else TextSecondary
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth(0.5f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(accent, secondary)
                                        )
                                    )
                            )
                        }
                    }
                },
                label = null,
                selected = selected,
                onClick = {
                    if (route != currentRoute) {
                        if (soundEnabled) SoundEngine.playNavigation()
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    unselectedIconColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

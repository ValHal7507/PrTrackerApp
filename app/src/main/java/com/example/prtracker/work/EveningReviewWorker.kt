package com.example.prtracker.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prtracker.MainActivity
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.PREntry
import com.example.prtracker.data.StorageManager
import java.util.Calendar

class EveningReviewWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storageManager = StorageManager(applicationContext)
        val full = storageManager.loadFullData()
        val exercises = full.exercises
        val restDays = full.restDays

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        val todayStr = getTodayString()
        val isRestDay = todayStr in restDays

        val todayExercises = mutableListOf<Pair<Exercise, List<PREntry>>>()
        for (exercise in exercises) {
            val todayEntries = exercise.entries.filter { dateToDateString(it.date) == todayStr }
            if (todayEntries.isNotEmpty()) {
                todayExercises.add(exercise to todayEntries)
            }
        }

        val trainedToday = todayExercises.isNotEmpty()

        if (trainedToday) {
            sendTrainedTodayNotification(todayExercises, exercises, restDays)
        } else if (isRestDay) {
            sendRestDayNotification(exercises, restDays)
        } else {
            sendMissedDayNotification(exercises, restDays)
        }

        return Result.success()
    }

    private fun sendTrainedTodayNotification(
        todayExercises: List<Pair<Exercise, List<PREntry>>>,
        allExercises: List<Exercise>,
        restDays: List<String>
    ) {
        val streak = calculateStreak(allExercises, restDays)
        val totalExercises = todayExercises.size

        val newPRExercises = mutableListOf<String>()
        var totalVolume = 0
        for ((exercise, entries) in todayExercises) {
            totalVolume += entries.sumOf { it.value }
            if (isNewPRToday(exercise)) {
                newPRExercises.add(exercise.name)
            }
        }

        val bestAchievementLine = when {
            newPRExercises.isNotEmpty() -> "New PR on ${newPRExercises.first()}!"
            streak > 3 -> "Streak extended to $streak days \uD83D\uDD25"
            else -> "$totalVolume total reps logged today"
        }

        val collapsedBody = "You logged $totalExercises exercises today. $bestAchievementLine"

        val longDateStr = formatDateLong(System.currentTimeMillis())

        val expandedBody = buildString {
            appendLine("\uD83D\uDCCB TODAY'S LOG \u2014 $longDateStr")
            appendLine()
            for ((exercise, entries) in todayExercises) {
                val totalValue = entries.sumOf { it.value }
                val unit = if (exercise.type == "hold") "s" else " reps"
                val crown = if (isNewPRToday(exercise)) " \uD83D\uDC51" else ""
                appendLine("\u2022 ${exercise.name}: $totalValue$unit$crown")
            }
            appendLine()
            appendLine("Total exercises logged: $totalExercises")
            appendLine("New PRs today: ${newPRExercises.size}")
            appendLine("Current streak: $streak days \uD83D\uDD25")
            if (streak >= 7) {
                appendLine("\uD83C\uDFC6 $streak day streak \u2014 incredible consistency!")
            }
        }

        showNotification(
            title = "TODAY'S SESSION RECAP \u2705",
            body = collapsedBody,
            expandedBody = expandedBody.toString()
        )
    }

    private fun sendMissedDayNotification(
        allExercises: List<Exercise>,
        restDays: List<String>
    ) {
        val streak = calculateStreak(allExercises, restDays)

        val expandedBody = buildString {
            appendLine("No workout logged for today yet.")
            appendLine()
            appendLine("You can still mark today as a rest day in the app if")
            appendLine("you planned a recovery day \u2014 your streak will stay intact.")
            appendLine()
            appendLine("CURRENT STREAK: $streak days \uD83D\uDD25 \u2014 don't break the chain!")
            appendLine()
            append("Open PRTracker to log a session or mark a rest day.")
        }

        showNotification(
            title = "DID YOU TRAIN TODAY? \uD83E\uDD14",
            body = "No entries logged yet. Still time to get a session in.",
            expandedBody = expandedBody
        )
    }

    private fun sendRestDayNotification(
        allExercises: List<Exercise>,
        restDays: List<String>
    ) {
        val streak = calculateStreak(allExercises, restDays)

        val expandedBody = buildString {
            appendLine("Today is marked as a rest day. Your streak is safe.")
            appendLine()
            appendLine("CURRENT STREAK: $streak days \uD83D\uDD25")
            appendLine()
            append("Use today to recover and come back stronger tomorrow.")
        }

        showNotification(
            title = "REST DAY \u2014 RECOVERY MODE \uD83D\uDD0B",
            body = "Smart training includes smart recovery. See you tomorrow.",
            expandedBody = expandedBody
        )
    }

    private fun showNotification(title: String, body: String, expandedBody: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "pr_tracker_evening")
            .setSmallIcon(com.example.prtracker.R.drawable.ic_goal_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) { }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "pr_tracker_evening",
            "Evening Daily Review",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily 9:30PM summary of what you logged today"
            enableVibration(false)
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun isNewPRToday(exercise: Exercise): Boolean {
        val todayStr = getTodayString()
        val todayEntries = exercise.entries.filter { dateToDateString(it.date) == todayStr }
        if (todayEntries.isEmpty()) return false
        val todayMax = todayEntries.maxOf { it.value }
        val allTimeMax = exercise.entries.maxOf { it.value }
        val hasPreviousEntries = exercise.entries.any { dateToDateString(it.date) != todayStr }
        return todayMax >= allTimeMax && hasPreviousEntries
    }

    private fun calculateStreak(
        exercises: List<Exercise>,
        restDays: List<String>
    ): Int {
        val workoutDays = mutableSetOf<String>()
        for (exercise in exercises) {
            for (entry in exercise.entries) {
                workoutDays.add(dateToDateString(entry.date))
            }
        }

        val todayStr = getTodayString()
        var startOffset = 0
        if (todayStr !in workoutDays && todayStr !in restDays) {
            startOffset = 1
        }

        var streak = 0
        var offset = startOffset
        while (true) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val dateStr = "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            if (dateStr in workoutDays) {
                streak++
                offset++
            } else if (dateStr in restDays) {
                offset++
            } else {
                break
            }
        }
        return streak
    }

    private fun getTodayString(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun dateToDateString(date: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun formatDateLong(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dayNames = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val day = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val month = monthNames[cal.get(Calendar.MONTH)]
        val date = cal.get(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        return "$day, $date $month $year"
    }

    companion object {
        const val NOTIFICATION_ID = 2130
        const val WORK_TAG = "evening_review"
    }
}

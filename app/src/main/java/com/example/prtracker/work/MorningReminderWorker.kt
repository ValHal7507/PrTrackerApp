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
import com.example.prtracker.data.StorageManager
import java.util.Calendar

class MorningReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storageManager = StorageManager(applicationContext)
        val full = storageManager.loadFullData()
        val exercises = full.exercises

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
        val hasTrainedToday = exercises.any { exercise ->
            exercise.entries.any { dateToDateString(it.date) == todayStr }
        }

        if (hasTrainedToday) {
            return Result.success()
        }

        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val motivationIndex = (dayOfWeek - 1) % 5
        val motivationalLines = listOf(
            "Your PRs won't break themselves.",
            "Yesterday you said tomorrow. That's today.",
            "The grind doesn't stop — neither do you.",
            "One session away from a new personal record.",
            "Your future self is counting on you right now."
        )
        val motivationalLine = motivationalLines[motivationIndex]

        val streak = calculateStreak(exercises, full.restDays)
        val topExercises = getTopExercises(exercises)

        val expandedBody = buildString {
            appendLine(motivationalLine)
            appendLine()
            appendLine("CURRENT STREAK: $streak days \uD83D\uDD25")
            appendLine()
            for (ex in topExercises) {
                val pr = ex.entries.maxOfOrNull { it.value } ?: 0
                val unit = if (ex.type == "hold") "s" else " reps"
                appendLine("• ${ex.name} — PR: $pr$unit")
            }
            appendLine()
            append("Open PRTracker to log today's session.")
        }

        showNotification(motivationalLine, expandedBody.toString())

        return Result.success()
    }

    private fun showNotification(body: String, expandedBody: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "pr_tracker_morning")
            .setSmallIcon(com.example.prtracker.R.drawable.ic_goal_notification)
            .setContentTitle("TIME TO TRAIN \uD83D\uDCAA")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) { }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "pr_tracker_morning",
            "Morning Training Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily 8AM reminder to log your training session"
            enableVibration(true)
            enableLights(true)
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun getTopExercises(
        exercises: List<Exercise>
    ): List<Exercise> {
        val pinned = exercises.filter { it.isPinned }
        if (pinned.isNotEmpty()) {
            return pinned.take(3)
        }
        return exercises
            .filter { it.entries.isNotEmpty() }
            .sortedByDescending { ex ->
                ex.entries.maxOfOrNull { it.date } ?: 0L
            }
            .take(3)
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

    companion object {
        const val NOTIFICATION_ID = 8001
        const val WORK_TAG = "morning_reminder"
    }
}

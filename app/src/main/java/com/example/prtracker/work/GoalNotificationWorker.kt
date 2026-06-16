package com.example.prtracker.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prtracker.data.Goal
import com.example.prtracker.data.StorageManager
import java.util.Calendar

class GoalNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storageManager = StorageManager(applicationContext)
        val (exercises, goals) = storageManager.loadData()

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        for (goal in goals) {
            val exercise = exercises.find { it.id == goal.exerciseId } ?: continue
            val progress = calculateProgress(goal, exercises)
            if (progress < goal.targetValue) {
                showGoalNotification(goal, progress)
            }
        }

        return Result.success()
    }

    private fun calculateProgress(goal: Goal, exercises: List<com.example.prtracker.data.Exercise>): Int {
        val exercise = exercises.find { it.id == goal.exerciseId } ?: return 0
        val now = System.currentTimeMillis()
        return exercise.entries.filter { entry ->
            when (goal.period) {
                "daily" -> isSameDay(entry.date, now)
                "weekly" -> isSameWeek(entry.date, now)
                "monthly" -> isSameMonth(entry.date, now)
                else -> false
            }
        }.sumOf { it.value }
    }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply {
            timeInMillis = date1
            firstDayOfWeek = Calendar.MONDAY
        }
        val cal2 = Calendar.getInstance().apply {
            timeInMillis = date2
            firstDayOfWeek = Calendar.MONDAY
        }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "pr_tracker_goals",
            "Daily Goal Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun showGoalNotification(goal: Goal, progress: Int) {
        val remaining = goal.targetValue - progress
        val unit = if (goal.type == "reps") "reps" else "seconds"
        val periodLabel = goal.period.replaceFirstChar { it.uppercaseChar() } + " Goal"

        val notification = NotificationCompat.Builder(applicationContext, "pr_tracker_goals")
            .setSmallIcon(com.example.prtracker.R.drawable.ic_goal_notification)
            .setContentTitle("${goal.exerciseName} — $periodLabel")
            .setContentText("$progress / ${goal.targetValue} $unit — $remaining left to go!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$progress / ${goal.targetValue} $unit — $remaining left to go!"
                )
            )
            .setProgress(goal.targetValue, progress, false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(goal.id.hashCode(), notification)
        } catch (_: SecurityException) { }
    }
}

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
import com.example.prtracker.data.StorageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storageManager = StorageManager(applicationContext)
        val full = storageManager.loadFullData()
        val exercises = full.exercises
        val goals = full.goals
        val weightEntries = full.weightEntries

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        val now = System.currentTimeMillis()
        val weekStart = getWeekStart(now)
        val weekEnd = getWeekEnd(now)

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startStr = dateFormat.format(Date(weekStart))
        val endStr = dateFormat.format(Date(weekEnd))

        val newPRs = mutableListOf<NewPRInfo>()
        var totalVolume = 0
        var totalExercisesWithActivity = 0
        var mostImprovedExercise: String? = null
        var mostImprovedPercent = 0f

        for (exercise in exercises) {
            val allEntriesSorted = exercise.entries.sortedBy { it.date }
            val prBeforeWeek = allEntriesSorted
                .filter { it.date < weekStart }
                .maxOfOrNull { it.value } ?: 0

            val weekEntries = allEntriesSorted.filter { entry ->
                entry.date in weekStart..weekEnd
            }

            if (weekEntries.isNotEmpty()) {
                totalExercisesWithActivity++
                totalVolume += weekEntries.sumOf { it.value }

                val currentMax = allEntriesSorted.maxOfOrNull { it.value } ?: 0
                if (currentMax > prBeforeWeek && prBeforeWeek > 0) {
                    newPRs.add(NewPRInfo(exercise.name, prBeforeWeek, currentMax))
                    val pct = ((currentMax - prBeforeWeek).toFloat() / prBeforeWeek) * 100f
                    if (pct > mostImprovedPercent) {
                        mostImprovedPercent = pct
                        mostImprovedExercise = exercise.name
                    }
                } else if (currentMax > 0 && prBeforeWeek == 0) {
                    newPRs.add(NewPRInfo(exercise.name, 0, currentMax))
                    if (mostImprovedExercise == null) {
                        mostImprovedExercise = exercise.name
                        mostImprovedPercent = 100f
                    }
                }
            }
        }

        val goalsCompleted = mutableListOf<String>()
        val goalsInProgress = mutableListOf<Pair<String, Float>>()

        for (goal in goals) {
            val exercise = exercises.find { it.id == goal.exerciseId } ?: continue
            val weekProgress = exercise.entries
                .filter { it.date in weekStart..weekEnd }
                .sumOf { it.value }
            val pct = if (goal.targetValue > 0)
                (weekProgress.toFloat() / goal.targetValue).coerceIn(0f, 1f)
            else 0f

            if (pct >= 1f) {
                goalsCompleted.add(goal.exerciseName)
            } else if (weekProgress > 0) {
                goalsInProgress.add(goal.exerciseName to pct)
            }
        }

        val weekWeightEntries = weightEntries
            .filter { it.date in weekStart..weekEnd }
            .sortedBy { it.date }

        val weightChangeText = if (weekWeightEntries.size >= 2) {
            val first = weekWeightEntries.first().weight
            val last = weekWeightEntries.last().weight
            val diff = last - first
            val unit = full.settings.weightUnit
            "%.1f %s → %.1f %s (%+.1f %s this week)".format(
                first, unit, last, unit, diff, unit
            )
        } else {
            null
        }

        val collapsedBody = buildString {
            append("${newPRs.size} new PRs")
            append(" · ${goalsCompleted.size} goals hit")
            val unitLabel = if (exercises.any { it.type == "hold" }) "seconds" else "reps"
            append(" · $totalVolume total $unitLabel this week")
        }

        val expandedBody = buildString {
            appendLine("🏆 NEW PRs THIS WEEK")
            if (newPRs.isEmpty()) {
                appendLine("• None this week")
            } else {
                for (pr in newPRs) {
                    if (pr.oldValue == 0) {
                        appendLine("• ${pr.exerciseName}: ${pr.newValue} (first PR)")
                    } else {
                        appendLine("• ${pr.exerciseName}: ${pr.oldValue} → ${pr.newValue}")
                    }
                }
            }
            appendLine()

            appendLine("🎯 GOALS")
            if (goalsCompleted.isEmpty() && goalsInProgress.isEmpty()) {
                appendLine("• None this week")
            } else {
                for (g in goalsCompleted) {
                    appendLine("• $g: COMPLETED ✓")
                }
                for ((name, pct) in goalsInProgress) {
                    appendLine("• $name: ${(pct * 100).toInt()}% — keep going!")
                }
            }
            appendLine()

            val volumeUnit = if (exercises.any { it.type == "hold" }) "seconds" else "reps"
            appendLine("💪 TOTAL VOLUME")
            appendLine("• $totalVolume $volumeUnit logged across $totalExercisesWithActivity exercises")
            appendLine()

            appendLine("⚖️ WEIGHT")
            if (weightChangeText != null) {
                appendLine("• $weightChangeText")
            } else {
                appendLine("• No weight logged this week")
            }
            appendLine()

            appendLine("⭐ MOST IMPROVED")
            if (mostImprovedExercise != null) {
                appendLine("• $mostImprovedExercise: +${"%.0f".format(mostImprovedPercent)}% improvement")
            } else {
                appendLine("• No PR improvements this week")
            }
        }

        showNotification(
            title = "Weekly Summary — $startStr to $endStr",
            body = collapsedBody,
            expandedBody = expandedBody.toString()
        )

        return Result.success()
    }

    private fun showNotification(title: String, body: String, expandedBody: String) {
        val notification = NotificationCompat.Builder(applicationContext, "pr_tracker_weekly")
            .setSmallIcon(com.example.prtracker.R.drawable.ic_goal_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(WEEKLY_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) { }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "pr_tracker_weekly",
            "Weekly Summary",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val WEEKLY_NOTIFICATION_ID = 9001
        const val WORK_TAG = "weekly_summary"
    }

    private data class NewPRInfo(
        val exerciseName: String,
        val oldValue: Int,
        val newValue: Int
    )

    private fun getWeekStart(now: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getWeekEnd(now: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
}

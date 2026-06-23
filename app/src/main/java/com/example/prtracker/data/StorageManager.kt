package com.example.prtracker.data

import android.content.Context
import androidx.compose.runtime.Immutable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

@Immutable
data class StorageData(
    val exercises: List<Exercise> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val weightEntries: List<WeightEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val restDays: List<String> = emptyList(),
    val runEntries: List<RunEntry> = emptyList(),
    val runningPRs: RunningPRs = RunningPRs(),
    val workoutPresets: List<WorkoutPreset> = emptyList(),
    val workoutSession: WorkoutSession? = null,
    val workoutHistory: List<WorkoutSession> = emptyList(),
    val totalXp: Long = 0L,
    val xpBootstrapped: Boolean = false
)

class StorageManager(private val context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "prs.json")

    fun loadData(): Pair<List<Exercise>, List<Goal>> {
        val full = loadFullData()
        return Pair(full.exercises, full.goals)
    }

    fun loadFullData(): StorageData {
        return try {
            if (file.exists()) {
                val json = file.readText()
                try {
                    val type = object : TypeToken<StorageData>() {}.type
                    val data: StorageData = gson.fromJson(json, type)
                    val sorted = data.exercises.sortedWith(
                        compareByDescending<Exercise> { it.isPinned }.thenBy { it.sortOrder }
                    )
                    data.copy(exercises = sorted)
                } catch (_: Exception) {
                    val type = object : TypeToken<List<Exercise>>() {}.type
                    val exercises: List<Exercise> = gson.fromJson(json, type) ?: emptyList()
                    val sorted = exercises.sortedWith(
                        compareByDescending<Exercise> { it.isPinned }.thenBy { it.sortOrder }
                    )
                    StorageData(exercises = sorted)
                }
            } else {
                StorageData()
            }
        } catch (e: Exception) {
            StorageData()
        }
    }

    fun saveData(exercises: List<Exercise>, goals: List<Goal>) {
        val existing = loadFullData()
        saveFullData(exercises, goals, existing.weightEntries, existing.settings, existing.restDays, existing.runEntries, existing.runningPRs, existing.workoutPresets, existing.workoutSession, existing.workoutHistory, existing.totalXp, existing.xpBootstrapped)
    }

    fun saveFullData(
        exercises: List<Exercise>,
        goals: List<Goal>,
        weightEntries: List<WeightEntry>,
        settings: AppSettings,
        restDays: List<String> = emptyList(),
        runEntries: List<RunEntry> = emptyList(),
        runningPRs: RunningPRs = RunningPRs(),
        workoutPresets: List<WorkoutPreset> = emptyList(),
        workoutSession: WorkoutSession? = null,
        workoutHistory: List<WorkoutSession> = emptyList(),
        totalXp: Long = 0L,
        xpBootstrapped: Boolean = false
    ) {
        try {
            val data = StorageData(exercises, goals, weightEntries, settings, restDays, runEntries, runningPRs, workoutPresets, workoutSession, workoutHistory, totalXp, xpBootstrapped)
            val json = gson.toJson(data)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadExercises(): List<Exercise> {
        return loadData().first
    }

    fun saveExercises(exercises: List<Exercise>) {
        val full = loadFullData()
        saveFullData(exercises, full.goals, full.weightEntries, full.settings, full.restDays, full.runEntries, full.runningPRs, full.workoutPresets, full.workoutSession, full.workoutHistory, full.totalXp, full.xpBootstrapped)
    }
}

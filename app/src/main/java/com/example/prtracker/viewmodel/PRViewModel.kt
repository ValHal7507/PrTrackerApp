package com.example.prtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prtracker.data.AppSettings
import com.example.prtracker.data.AppearanceSettings
import com.example.prtracker.data.AppTheme
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.Goal
import com.example.prtracker.data.LeverageTelemetry
import com.example.prtracker.data.PREntry
import com.google.gson.Gson
import com.example.prtracker.data.RsiEngine
import com.example.prtracker.data.RunEntry
import com.example.prtracker.data.RunningPREngine
import com.example.prtracker.data.RunningPRs
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.StorageData
import com.example.prtracker.data.StorageManager
import com.example.prtracker.data.TierEvaluator
import com.example.prtracker.data.TierResult
import com.example.prtracker.data.WeightEntry
import com.example.prtracker.data.SessionExerciseProgress
import com.example.prtracker.data.SessionSetEntry
import com.example.prtracker.data.WorkoutPreset
import com.example.prtracker.data.WorkoutSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class PRViewModel(application: Application) : AndroidViewModel(application) {
    private val storageManager = StorageManager(application)
    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals

    private val _weightEntries = MutableStateFlow<List<WeightEntry>>(emptyList())
    val weightEntries: StateFlow<List<WeightEntry>> = _weightEntries

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings

    private val _restDays = MutableStateFlow<List<String>>(emptyList())
    val restDays: StateFlow<List<String>> = _restDays

    private val _hapticEvent = MutableSharedFlow<Unit>(replay = 0)
    val hapticEvent: SharedFlow<Unit> = _hapticEvent

    private val _allTelemetry = MutableStateFlow<Map<String, List<LeverageTelemetry>>>(emptyMap())
    val allTelemetry: StateFlow<Map<String, List<LeverageTelemetry>>> = _allTelemetry

    private val _isLoadingTelemetry = MutableStateFlow(false)
    val isLoadingTelemetry: StateFlow<Boolean> = _isLoadingTelemetry

    private val _runEntries = MutableStateFlow<List<RunEntry>>(emptyList())
    val runEntries: StateFlow<List<RunEntry>> = _runEntries

    private val _runningPRs = MutableStateFlow(RunningPRs())
    val runningPRs: StateFlow<RunningPRs> = _runningPRs

    private val _workoutPresets = MutableStateFlow<List<WorkoutPreset>>(emptyList())
    val workoutPresets: StateFlow<List<WorkoutPreset>> = _workoutPresets

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession: StateFlow<WorkoutSession?> = _activeSession

    private val _workoutHistory = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workoutHistory: StateFlow<List<WorkoutSession>> = _workoutHistory

    private val _pendingImportJson = MutableStateFlow<String?>(null)
    val pendingImportJson: StateFlow<String?> = _pendingImportJson

    fun setPendingImportJson(json: String?) {
        _pendingImportJson.value = json
    }

    fun consumePendingImportJson(): String? {
        val json = _pendingImportJson.value
        _pendingImportJson.value = null
        return json
    }

    val currentStreak: StateFlow<Int> = combine(_exercises, _restDays) { exercises, restDays ->
        calculateStreak(exercises, restDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val pinnedExercises: StateFlow<List<Exercise>> = _exercises.map { list ->
        list.filter { it.isPinned }.sortedBy { it.sortOrder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val unpinnedExercises: StateFlow<List<Exercise>> = _exercises.map { list ->
        list.filter { !it.isPinned }.sortedBy { it.sortOrder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val tierResult: StateFlow<TierResult> = _exercises
        .map { TierEvaluator.evaluate(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TierEvaluator.evaluate(emptyList())
        )

    fun toggleHaptic(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(hapticEnabled = enabled)
        saveData()
    }

    fun toggleSpeeches(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(speechesEnabled = enabled)
        saveData()
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val full = storageManager.loadFullData()
            _exercises.value = full.exercises
            _goals.value = full.goals
            _weightEntries.value = full.weightEntries
            _appSettings.value = full.settings
            _restDays.value = full.restDays
            _runEntries.value = full.runEntries
            _runningPRs.value = full.runningPRs
            _workoutPresets.value = full.workoutPresets
            _activeSession.value = full.workoutSession
            _workoutHistory.value = full.workoutHistory
            SoundEngine.volume = full.settings.soundVolume
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            storageManager.saveFullData(
                _exercises.value,
                _goals.value,
                _weightEntries.value,
                _appSettings.value,
                _restDays.value,
                _runEntries.value,
                _runningPRs.value,
                _workoutPresets.value,
                _activeSession.value,
                _workoutHistory.value
            )
        }
    }

    fun addExercise(exercise: Exercise) {
        val current = _exercises.value.toMutableList()
        val maxOrder = current.filter { it.isPinned == exercise.isPinned }.maxOfOrNull { it.sortOrder } ?: -1
        current.add(exercise.copy(sortOrder = maxOrder + 1))
        _exercises.value = current
        saveData()
    }

    fun logEntry(exerciseId: String, entry: PREntry) {
        var updatedExercise: Exercise? = null
        _exercises.value = _exercises.value.mapNotNull { exercise ->
            if (exercise.id == exerciseId) {
                updatedExercise = exercise.copy(entries = exercise.entries + entry)
                null
            } else {
                exercise
            }
        }.toMutableList().also { list ->
            if (updatedExercise != null) {
                list.add(0, updatedExercise!!)
            }
        }
        saveData()
    }

    fun deleteExercise(exerciseId: String) {
        _exercises.value = _exercises.value.filter { it.id != exerciseId }
        _goals.value = _goals.value.filter { it.exerciseId != exerciseId }
        saveData()
    }

    fun deleteEntry(exerciseId: String, entryId: String) {
        _exercises.value = _exercises.value.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(entries = exercise.entries.filter { it.id != entryId })
            } else {
                exercise
            }
        }
        saveData()
    }

    fun clearAllData() {
        _exercises.value = emptyList()
        _goals.value = emptyList()
        _weightEntries.value = emptyList()
        _appSettings.value = AppSettings()
        _restDays.value = emptyList()
        _runEntries.value = emptyList()
        _runningPRs.value = RunningPRs()
        _workoutPresets.value = emptyList()
        _activeSession.value = null
        _workoutHistory.value = emptyList()
        _allTelemetry.value = emptyMap()
        saveData()
    }

    fun addRunEntry(entry: RunEntry) {
        _runEntries.value = listOf(entry) + _runEntries.value
        _runningPRs.value = RunningPREngine.computePRs(_runEntries.value)
        saveData()
    }

    fun deleteRunEntry(entryId: String) {
        _runEntries.value = _runEntries.value.filter { it.id != entryId }
        _runningPRs.value = RunningPREngine.computePRs(_runEntries.value)
        saveData()
    }

    fun isNewRunPR(entry: RunEntry): Boolean {
        return RunningPREngine.isNewPR(_runningPRs.value, entry)
    }

    fun savePresets() {
        saveData()
    }

    fun addPreset(preset: WorkoutPreset) {
        _workoutPresets.value = _workoutPresets.value + preset
        saveData()
    }

    fun updatePreset(updated: WorkoutPreset) {
        _workoutPresets.value = _workoutPresets.value.map { if (it.id == updated.id) updated else it }
        saveData()
    }

    fun deletePreset(id: String) {
        _workoutPresets.value = _workoutPresets.value.filter { it.id != id }
        saveData()
    }

    fun togglePresetPin(id: String) {
        _workoutPresets.value = _workoutPresets.value.map { preset ->
            if (preset.id == id) preset.copy(isPinned = !preset.isPinned) else preset
        }
        saveData()
    }

    fun reorderPresets(from: Int, to: Int) {
        val sorted = _workoutPresets.value.sortedWith(
            compareByDescending<WorkoutPreset> { it.isPinned }.thenBy { it.sortOrder }.thenByDescending { it.createdAt }
        )
        val mutable = sorted.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        val rewritten = mutable.mapIndexed { index, preset -> preset.copy(sortOrder = index) }
        _workoutPresets.value = rewritten
        saveData()
    }

    fun startWorkout(preset: WorkoutPreset) {
        val exercises = preset.exercises.map { pe ->
            SessionExerciseProgress(
                exerciseName = pe.exerciseName,
                targetValue = if (pe.targetHoldSeconds > 0) pe.targetHoldSeconds else pe.targetReps,
                isHold = pe.targetHoldSeconds > 0,
                totalSets = pe.sets,
                completedSets = emptyList()
            )
        }
        _activeSession.value = WorkoutSession(
            id = java.util.UUID.randomUUID().toString(),
            presetId = preset.id,
            presetName = preset.name,
            startedAt = System.currentTimeMillis(),
            pausedDurationMs = 0L,
            pausedSinceMs = 0L,
            isPaused = false,
            isCompleted = false,
            exercises = exercises
        )
        saveSessionData()
    }

    fun completeSetInSession(exerciseIndex: Int, value: Int) {
        val session = _activeSession.value ?: return
        if (exerciseIndex < 0 || exerciseIndex >= session.exercises.size) return
        val ex = session.exercises[exerciseIndex]
        val newEntry = SessionSetEntry(value = value)
        val updatedEx = ex.copy(completedSets = ex.completedSets + newEntry)
        val updatedExercises = session.exercises.toMutableList().also { it[exerciseIndex] = updatedEx }
        _activeSession.value = session.copy(exercises = updatedExercises)
        saveSessionData()
    }

    fun togglePauseWorkout() {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        _activeSession.value = if (session.isPaused) {
            val extraPause = now - session.pausedSinceMs
            session.copy(
                isPaused = false,
                pausedDurationMs = session.pausedDurationMs + extraPause,
                pausedSinceMs = 0L
            )
        } else {
            session.copy(isPaused = true, pausedSinceMs = now)
        }
        saveSessionData()
    }

    fun finishWorkout() {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        var updatedExercises = _exercises.value.toMutableList()

        for (exProgress in session.exercises) {
            if (exProgress.completedSets.isEmpty()) continue
            val matchingExercise = updatedExercises.find { it.name == exProgress.exerciseName }
            if (matchingExercise != null) {
                val setEntries = exProgress.completedSets.map { setEntry ->
                    PREntry(
                        id = java.util.UUID.randomUUID().toString(),
                        value = setEntry.value,
                        date = now,
                        note = "From workout: ${session.presetName}"
                    )
                }
                updatedExercises = updatedExercises.map { ex ->
                    if (ex.id == matchingExercise.id) {
                        ex.copy(entries = ex.entries + setEntries)
                    } else {
                        ex
                    }
                }.toMutableList()
                val movedExercise = updatedExercises.find { it.id == matchingExercise.id } ?: continue
                updatedExercises.removeAll { it.id == movedExercise.id }
                updatedExercises.add(0, movedExercise)
            }
        }

        _exercises.value = updatedExercises
        val completedSession = session.copy(isCompleted = true, isPaused = false)
        _activeSession.value = completedSession
        _workoutHistory.value = listOf(completedSession) + _workoutHistory.value
        saveSessionData()
        saveData()
    }

    fun discardWorkout() {
        _activeSession.value = null
        saveSessionData()
    }

    fun autoPauseWorkout() {
        val session = _activeSession.value ?: return
        if (session.isCompleted || session.isPaused) return
        _activeSession.value = session.copy(isPaused = true, pausedSinceMs = System.currentTimeMillis())
        saveSessionData()
    }

    fun deleteWorkoutHistoryEntry(sessionId: String) {
        _workoutHistory.value = _workoutHistory.value.filter { it.id != sessionId }
        saveData()
    }

    private fun saveSessionData() {
        viewModelScope.launch {
            val full = storageManager.loadFullData()
            storageManager.saveFullData(
                exercises = _exercises.value,
                goals = _goals.value,
                weightEntries = _weightEntries.value,
                settings = _appSettings.value,
                restDays = _restDays.value,
                runEntries = _runEntries.value,
                runningPRs = _runningPRs.value,
                workoutPresets = _workoutPresets.value,
                workoutSession = _activeSession.value,
                workoutHistory = _workoutHistory.value
            )
        }
    }

    fun loadAllTelemetry() {
        _isLoadingTelemetry.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val result = _exercises.value.associate { exercise ->
                exercise.id to RsiEngine.buildTelemetry(
                    exercise, _weightEntries.value, _appSettings.value.weightUnit
                )
            }
            withContext(Dispatchers.Main) {
                _allTelemetry.value = result
                _isLoadingTelemetry.value = false
            }
        }
    }

    fun clearAllTelemetry() {
        _allTelemetry.value = emptyMap()
        _isLoadingTelemetry.value = false
    }

    fun getCurrentRsi(exerciseId: String): Int? {
        return _allTelemetry.value[exerciseId]?.lastOrNull()?.rsiScore
    }

    fun getRsiDelta(exerciseId: String): Int? {
        val telemetry = _allTelemetry.value[exerciseId] ?: return null
        if (telemetry.size < 2) return null
        val latest = telemetry.last()
        val thirtyDaysAgo = thirtyDaysBeforeDateString(latest.dateString)
        val oneMonthAgo = telemetry.dropLast(1).lastOrNull { it.dateString <= thirtyDaysAgo }
            ?: telemetry.dropLast(1).firstOrNull()
            ?: return null
        return latest.rsiScore - oneMonthAgo.rsiScore
    }

    fun getRsiStatusLabel(delta: Int?): String {
        return when {
            delta == null || delta == 0 -> "STABLE"
            delta > 0 -> "OVERLOAD"
            else -> "DELOAD"
        }
    }

    private fun thirtyDaysBeforeDateString(dateString: String): String {
        val parts = dateString.split("-")
        val cal = Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -30)
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun setGoal(exerciseId: String, goal: Int?) {
        _exercises.value = _exercises.value.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(goal = goal)
            } else {
                exercise
            }
        }
        saveData()
    }

    fun getGoalProgress(exerciseId: String): Float {
        val exercise = _exercises.value.find { it.id == exerciseId } ?: return 0f
        val goal = exercise.goal ?: return 0f
        if (goal <= 0) return 0f
        val currentPR = exercise.entries.maxOfOrNull { it.value } ?: 0
        return currentPR.toFloat() / goal.toFloat()
    }

    fun isGoalReached(exerciseId: String): Boolean {
        val exercise = _exercises.value.find { it.id == exerciseId } ?: return false
        val goal = exercise.goal ?: return false
        if (goal <= 0) return false
        val currentPR = exercise.entries.maxOfOrNull { it.value } ?: 0
        return currentPR >= goal
    }

    fun isNewPR(exerciseId: String, value: Int): Boolean {
        val exercise = _exercises.value.find { it.id == exerciseId } ?: return false
        return exercise.entries.isEmpty() || value > (exercise.entries.maxOfOrNull { it.value } ?: 0)
    }

    fun getCurrentPR(exerciseId: String): Int {
        val exercise = _exercises.value.find { it.id == exerciseId } ?: return 0
        return exercise.entries.maxOfOrNull { it.value } ?: 0
    }

    fun getExerciseById(exerciseId: String): Exercise? {
        return _exercises.value.find { it.id == exerciseId }
    }

    fun togglePin(exerciseId: String) {
        _exercises.value = _exercises.value.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(isPinned = !exercise.isPinned)
            } else {
                exercise
            }
        }
        recalculateSortOrders()
        saveData()
    }

    fun swapExercises(id1: String, id2: String) {
        val e1 = _exercises.value.find { it.id == id1 } ?: return
        val e2 = _exercises.value.find { it.id == id2 } ?: return
        val s1 = e1.sortOrder
        val s2 = e2.sortOrder
        _exercises.value = _exercises.value.map { exercise ->
            when (exercise.id) {
                id1 -> exercise.copy(sortOrder = s2)
                id2 -> exercise.copy(sortOrder = s1)
                else -> exercise
            }
        }
        saveData()
    }

    private fun recalculateSortOrders() {
        val current = _exercises.value
        val pinned = current.filter { it.isPinned }.sortedBy { it.sortOrder }
        val unpinned = current.filter { !it.isPinned }.sortedBy { it.sortOrder }
        val updatedPinned = pinned.mapIndexed { index, exercise -> exercise.copy(sortOrder = index) }
        val updatedUnpinned = unpinned.mapIndexed { index, exercise -> exercise.copy(sortOrder = index) }
        _exercises.value = updatedPinned + updatedUnpinned
    }

    fun addGoal(goal: Goal) {
        _goals.value = _goals.value + goal
        saveData()
    }

    fun deleteGoal(goalId: String) {
        _goals.value = _goals.value.filter { it.id != goalId }
        saveData()
    }

    fun getProgressForGoal(goal: Goal): Int {
        val exercise = _exercises.value.find { it.id == goal.exerciseId } ?: return 0
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

    fun getProgressPercent(goal: Goal): Float {
        if (goal.targetValue <= 0) return 0f
        return (getProgressForGoal(goal).toFloat() / goal.targetValue).coerceIn(0f, 1f)
    }

    fun addWeightEntry(entry: WeightEntry) {
        _weightEntries.value = _weightEntries.value + entry
        saveData()
    }

    fun deleteWeightEntry(id: String) {
        _weightEntries.value = _weightEntries.value.filter { it.id != id }
        saveData()
    }

    fun getCurrentWeight(): Float? {
        return _weightEntries.value.maxByOrNull { it.date }?.weight
    }

    fun getLowestWeight(): Float? {
        return _weightEntries.value.minOfOrNull { it.weight }
    }

    fun getHighestWeight(): Float? {
        return _weightEntries.value.maxOfOrNull { it.weight }
    }

    fun getAverageWeight(): Float? {
        val entries = _weightEntries.value
        if (entries.isEmpty()) return null
        return entries.map { it.weight }.sum() / entries.size
    }

    fun setWeightUnit(unit: String) {
        val current = _appSettings.value
        if (current.weightUnit != unit) {
            val conversionFactor = if (unit == "kg") 1f / 2.20462f else 2.20462f
            _weightEntries.value = _weightEntries.value.map { entry ->
                entry.copy(weight = entry.weight * conversionFactor)
            }
            val target = current.targetWeight?.let { it * conversionFactor }
            _appSettings.value = current.copy(weightUnit = unit, targetWeight = target)
            saveData()
        }
    }

    fun setTargetWeight(weight: Float?) {
        _appSettings.value = _appSettings.value.copy(targetWeight = weight)
        saveData()
    }

    fun getCalendarDayValue(exercise: Exercise, dateString: String): Int {
        val cal = Calendar.getInstance()
        val entriesOnDay = exercise.entries.filter { entry ->
            cal.timeInMillis = entry.date
            val entryDate = "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            entryDate == dateString
        }
        if (entriesOnDay.isEmpty()) return 0
        return if (_appSettings.value.calendarDayViewMode == "sum") {
            entriesOnDay.sumOf { it.value }
        } else {
            entriesOnDay.maxOf { it.value }
        }
    }

    fun setCalendarDayViewMode(mode: String) {
        _appSettings.value = _appSettings.value.copy(calendarDayViewMode = mode)
        saveData()
    }

    fun updateAppearance(appearance: AppearanceSettings) {
        _appSettings.value = _appSettings.value.copy(appearance = appearance)
        saveData()
    }

    fun applyTheme(theme: AppTheme) {
        _appSettings.value = _appSettings.value.copy(appearance = theme.settings)
        saveData()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(soundEnabled = enabled)
        saveData()
    }

    fun setSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        SoundEngine.volume = clamped
        _appSettings.value = _appSettings.value.copy(soundVolume = clamped)
        saveData()
    }

    fun setMorningReminderTime(hour: Int, minute: Int) {
        _appSettings.value = _appSettings.value.copy(
            morningReminderHour = hour,
            morningReminderMinute = minute
        )
        saveData()
    }

    fun setEveningReviewTime(hour: Int, minute: Int) {
        _appSettings.value = _appSettings.value.copy(
            eveningReviewHour = hour,
            eveningReviewMinute = minute
        )
        saveData()
    }

    fun getTimeRemaining(goal: Goal): String {
        val now = Calendar.getInstance()
        return when (goal.period) {
            "daily" -> {
                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                val diffMs = endOfDay.timeInMillis - now.timeInMillis
                val hours = diffMs / (1000 * 60 * 60)
                val minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60)
                "Resets in ${hours}h ${minutes}m"
            }
            "weekly" -> {
                val endOfWeek = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                val diffMs = endOfWeek.timeInMillis - now.timeInMillis
                val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                "${days} days left this week"
            }
            "monthly" -> {
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                val diffMs = endOfMonth.timeInMillis - now.timeInMillis
                val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                "${days} days left in ${monthNames[now.get(Calendar.MONTH)]}"
            }
            else -> ""
        }
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

    fun toggleTodayAsRestDay() {
        val today = System.currentTimeMillis().toDateString()
        val current = _restDays.value.toMutableList()
        if (today in current) {
            current.remove(today)
        } else {
            current.add(today)
        }
        _restDays.value = current
        saveData()
        viewModelScope.launch { _hapticEvent.emit(Unit) }
    }

    private fun calculateStreak(exercises: List<Exercise>, restDays: List<String>): Int {
        val workoutDays: Set<String> = exercises
            .flatMap { it.entries }
            .map { it.date.toDateString() }
            .toHashSet()
        val restDaySet: Set<String> = restDays.toHashSet()
        var streak = 0
        var offset = 0
        val today = System.currentTimeMillis().toDateString()
        if (today !in workoutDays && today !in restDaySet) {
            offset = -1
        }
        while (true) {
            val day = offsetDayString(offset)
            when {
                day in workoutDays -> { streak++; offset-- }
                day in restDaySet -> { offset-- }
                else -> break
            }
            if (offset < -3650) break
        }
        return streak
    }

    private fun Long.toDateString(): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = this
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun offsetDayString(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    enum class SyncMode { REPLACE, MERGE }

    fun generateExportJson(): String {
        val fullData = StorageData(
            exercises = _exercises.value,
            goals = _goals.value,
            weightEntries = _weightEntries.value,
            settings = _appSettings.value,
            restDays = _restDays.value,
            runEntries = _runEntries.value,
            runningPRs = _runningPRs.value,
            workoutPresets = _workoutPresets.value,
            workoutSession = _activeSession.value,
            workoutHistory = _workoutHistory.value
        )
        return Gson().toJson(fullData)
    }

    fun importSyncData(data: StorageData, mode: SyncMode) {
        when (mode) {
            SyncMode.REPLACE -> {
                _exercises.value = data.exercises
                _goals.value = data.goals
                _weightEntries.value = data.weightEntries
                _appSettings.value = data.settings
                _restDays.value = data.restDays
                _runEntries.value = data.runEntries
                _runningPRs.value = data.runningPRs
                _workoutPresets.value = data.workoutPresets
                _workoutHistory.value = data.workoutHistory
            }
            SyncMode.MERGE -> {
                val localExercises = _exercises.value
                val localGoals = _goals.value
                val localWeightEntries = _weightEntries.value
                val localSettings = _appSettings.value

                val mergedExercises = mergeExercises(localExercises, data.exercises)
                val mergedGoals = mergeGoals(localGoals, data.goals)
                val mergedWeightEntries = mergeWeightEntries(localWeightEntries, data.weightEntries)

                val localRestDays = _restDays.value.toSet()
                val incomingRestDays = data.restDays.toSet()
                val mergedRestDays = (localRestDays + incomingRestDays).toList()

                val localPresetIds = _workoutPresets.value.map { it.id }.toSet()
                val newPresets = data.workoutPresets.filter { it.id !in localPresetIds }
                _workoutPresets.value = _workoutPresets.value + newPresets

                val localHistoryIds = _workoutHistory.value.map { it.id }.toSet()
                val newHistory = data.workoutHistory.filter { it.id !in localHistoryIds }
                _workoutHistory.value = _workoutHistory.value + newHistory

                val localRunIds = _runEntries.value.map { it.id }.toSet()
                val newRuns = data.runEntries.filter { it.id !in localRunIds }
                _runEntries.value = (_runEntries.value + newRuns).sortedByDescending { it.date }
                _runningPRs.value = RunningPREngine.computePRs(_runEntries.value)

                _exercises.value = mergedExercises
                _goals.value = mergedGoals
                _weightEntries.value = mergedWeightEntries
                _appSettings.value = localSettings
                _restDays.value = mergedRestDays
            }
        }
        saveData()
    }

    private fun mergeExercises(local: List<Exercise>, incoming: List<Exercise>): List<Exercise> {
        val localMap = local.associateBy { it.id }.toMutableMap()
        for (inc in incoming) {
            val existing = localMap[inc.id]
            if (existing != null) {
                val allEntries = (existing.entries + inc.entries).distinctBy { it.id }
                val higherGoal = maxOf(existing.goal ?: 0, inc.goal ?: 0).let { if (it == 0) null else it }
                localMap[inc.id] = existing.copy(
                    entries = allEntries,
                    goal = higherGoal
                )
            } else {
                val sameName = localMap.values.find { it.name == inc.name }
                if (sameName != null) {
                    val allEntries = (sameName.entries + inc.entries).distinctBy { it.id }
                    val higherGoal = maxOf(sameName.goal ?: 0, inc.goal ?: 0).let { if (it == 0) null else it }
                    localMap[sameName.id] = sameName.copy(
                        entries = allEntries,
                        goal = higherGoal
                    )
                } else {
                    localMap[inc.id] = inc.copy(
                        isPinned = false,
                        sortOrder = (localMap.values.maxOfOrNull { it.sortOrder } ?: -1) + 1
                    )
                }
            }
        }
        return localMap.values.toList()
    }

    private fun mergeGoals(local: List<Goal>, incoming: List<Goal>): List<Goal> {
        val localIds = local.map { it.id }.toSet()
        val newGoals = incoming.filter { it.id !in localIds }
        return local + newGoals
    }

    private fun mergeWeightEntries(local: List<WeightEntry>, incoming: List<WeightEntry>): List<WeightEntry> {
        val localIds = local.map { it.id }.toSet()
        val newEntries = incoming.filter { it.id !in localIds }
        return (local + newEntries).sortedByDescending { it.date }
    }
}

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
    val xpBootstrapped: Boolean = false,
    val potionInventory: Map<String, Int> = emptyMap(),
    val lastPotionEarnedTimestamp: Long = 0L,
    val miniGameHighScore: Int = 0,
    val petInventory: List<Pet> = emptyList(),
    val totalRolls: Int = 0,
    val rollsSinceEpicOrAbove: Int = 0,
    val rollsSinceLegendary: Int = 0,
    val rollsSinceMythical: Int = 0,
    val lastDiceRollTimestamp: Long = 0L,
    val coins: Long = 0L,
    val petUpgrades: Map<String, Int> = emptyMap(),
    val equippedPetIds: List<String> = emptyList()
)

@Immutable
data class PetStorageData(
    val petInventory: List<Pet> = emptyList(),
    val totalRolls: Int = 0,
    val rollsSinceEpicOrAbove: Int = 0,
    val rollsSinceLegendary: Int = 0,
    val rollsSinceMythical: Int = 0,
    val lastDiceRollTimestamp: Long = 0L,
    val coins: Long = 0L,
    val petUpgrades: Map<String, Int> = emptyMap(),
    val equippedPetIds: List<String> = emptyList()
)

class StorageManager(private val context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "prs.json")
    private val petFile = File(context.filesDir, "pets.json")

    fun loadData(): Pair<List<Exercise>, List<Goal>> {
        val full = loadFullData()
        return Pair(full.exercises, full.goals)
    }

    fun loadPetData(): PetStorageData {
        return try {
            if (petFile.exists()) {
                val json = petFile.readText()
                gson.fromJson(json, PetStorageData::class.java) ?: PetStorageData()
            } else {
                PetStorageData()
            }
        } catch (e: Exception) {
            PetStorageData()
        }
    }

    fun savePetData(data: PetStorageData) {
        try {
            val json = gson.toJson(data)
            petFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun migrateIfNeeded() {
        if (petFile.exists()) return
        if (!file.exists()) {
            savePetData(PetStorageData())
            return
        }
        try {
            val json = file.readText()
            val type = object : TypeToken<StorageData>() {}.type
            val data: StorageData = gson.fromJson(json, type) ?: return
            if (data.petInventory.isEmpty() && data.totalRolls == 0 && data.coins == 0L) {
                savePetData(PetStorageData())
                return
            }
            val petData = PetStorageData(
                petInventory = data.petInventory,
                totalRolls = data.totalRolls,
                rollsSinceEpicOrAbove = data.rollsSinceEpicOrAbove,
                rollsSinceLegendary = data.rollsSinceLegendary,
                rollsSinceMythical = data.rollsSinceMythical,
                lastDiceRollTimestamp = data.lastDiceRollTimestamp,
                coins = data.coins,
                petUpgrades = data.petUpgrades,
                equippedPetIds = data.equippedPetIds
            )
            savePetData(petData)
            val cleaned = data.copy(
                petInventory = emptyList(),
                totalRolls = 0,
                rollsSinceEpicOrAbove = 0,
                rollsSinceLegendary = 0,
                rollsSinceMythical = 0,
                lastDiceRollTimestamp = 0L,
                coins = 0L,
                petUpgrades = emptyMap(),
                equippedPetIds = emptyList()
            )
            file.writeText(gson.toJson(cleaned))
        } catch (e: Exception) {
            e.printStackTrace()
            savePetData(PetStorageData())
        }
    }

    fun loadFullData(): StorageData {
        return try {
            migrateIfNeeded()
            val petData = loadPetData()
            if (file.exists()) {
                val json = file.readText()
                try {
                    val type = object : TypeToken<StorageData>() {}.type
                    val data: StorageData = gson.fromJson(json, type)
                    val sorted = data.exercises.sortedWith(
                        compareByDescending<Exercise> { it.isPinned }.thenBy { it.sortOrder }
                    )
                    data.copy(
                        exercises = sorted,
                        petInventory = petData.petInventory,
                        totalRolls = petData.totalRolls,
                        rollsSinceEpicOrAbove = petData.rollsSinceEpicOrAbove,
                        rollsSinceLegendary = petData.rollsSinceLegendary,
                        rollsSinceMythical = petData.rollsSinceMythical,
                        lastDiceRollTimestamp = petData.lastDiceRollTimestamp,
                        coins = petData.coins,
                        petUpgrades = petData.petUpgrades,
                        equippedPetIds = petData.equippedPetIds
                    )
                } catch (_: Exception) {
                    val type = object : TypeToken<List<Exercise>>() {}.type
                    val exercises: List<Exercise> = gson.fromJson(json, type) ?: emptyList()
                    val sorted = exercises.sortedWith(
                        compareByDescending<Exercise> { it.isPinned }.thenBy { it.sortOrder }
                    )
                    StorageData(
                        exercises = sorted,
                        petInventory = petData.petInventory,
                        totalRolls = petData.totalRolls,
                        rollsSinceEpicOrAbove = petData.rollsSinceEpicOrAbove,
                        rollsSinceLegendary = petData.rollsSinceLegendary,
                        rollsSinceMythical = petData.rollsSinceMythical,
                        lastDiceRollTimestamp = petData.lastDiceRollTimestamp,
                        coins = petData.coins,
                        petUpgrades = petData.petUpgrades,
                        equippedPetIds = petData.equippedPetIds
                    )
                }
            } else {
                StorageData(
                    petInventory = petData.petInventory,
                    totalRolls = petData.totalRolls,
                    rollsSinceEpicOrAbove = petData.rollsSinceEpicOrAbove,
                    rollsSinceLegendary = petData.rollsSinceLegendary,
                    rollsSinceMythical = petData.rollsSinceMythical,
                    lastDiceRollTimestamp = petData.lastDiceRollTimestamp,
                    coins = petData.coins,
                    petUpgrades = petData.petUpgrades,
                    equippedPetIds = petData.equippedPetIds
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            StorageData()
        }
    }

    fun saveData(exercises: List<Exercise>, goals: List<Goal>) {
        val existing = loadFullData()
        saveFullData(exercises, goals, existing.weightEntries, existing.settings, existing.restDays, existing.runEntries, existing.runningPRs, existing.workoutPresets, existing.workoutSession, existing.workoutHistory, existing.totalXp, existing.xpBootstrapped, existing.potionInventory, existing.lastPotionEarnedTimestamp, existing.miniGameHighScore, existing.petInventory, existing.totalRolls, existing.rollsSinceEpicOrAbove, existing.rollsSinceLegendary, existing.rollsSinceMythical, existing.lastDiceRollTimestamp, existing.coins, existing.petUpgrades, existing.equippedPetIds)
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
        xpBootstrapped: Boolean = false,
        potionInventory: Map<String, Int> = emptyMap(),
        lastPotionEarnedTimestamp: Long = 0L,
        miniGameHighScore: Int = 0,
        petInventory: List<Pet> = emptyList(),
        totalRolls: Int = 0,
        rollsSinceEpicOrAbove: Int = 0,
        rollsSinceLegendary: Int = 0,
        rollsSinceMythical: Int = 0,
        lastDiceRollTimestamp: Long = 0L,
        coins: Long = 0L,
        petUpgrades: Map<String, Int> = emptyMap(),
        equippedPetIds: List<String> = emptyList()
    ) {
        try {
            val appData = StorageData(exercises, goals, weightEntries, settings, restDays, runEntries, runningPRs, workoutPresets, workoutSession, workoutHistory, totalXp, xpBootstrapped, potionInventory, lastPotionEarnedTimestamp, miniGameHighScore)
            file.writeText(gson.toJson(appData))
            val petData = PetStorageData(petInventory, totalRolls, rollsSinceEpicOrAbove, rollsSinceLegendary, rollsSinceMythical, lastDiceRollTimestamp, coins, petUpgrades, equippedPetIds)
            petFile.writeText(gson.toJson(petData))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadExercises(): List<Exercise> {
        return loadData().first
    }

    fun saveExercises(exercises: List<Exercise>) {
        val full = loadFullData()
        saveFullData(exercises, full.goals, full.weightEntries, full.settings, full.restDays, full.runEntries, full.runningPRs, full.workoutPresets, full.workoutSession, full.workoutHistory, full.totalXp, full.xpBootstrapped, full.potionInventory, full.lastPotionEarnedTimestamp, full.miniGameHighScore, full.petInventory, full.totalRolls, full.rollsSinceEpicOrAbove, full.rollsSinceLegendary, full.rollsSinceMythical, full.lastDiceRollTimestamp, full.coins, full.petUpgrades, full.equippedPetIds)
    }
}

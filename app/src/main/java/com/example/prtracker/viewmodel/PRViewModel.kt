package com.example.prtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.prtracker.data.AppSettings
import com.example.prtracker.data.AppearanceSettings
import com.example.prtracker.data.AppTheme
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.ExerciseDifficulty
import com.example.prtracker.data.parsedDifficulty
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
import com.example.prtracker.data.coinValue
import com.example.prtracker.data.xpMultiplier
import com.example.prtracker.data.PotionType
import com.example.prtracker.data.XpEngine
import com.example.prtracker.data.SessionExerciseProgress
import com.example.prtracker.data.SessionSetEntry
import com.example.prtracker.data.WorkoutPreset
import com.example.prtracker.data.WorkoutSession
import com.example.prtracker.work.PotionCooldownWorker
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
import java.util.concurrent.TimeUnit

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

    private val _totalXp = MutableStateFlow(0L)
    val totalXp: StateFlow<Long> = _totalXp

    private val _potionInventory = MutableStateFlow<Map<String, Int>>(emptyMap())
    val potionInventory: StateFlow<Map<String, Int>> = _potionInventory

    private val _lastPotionEarnedTimestamp = MutableStateFlow(0L)
    val lastPotionEarnedTimestamp: StateFlow<Long> = _lastPotionEarnedTimestamp

    private val _miniGameHighScore = MutableStateFlow(0)
    val miniGameHighScore: StateFlow<Int> = _miniGameHighScore

    private val _petInventory = MutableStateFlow<List<com.example.prtracker.data.Pet>>(emptyList())
    val petInventory: StateFlow<List<com.example.prtracker.data.Pet>> = _petInventory

    private val _totalRolls = MutableStateFlow(0)
    val totalRolls: StateFlow<Int> = _totalRolls

    private val _rollsSinceEpicOrAbove = MutableStateFlow(0)
    private val _rollsSinceLegendary = MutableStateFlow(0)
    private val _rollsSinceMythical = MutableStateFlow(0)
    private val _rollsSinceDivine = MutableStateFlow(0)
    private val _lastDiceRollTimestamp = MutableStateFlow(0L)

    private val _coins = MutableStateFlow(0L)
    val coins: StateFlow<Long> = _coins

    private val _petUpgrades = MutableStateFlow<Map<String, Int>>(emptyMap())
    val petUpgrades: StateFlow<Map<String, Int>> = _petUpgrades

    private val _autoRoll = MutableStateFlow(false)
    val autoRoll: StateFlow<Boolean> = _autoRoll

    private val _equippedPetIds = MutableStateFlow<List<String>>(emptyList())
    val equippedPetIds: StateFlow<List<String>> = _equippedPetIds

    private val _miniGameSettings = MutableStateFlow(com.example.prtracker.data.MiniGameSettings())
    val miniGameSettings: StateFlow<com.example.prtracker.data.MiniGameSettings> = _miniGameSettings

    private val _diceInventory = MutableStateFlow<List<com.example.prtracker.data.SpecialDice>>(emptyList())
    val diceInventory: StateFlow<List<com.example.prtracker.data.SpecialDice>> = _diceInventory

    private val _activeDiceEffects = MutableStateFlow<List<com.example.prtracker.data.ActiveDiceEffect>>(emptyList())
    val activeDiceEffects: StateFlow<List<com.example.prtracker.data.ActiveDiceEffect>> = _activeDiceEffects

    private val _speciesTierCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val speciesTierCounts: StateFlow<Map<String, Int>> = _speciesTierCounts

    fun toggleAutoRoll() {
        _autoRoll.value = !_autoRoll.value
    }

    fun getDiceCount(typeId: String): Int =
        _diceInventory.value.find { it.typeId == typeId }?.quantity ?: 0

    fun buyDice(typeId: String, count: Int = 1) {
        val diceType = com.example.prtracker.data.SpecialDiceType.fromId(typeId) ?: return
        val totalCost = diceType.price * count
        if (_coins.value < totalCost) return
        _coins.value -= totalCost
        val existing = _diceInventory.value.find { it.typeId == typeId }
        if (existing != null) {
            _diceInventory.value = _diceInventory.value.map {
                if (it.id == existing.id) it.copy(quantity = it.quantity + count) else it
            }
        } else {
            _diceInventory.value = _diceInventory.value + com.example.prtracker.data.SpecialDice(typeId = typeId, quantity = count)
        }
        savePetData()
    }

    fun useDice(diceId: String) {
        val dice = _diceInventory.value.find { it.id == diceId } ?: return
        useDiceByType(dice.typeId ?: return, 1)
    }

    fun useDiceByType(typeId: String, count: Int = 1) {
        if (count <= 0) return
        val diceType = com.example.prtracker.data.SpecialDiceType.fromId(typeId) ?: return
        val entry = _diceInventory.value.find { it.typeId == typeId } ?: return
        val consume = minOf(count, entry.quantity)
        val totalRolls = diceType.rollsCount * consume

        if (entry.quantity <= consume) {
            _diceInventory.value = _diceInventory.value.filter { it.id != entry.id }
        } else {
            _diceInventory.value = _diceInventory.value.map {
                if (it.id == entry.id) it.copy(quantity = it.quantity - consume) else it
            }
        }

        val currentEffects = _activeDiceEffects.value.toMutableList()
        val existingIndex = currentEffects.indexOfFirst { it.diceTypeId == typeId }
        if (existingIndex >= 0) {
            val existing = currentEffects[existingIndex]
            currentEffects[existingIndex] = existing.copy(
                rollsRemaining = existing.rollsRemaining + totalRolls,
                rollsTotal = existing.rollsTotal + totalRolls
            )
        } else {
            val effect = com.example.prtracker.data.ActiveDiceEffect(
                diceTypeId = typeId,
                rollsRemaining = totalRolls,
                rollsTotal = totalRolls
            )
            val strengthOrder = com.example.prtracker.data.SpecialDiceType.strengthOrder
            val insertIndex = currentEffects.indexOfFirst { existing ->
                val existingType = com.example.prtracker.data.SpecialDiceType.fromId(existing.diceTypeId)
                val existingStrength = existingType?.let { strengthOrder.indexOf(it) } ?: -1
                val newStrength = strengthOrder.indexOf(diceType)
                existingStrength > newStrength
            }
            if (insertIndex == -1) {
                currentEffects.add(effect)
            } else {
                currentEffects.add(insertIndex, effect)
            }
        }
        _activeDiceEffects.value = currentEffects
        savePetData()
    }

    private fun collapseDiceInventory() {
        val inv = _diceInventory.value
        if (inv.size <= 1) return
        val collapsed = inv.groupBy { it.typeId }
            .map { (typeId, entries) ->
                val first = entries.first()
                val total = entries.sumOf { it.quantity }
                first.copy(quantity = total)
            }
        _diceInventory.value = collapsed
    }

    private fun decrementActiveDiceEffects(): com.example.prtracker.data.SpecialDiceType? {
        val effects = _activeDiceEffects.value.toMutableList()
        if (effects.isEmpty()) return null
        val first = effects[0]
        val updated = first.copy(rollsRemaining = first.rollsRemaining - 1)
        if (updated.rollsRemaining <= 0) {
            effects.removeAt(0)
        } else {
            effects[0] = updated
        }
        _activeDiceEffects.value = effects
        return first.diceType
    }

    fun removeActiveDiceEffect(typeId: String) {
        val effects = _activeDiceEffects.value.toMutableList()
        val index = effects.indexOfFirst { it.diceTypeId == typeId }
        if (index >= 0) {
            effects.removeAt(index)
            _activeDiceEffects.value = effects
            savePetData()
        }
    }

    fun equipPet(petId: String) {
        val maxSlots = getUpgradeLevel(com.example.prtracker.data.PetUpgrade.EQUIP_SLOTS) + 2
        val current = _equippedPetIds.value.toMutableList()
        if (current.contains(petId) || current.size >= maxSlots) return
        current.add(petId)
        _equippedPetIds.value = current
        savePetData()
    }

    fun unequipPet(petId: String) {
        _equippedPetIds.value = _equippedPetIds.value.filter { it != petId }
        savePetData()
    }

    fun petXpMultiplier(): Float {
        val equipped = _equippedPetIds.value
        if (equipped.isEmpty()) return 1.0f
        val inventory = _petInventory.value
        var mult = 1.0f
        for (id in equipped) {
            val pet = inventory.find { it.id == id } ?: continue
            mult += pet.xpMultiplier(inventory) - 1.0f
        }
        return mult.coerceAtLeast(1.0f)
    }

    fun maxEquipSlots(): Int = getUpgradeLevel(com.example.prtracker.data.PetUpgrade.EQUIP_SLOTS) + 2

    fun equipBest() {
        val inventory = _petInventory.value
        val best = inventory.sortedByDescending { it.xpMultiplier(inventory) }
            .take(maxEquipSlots()).map { it.id }
        _equippedPetIds.value = best
        savePetData()
    }

    fun coinMultiplier(): Float = 1.0f + getUpgradeLevel(com.example.prtracker.data.PetUpgrade.COIN_MULTIPLIER) * 0.20f

    fun getMultiRollCount(): Int =
        com.example.prtracker.data.PetUpgrade.multiRollCount(
            getUpgradeLevel(com.example.prtracker.data.PetUpgrade.MULTI_ROLL)
        )

    fun getEffectiveRollCount(): Int {
        val maxUnlocked = getMultiRollCount()
        val chosen = _miniGameSettings.value.selectedRollCount
        return if (chosen <= 0) maxUnlocked else minOf(chosen, maxUnlocked)
    }

    fun setAutoSellRarity(rarity: String, enabled: Boolean) {
        val current = _miniGameSettings.value.autoSellRarities.toMutableSet()
        if (enabled) current.add(rarity) else current.remove(rarity)
        _miniGameSettings.value = _miniGameSettings.value.copy(autoSellRarities = current)
        savePetData()
    }

    fun setFreezeRarity(rarity: String, enabled: Boolean) {
        val current = _miniGameSettings.value.freezeRarities.toMutableSet()
        if (enabled) current.add(rarity) else current.remove(rarity)
        _miniGameSettings.value = _miniGameSettings.value.copy(freezeRarities = current)
        savePetData()
    }

    fun setSelectedRollCount(count: Int) {
        _miniGameSettings.value = _miniGameSettings.value.copy(selectedRollCount = count)
        savePetData()
    }

    private fun incrementSpeciesTierCount(speciesId: String, tier: String) {
        val key = "${speciesId}_$tier"
        _speciesTierCounts.value = _speciesTierCounts.value.toMutableMap().apply {
            put(key, (get(key) ?: 0) + 1)
        }
    }

    fun sellPet(petId: String) {
        val pet = _petInventory.value.find { it.id == petId } ?: return
        val r = com.example.prtracker.data.PetRarity.fromName(pet.rarity)
        val isPremium = r == com.example.prtracker.data.PetRarity.SUPER ||
            r == com.example.prtracker.data.PetRarity.EXCLUSIVE ||
            r == com.example.prtracker.data.PetRarity.SECRET
        _coins.value += if (isPremium) pet.coinValue()
        else (pet.coinValue() * coinMultiplier()).toLong()
        _petInventory.value = _petInventory.value.filter { it.id != petId }
        _equippedPetIds.value = _equippedPetIds.value.filter { it != petId }
        savePetData()
    }

    fun toggleFavorite(petId: String) {
        _petInventory.value = _petInventory.value.map {
            if (it.id == petId) it.copy(isFavorited = !it.isFavorited) else it
        }
        savePetData()
    }

    fun sellAllUnfavorited(): Long {
        val unfavorited = _petInventory.value.filter { !it.isFavorited }
        val (premiumPets, normalPets) = unfavorited.partition {
            val r = com.example.prtracker.data.PetRarity.fromName(it.rarity)
            r == com.example.prtracker.data.PetRarity.SUPER ||
                r == com.example.prtracker.data.PetRarity.EXCLUSIVE ||
                r == com.example.prtracker.data.PetRarity.SECRET
        }
        val premiumValue = premiumPets.sumOf { it.coinValue() }
        val normalValue = (normalPets.sumOf { it.coinValue().toLong() } * coinMultiplier()).toLong()
        val totalCoins = premiumValue + normalValue
        if (totalCoins > 0) {
            val soldIds = unfavorited.map { it.id }.toSet()
            _coins.value += totalCoins
            _petInventory.value = _petInventory.value.filter { it.isFavorited }
            _equippedPetIds.value = _equippedPetIds.value.filter { it !in soldIds }
            savePetData()
        }
        return totalCoins
    }

    fun sellPets(ids: Collection<String>): Long {
        val selected = _petInventory.value.filter { it.id in ids && !it.isFavorited }
        val (premiumPets, normalPets) = selected.partition {
            val r = com.example.prtracker.data.PetRarity.fromName(it.rarity)
            r == com.example.prtracker.data.PetRarity.SUPER ||
                r == com.example.prtracker.data.PetRarity.EXCLUSIVE ||
                r == com.example.prtracker.data.PetRarity.SECRET
        }
        val premiumValue = premiumPets.sumOf { it.coinValue() }
        val normalValue = (normalPets.sumOf { it.coinValue().toLong() } * coinMultiplier()).toLong()
        val totalCoins = premiumValue + normalValue
        if (totalCoins > 0) {
            val soldIds = selected.map { it.id }.toSet()
            _coins.value += totalCoins
            _petInventory.value = _petInventory.value.filter { it.id !in soldIds }
            _equippedPetIds.value = _equippedPetIds.value.filter { it !in soldIds }
            savePetData()
        }
        return totalCoins
    }

    fun getUpgradeLevel(upgrade: com.example.prtracker.data.PetUpgrade): Int {
        return _petUpgrades.value[upgrade.id] ?: 0
    }

    fun purchaseUpgrade(upgrade: com.example.prtracker.data.PetUpgrade): Boolean {
        val currentLevel = getUpgradeLevel(upgrade)
        val cost = upgrade.nextLevelCost(currentLevel)
        if (_coins.value < cost) return false
        _coins.value -= cost
        _petUpgrades.value = _petUpgrades.value + (upgrade.id to (currentLevel + 1))
        savePetData()
        return true
    }

    fun purchaseUpgradeMultiple(upgrade: com.example.prtracker.data.PetUpgrade, count: Int): Int {
        val startLevel = getUpgradeLevel(upgrade)
        var totalSpent = 0L
        var purchased = 0
        for (i in 0 until count) {
            val cost = upgrade.nextLevelCost(startLevel + i)
            if (totalSpent + cost > _coins.value) break
            totalSpent += cost
            purchased++
        }
        if (purchased == 0) return 0
        _coins.value -= totalSpent
        _petUpgrades.value = _petUpgrades.value + (upgrade.id to (startLevel + purchased))
        savePetData()
        return purchased
    }

    private val _lastBonusXpEarned = MutableSharedFlow<Long>(replay = 0)
    val lastBonusXpEarned: SharedFlow<Long> = _lastBonusXpEarned

    private val _activePotionType = MutableStateFlow<PotionType?>(null)
    val activePotionType: StateFlow<PotionType?> = _activePotionType

    private var _xpBootstrapped = false

    val canPlayMiniGame: StateFlow<Boolean> = _lastPotionEarnedTimestamp
        .map { ts -> System.currentTimeMillis() - ts >= 12 * 60 * 60 * 1000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val miniGameCooldownRemainingMs: StateFlow<Long> = _lastPotionEarnedTimestamp
        .map { ts ->
            val remaining = 12 * 60 * 60 * 1000 - (System.currentTimeMillis() - ts)
            remaining.coerceAtLeast(0L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val canRollDice: StateFlow<Boolean> = _lastDiceRollTimestamp
        .map { ts -> System.currentTimeMillis() - ts >= 1000L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentLevel: StateFlow<Int> = _totalXp
        .map { XpEngine.levelFromTotalXp(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val xpInCurrentLevel: StateFlow<Long> = _totalXp
        .map { XpEngine.xpInCurrentLevel(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val xpNeededForLevelUp: StateFlow<Long> = _totalXp
        .map { XpEngine.xpNeededForCurrentLevelUp(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000L)

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

    fun potionCount(type: PotionType): Int =
        _potionInventory.value.getOrDefault(type.id, 0)

    val hasAnyPotion: StateFlow<Boolean> = _potionInventory
        .map { inventory -> inventory.values.any { it > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun awardPotion(type: PotionType) {
        val currentCount = potionCount(type)
        if (currentCount >= type.maxStack) return
        _potionInventory.value = _potionInventory.value + (type.id to currentCount + 1)
        _lastPotionEarnedTimestamp.value = System.currentTimeMillis()
        saveData()

        val workManager = WorkManager.getInstance(getApplication())
        workManager.cancelAllWorkByTag(PotionCooldownWorker.WORK_TAG)
        val request = OneTimeWorkRequestBuilder<PotionCooldownWorker>()
            .setInitialDelay(12, TimeUnit.HOURS)
            .addTag(PotionCooldownWorker.WORK_TAG)
            .build()
        workManager.enqueue(request)
    }

    private fun consumePotion(type: PotionType) {
        val newCount = (potionCount(type) - 1).coerceAtLeast(0)
        _potionInventory.value = if (newCount == 0) {
            _potionInventory.value - type.id
        } else {
            _potionInventory.value + (type.id to newCount)
        }
        _activePotionType.value = null
    }

    fun activatePotion(type: PotionType) {
        if (potionCount(type) > 0) {
            _activePotionType.value = type
        }
    }

    fun deactivatePotion() {
        _activePotionType.value = null
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val full = storageManager.loadFullData()
            val petData = storageManager.loadPetData()
            var loadedXp = full.totalXp
            var bootstrapped = full.xpBootstrapped

            val migratedExercises = full.exercises.map { exercise ->
                val difficulty = exercise.parsedDifficulty()
                val updatedEntries = exercise.entries.map { entry ->
                    if (entry.xpEarned == 0L && entry.value > 0) {
                        entry.copy(xpEarned = XpEngine.xpForEntry(entry.value, exercise.type, difficulty))
                    } else entry
                }
                exercise.copy(entries = updatedEntries)
            }

            _exercises.value = migratedExercises

            if (!bootstrapped) {
                bootstrapped = true
            }

            val migratedHistory = full.workoutHistory.map { session ->
                if (session.xpEarned > 0L) return@map session
                var computed = 0L
                for (exProgress in session.exercises) {
                    val match = migratedExercises.find { it.name == exProgress.exerciseName } ?: continue
                    val type = if (exProgress.isHold) "hold" else "reps"
                    val difficulty = match.parsedDifficulty()
                    for (setEntry in exProgress.completedSets) {
                        computed += XpEngine.xpForEntry(setEntry.value, type, difficulty)
                    }
                }
                session.copy(xpEarned = computed)
            }

            storageManager.saveFullData(
                migratedExercises, full.goals, full.weightEntries, full.settings,
                full.restDays, full.runEntries, full.runningPRs, full.workoutPresets,
                full.workoutSession, migratedHistory, loadedXp, bootstrapped,
                full.potionInventory, full.lastPotionEarnedTimestamp, full.miniGameHighScore,
                full.petInventory, full.totalRolls, full.rollsSinceEpicOrAbove,
                full.rollsSinceLegendary, full.rollsSinceMythical, petData.rollsSinceDivine, full.lastDiceRollTimestamp,
                full.coins, full.petUpgrades, full.equippedPetIds,
                full.diceInventory, full.activeDiceEffects, petData.miniGameSettings
            )

            _totalXp.value = loadedXp
            _xpBootstrapped = bootstrapped

            _goals.value = full.goals
            _weightEntries.value = full.weightEntries
            _appSettings.value = full.settings
            _restDays.value = full.restDays
            _runEntries.value = full.runEntries
            _runningPRs.value = full.runningPRs
            _workoutPresets.value = full.workoutPresets
            _activeSession.value = full.workoutSession
            _workoutHistory.value = migratedHistory
            _potionInventory.value = full.potionInventory
            _lastPotionEarnedTimestamp.value = full.lastPotionEarnedTimestamp
            _miniGameHighScore.value = full.miniGameHighScore
            _petInventory.value = full.petInventory
            _totalRolls.value = full.totalRolls
            _rollsSinceEpicOrAbove.value = full.rollsSinceEpicOrAbove
            _rollsSinceLegendary.value = full.rollsSinceLegendary
            _rollsSinceMythical.value = full.rollsSinceMythical
            _rollsSinceDivine.value = petData.rollsSinceDivine
            _lastDiceRollTimestamp.value = full.lastDiceRollTimestamp
            _coins.value = full.coins
            _petUpgrades.value = full.petUpgrades
            _equippedPetIds.value = full.equippedPetIds
            _diceInventory.value = full.diceInventory
            collapseDiceInventory()
            _activeDiceEffects.value = full.activeDiceEffects
            _miniGameSettings.value = petData.miniGameSettings
            _speciesTierCounts.value = petData.speciesTierCounts
            // Bootstrap: populate counts from existing inventory for first-time users
            if (_speciesTierCounts.value.isEmpty() && _petInventory.value.isNotEmpty()) {
                val counts = mutableMapOf<String, Int>()
                for (p in _petInventory.value) {
                    val key = "${p.speciesId}_${p.tier}"
                    counts[key] = (counts[key] ?: 0) + 1
                }
                _speciesTierCounts.value = counts
            }
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
                _workoutHistory.value,
                _totalXp.value,
                _xpBootstrapped,
                _potionInventory.value,
                _lastPotionEarnedTimestamp.value,
                _miniGameHighScore.value,
                _petInventory.value,
                _totalRolls.value,
                _rollsSinceEpicOrAbove.value,
                _rollsSinceLegendary.value,
                _rollsSinceMythical.value,
                _rollsSinceDivine.value,
                _lastDiceRollTimestamp.value,
                _coins.value,
                _petUpgrades.value,
                _equippedPetIds.value,
                _diceInventory.value,
                _activeDiceEffects.value,
                _miniGameSettings.value
            )
        }
    }

    private fun recalculateTotalXp() {
        _totalXp.value = _exercises.value.sumOf { exercise ->
            exercise.entries.sumOf { it.xpEarned }
        }
    }

    fun addExercise(exercise: Exercise) {
        val current = _exercises.value.toMutableList()
        val maxOrder = current.filter { it.isPinned == exercise.isPinned }.maxOfOrNull { it.sortOrder } ?: -1
        current.add(exercise.copy(sortOrder = maxOrder + 1))
        _exercises.value = current
        saveData()
    }

    fun renameExercise(exerciseId: String, newName: String) {
        val exercises = _exercises.value.toMutableList()
        val index = exercises.indexOfFirst { it.id == exerciseId }
        if (index < 0) return
        val oldName = exercises[index].name
        if (oldName == newName) return
        exercises[index] = exercises[index].copy(name = newName)
        _exercises.value = exercises

        _goals.value = _goals.value.map { goal ->
            if (goal.exerciseId == exerciseId) goal.copy(exerciseName = newName) else goal
        }

        _workoutPresets.value = _workoutPresets.value.map { preset ->
            preset.copy(exercises = preset.exercises.map { pe ->
                if (pe.exerciseName == oldName) pe.copy(exerciseName = newName) else pe
            })
        }

        val session = _activeSession.value
        if (session != null) {
            val updatedSessionExercises = session.exercises.map { sep ->
                if (sep.exerciseName == oldName) sep.copy(exerciseName = newName) else sep
            }
            _activeSession.value = session.copy(exercises = updatedSessionExercises)
        }

        _workoutHistory.value = _workoutHistory.value.map { session ->
            session.copy(exercises = session.exercises.map { sep ->
                if (sep.exerciseName == oldName) sep.copy(exerciseName = newName) else sep
            })
        }

        saveData()
        if (_activeSession.value != null) saveSessionData()
    }

    fun logEntry(exerciseId: String, entry: PREntry) {
        val exercise = _exercises.value.find { it.id == exerciseId }
        val hasXpDouble = _activePotionType.value == PotionType.XP_DOUBLE
        val baseXp = if (exercise != null && entry.value > 0) XpEngine.xpForEntry(entry.value, exercise.type, exercise.parsedDifficulty()) else 0L
        val petMult = petXpMultiplier()
        val potionMult = if (hasXpDouble) 2 else 1
        val finalXp = (baseXp * petMult * potionMult).toLong()
        val entryWithXp = entry.copy(xpEarned = finalXp)

        val updated = _exercises.value.toMutableList()
        var targetIndex = -1
        for (i in updated.indices) {
            val ex = updated[i]
            if (ex.id == exerciseId) {
                updated[i] = ex.copy(entries = listOf(entryWithXp) + ex.entries)
                targetIndex = i
                break
            }
        }
        if (targetIndex >= 0) {
            val moved = updated.removeAt(targetIndex)
            updated.add(0, moved)
        }
        _exercises.value = updated

        if (finalXp > 0L) {
            _totalXp.value += finalXp
            if (hasXpDouble) {
                consumePotion(PotionType.XP_DOUBLE)
                viewModelScope.launch { _lastBonusXpEarned.emit(finalXp) }
            }
        } else {
            recalculateTotalXp()
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
        recalculateTotalXp()
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
        _totalXp.value = 0L
        _xpBootstrapped = false
        _potionInventory.value = emptyMap()
        _lastPotionEarnedTimestamp.value = 0L
        _miniGameHighScore.value = 0
        _activePotionType.value = null
        _petInventory.value = emptyList()
        _totalRolls.value = 0
        _rollsSinceEpicOrAbove.value = 0
        _rollsSinceLegendary.value = 0
        _rollsSinceMythical.value = 0
        _rollsSinceDivine.value = 0
        _lastDiceRollTimestamp.value = 0L
        _coins.value = 0L
        _equippedPetIds.value = emptyList()
        _diceInventory.value = emptyList()
        _activeDiceEffects.value = emptyList()
        saveData()
    }

    fun clearPetData() {
        _petInventory.value = emptyList()
        _totalRolls.value = 0
        _rollsSinceEpicOrAbove.value = 0
        _rollsSinceLegendary.value = 0
        _rollsSinceMythical.value = 0
        _rollsSinceDivine.value = 0
        _lastDiceRollTimestamp.value = 0L
        _coins.value = 0L
        _petUpgrades.value = emptyMap()
        _equippedPetIds.value = emptyList()
        _diceInventory.value = emptyList()
        _activeDiceEffects.value = emptyList()
        savePetData()
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
                targetValue = if (pe.isUntilFailure) 0 else if (pe.targetHoldSeconds > 0) pe.targetHoldSeconds else pe.targetReps,
                isHold = pe.targetHoldSeconds > 0,
                isUntilFailure = pe.isUntilFailure,
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
        val exercises = _exercises.value.toMutableList()
        var totalEarned = 0L
        val hasXpDouble = _activePotionType.value == PotionType.XP_DOUBLE
        val petMult = petXpMultiplier()
        val potionMult = if (hasXpDouble) 2 else 1

        for (exProgress in session.exercises) {
            if (exProgress.completedSets.isEmpty()) continue
            val exerciseType = if (exProgress.isHold) "hold" else "reps"
            val index = exercises.indexOfFirst { it.name == exProgress.exerciseName }
            if (index < 0) continue
            val target = exercises[index]
            val difficulty = target.parsedDifficulty()
            val setEntries = mutableListOf<PREntry>()
            for (setEntry in exProgress.completedSets) {
                val baseXp = XpEngine.xpForEntry(setEntry.value, exerciseType, difficulty)
                val finalXp = (baseXp * petMult * potionMult).toLong()
                totalEarned += finalXp
                setEntries.add(
                    PREntry(
                        id = java.util.UUID.randomUUID().toString(),
                        value = setEntry.value,
                        date = now,
                        note = "From workout: ${session.presetName}",
                        xpEarned = finalXp
                    )
                )
            }
            val updated = target.copy(entries = target.entries + setEntries)
            exercises.removeAt(index)
            exercises.add(0, updated)
        }

        _exercises.value = exercises
        recalculateTotalXp()
        if (hasXpDouble) consumePotion(PotionType.XP_DOUBLE)
        val completedSession = session.copy(isCompleted = true, isPaused = false, xpEarned = totalEarned)
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
                workoutHistory = _workoutHistory.value,
                totalXp = _totalXp.value,
                xpBootstrapped = _xpBootstrapped,
                potionInventory = _potionInventory.value,
                lastPotionEarnedTimestamp = _lastPotionEarnedTimestamp.value,
                miniGameHighScore = _miniGameHighScore.value,
                petInventory = _petInventory.value,
                totalRolls = _totalRolls.value,
                rollsSinceEpicOrAbove = _rollsSinceEpicOrAbove.value,
                rollsSinceLegendary = _rollsSinceLegendary.value,
                rollsSinceMythical = _rollsSinceMythical.value,
                rollsSinceDivine = _rollsSinceDivine.value,
                lastDiceRollTimestamp = _lastDiceRollTimestamp.value,
                coins = _coins.value,
                petUpgrades = _petUpgrades.value,
                diceInventory = _diceInventory.value,
                activeDiceEffects = _activeDiceEffects.value,
                miniGameSettings = _miniGameSettings.value
            )
        }
    }

    private fun savePetData() {
        viewModelScope.launch {
            storageManager.savePetData(
                com.example.prtracker.data.PetStorageData(
                    petInventory = _petInventory.value,
                    totalRolls = _totalRolls.value,
                    rollsSinceEpicOrAbove = _rollsSinceEpicOrAbove.value,
                    rollsSinceLegendary = _rollsSinceLegendary.value,
                    rollsSinceMythical = _rollsSinceMythical.value,
                    rollsSinceDivine = _rollsSinceDivine.value,
                    lastDiceRollTimestamp = _lastDiceRollTimestamp.value,
                    coins = _coins.value,
                    petUpgrades = _petUpgrades.value,
                    equippedPetIds = _equippedPetIds.value,
                    diceInventory = _diceInventory.value,
                    activeDiceEffects = _activeDiceEffects.value,
                    miniGameSettings = _miniGameSettings.value,
                    speciesTierCounts = _speciesTierCounts.value
                )
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

    fun updateBestRestGameServings(newBest: Int) {
        if (newBest > _appSettings.value.bestRestGameServings) {
            _appSettings.value = _appSettings.value.copy(bestRestGameServings = newBest)
            _miniGameHighScore.value = newBest
            saveData()
        }
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
            workoutHistory = _workoutHistory.value,
            totalXp = _totalXp.value,
            xpBootstrapped = true,
            potionInventory = _potionInventory.value,
            lastPotionEarnedTimestamp = _lastPotionEarnedTimestamp.value,
            miniGameHighScore = _miniGameHighScore.value,
            diceInventory = _diceInventory.value,
            activeDiceEffects = _activeDiceEffects.value
        )
        return Gson().toJson(fullData)
    }

    fun generateAppExportJson(): String {
        val appData = StorageData(
            exercises = _exercises.value,
            goals = _goals.value,
            weightEntries = _weightEntries.value,
            settings = _appSettings.value,
            restDays = _restDays.value,
            runEntries = _runEntries.value,
            runningPRs = _runningPRs.value,
            workoutPresets = _workoutPresets.value,
            workoutSession = _activeSession.value,
            workoutHistory = _workoutHistory.value,
            totalXp = _totalXp.value,
            xpBootstrapped = true,
            potionInventory = _potionInventory.value,
            lastPotionEarnedTimestamp = _lastPotionEarnedTimestamp.value,
            miniGameHighScore = _miniGameHighScore.value
        )
        return Gson().toJson(appData)
    }

    fun generatePetExportJson(): String {
        val petData = com.example.prtracker.data.PetStorageData(
            petInventory = _petInventory.value,
            totalRolls = _totalRolls.value,
            rollsSinceEpicOrAbove = _rollsSinceEpicOrAbove.value,
            rollsSinceLegendary = _rollsSinceLegendary.value,
            rollsSinceMythical = _rollsSinceMythical.value,
            rollsSinceDivine = _rollsSinceDivine.value,
            lastDiceRollTimestamp = _lastDiceRollTimestamp.value,
            coins = _coins.value,
            petUpgrades = _petUpgrades.value,
            equippedPetIds = _equippedPetIds.value,
            diceInventory = _diceInventory.value,
            activeDiceEffects = _activeDiceEffects.value,
            speciesTierCounts = _speciesTierCounts.value
        )
        return Gson().toJson(petData)
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
                _potionInventory.value = data.potionInventory
                _lastPotionEarnedTimestamp.value = data.lastPotionEarnedTimestamp
                _miniGameHighScore.value = data.miniGameHighScore
                _petInventory.value = data.petInventory
                _totalRolls.value = data.totalRolls
                _rollsSinceEpicOrAbove.value = data.rollsSinceEpicOrAbove
                _rollsSinceLegendary.value = data.rollsSinceLegendary
                _rollsSinceMythical.value = data.rollsSinceMythical
                _lastDiceRollTimestamp.value = data.lastDiceRollTimestamp
                _coins.value = data.coins
                _petUpgrades.value = data.petUpgrades
                _equippedPetIds.value = data.equippedPetIds
                _diceInventory.value = data.diceInventory
                collapseDiceInventory()
                _activeDiceEffects.value = data.activeDiceEffects
                recalculateTotalXp()
                _xpBootstrapped = true
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

                val mergedInventory = mergePotionInventory(_potionInventory.value, data.potionInventory)
                _potionInventory.value = mergedInventory
                _lastPotionEarnedTimestamp.value = maxOf(_lastPotionEarnedTimestamp.value, data.lastPotionEarnedTimestamp)
                _miniGameHighScore.value = maxOf(_miniGameHighScore.value, data.miniGameHighScore)

                mergePetData(data)

                _exercises.value = mergedExercises
                _goals.value = mergedGoals
                _weightEntries.value = mergedWeightEntries
                _appSettings.value = localSettings
                _restDays.value = mergedRestDays

                recalculateTotalXp()
            }
        }
        _xpBootstrapped = true
        saveData()
    }

    fun importAppData(json: String, mode: SyncMode) {
        try {
            val data = Gson().fromJson(json, StorageData::class.java) ?: return
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
                    _potionInventory.value = data.potionInventory
                    _lastPotionEarnedTimestamp.value = data.lastPotionEarnedTimestamp
                    _miniGameHighScore.value = data.miniGameHighScore
                    recalculateTotalXp()
                    _xpBootstrapped = true
                }
                SyncMode.MERGE -> {
                    val mergedExercises = mergeExercises(_exercises.value, data.exercises)
                    val mergedGoals = mergeGoals(_goals.value, data.goals)
                    val mergedWeightEntries = mergeWeightEntries(_weightEntries.value, data.weightEntries)

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

                    val mergedInventory = mergePotionInventory(_potionInventory.value, data.potionInventory)
                    _potionInventory.value = mergedInventory
                    _lastPotionEarnedTimestamp.value = maxOf(_lastPotionEarnedTimestamp.value, data.lastPotionEarnedTimestamp)
                    _miniGameHighScore.value = maxOf(_miniGameHighScore.value, data.miniGameHighScore)

                    _exercises.value = mergedExercises
                    _goals.value = mergedGoals
                    _weightEntries.value = mergedWeightEntries
                    _restDays.value = mergedRestDays

                    recalculateTotalXp()
                }
            }
            _xpBootstrapped = true
            saveData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importPetData(json: String, mode: SyncMode) {
        try {
            val data = Gson().fromJson(json, com.example.prtracker.data.PetStorageData::class.java) ?: return
            when (mode) {
                SyncMode.REPLACE -> {
                    _petInventory.value = data.petInventory
                    _totalRolls.value = data.totalRolls
                    _rollsSinceEpicOrAbove.value = data.rollsSinceEpicOrAbove
                    _rollsSinceLegendary.value = data.rollsSinceLegendary
                    _rollsSinceMythical.value = data.rollsSinceMythical
                    _rollsSinceDivine.value = data.rollsSinceDivine
                    _lastDiceRollTimestamp.value = data.lastDiceRollTimestamp
                    _coins.value = data.coins
                    _petUpgrades.value = data.petUpgrades
                    _equippedPetIds.value = data.equippedPetIds
                    _diceInventory.value = data.diceInventory
                    collapseDiceInventory()
                    _activeDiceEffects.value = data.activeDiceEffects
                    _speciesTierCounts.value = data.speciesTierCounts
                }
                SyncMode.MERGE -> {
                    mergePetDataFromStorage(data)
                }
            }
            savePetData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mergePetData(data: StorageData) {
        val localPetIds = _petInventory.value.map { it.id }.toSet()
        val newPets = data.petInventory.filter { it.id !in localPetIds }
        val existingPets = _petInventory.value.map { pet ->
            val incoming = data.petInventory.find { it.id == pet.id }
            if (incoming != null) {
                val incomingTier = com.example.prtracker.data.PetTier.fromName(incoming.tier)
                val localTier = com.example.prtracker.data.PetTier.fromName(pet.tier)
                when {
                    incomingTier.order > localTier.order -> pet.copy(tier = incoming.tier, stars = incoming.stars)
                    incomingTier.order == localTier.order && incoming.stars > pet.stars -> pet.copy(stars = incoming.stars)
                    else -> pet
                }
            } else pet
        }
        _petInventory.value = (existingPets + newPets).distinctBy { it.id }
        _totalRolls.value = maxOf(_totalRolls.value, data.totalRolls)
        _rollsSinceEpicOrAbove.value = data.rollsSinceEpicOrAbove
        _rollsSinceLegendary.value = data.rollsSinceLegendary
        _rollsSinceMythical.value = data.rollsSinceMythical
        _lastDiceRollTimestamp.value = maxOf(_lastDiceRollTimestamp.value, data.lastDiceRollTimestamp)
        _coins.value = maxOf(_coins.value, data.coins)
        val mergedUpgrades = _petUpgrades.value.toMutableMap()
        for ((key, value) in data.petUpgrades) {
            val local = mergedUpgrades[key] ?: 0
            mergedUpgrades[key] = maxOf(local, value)
        }
        _petUpgrades.value = mergedUpgrades
        // Merge dice inventory by ID dedup
        val localDiceIds = _diceInventory.value.map { it.id }.toSet()
        val newDice = data.diceInventory.filter { it.id !in localDiceIds }
        _diceInventory.value = _diceInventory.value + newDice
        // Merge active dice effects: keep strongest if same type, add if different type
        val existingEffects = _activeDiceEffects.value.toMutableList()
        for (incoming in data.activeDiceEffects) {
            val existing = existingEffects.indexOfFirst { it.diceTypeId == incoming.diceTypeId }
            if (existing != -1) {
                existingEffects[existing] = existingEffects[existing].copy(
                    rollsRemaining = maxOf(existingEffects[existing].rollsRemaining, incoming.rollsRemaining)
                )
            } else {
                existingEffects.add(incoming)
            }
        }
        // Re-sort by strength
        val strengthOrder = com.example.prtracker.data.SpecialDiceType.strengthOrder
        existingEffects.sortByDescending { effect ->
            strengthOrder.indexOfFirst { it.id == effect.diceTypeId }.let { if (it == -1) Int.MAX_VALUE else it }
        }
        _activeDiceEffects.value = existingEffects
    }

    private fun mergePetDataFromStorage(data: com.example.prtracker.data.PetStorageData) {
        val localPetIds = _petInventory.value.map { it.id }.toSet()
        val newPets = data.petInventory.filter { it.id !in localPetIds }
        val existingPets = _petInventory.value.map { pet ->
            val incoming = data.petInventory.find { it.id == pet.id }
            if (incoming != null) {
                val incomingTier = com.example.prtracker.data.PetTier.fromName(incoming.tier)
                val localTier = com.example.prtracker.data.PetTier.fromName(pet.tier)
                when {
                    incomingTier.order > localTier.order -> pet.copy(tier = incoming.tier, stars = incoming.stars)
                    incomingTier.order == localTier.order && incoming.stars > pet.stars -> pet.copy(stars = incoming.stars)
                    else -> pet
                }
            } else pet
        }
        _petInventory.value = (existingPets + newPets).distinctBy { it.id }
        _totalRolls.value = maxOf(_totalRolls.value, data.totalRolls)
        _rollsSinceEpicOrAbove.value = data.rollsSinceEpicOrAbove
        _rollsSinceLegendary.value = data.rollsSinceLegendary
        _rollsSinceMythical.value = data.rollsSinceMythical
        _rollsSinceDivine.value = data.rollsSinceDivine
        _lastDiceRollTimestamp.value = maxOf(_lastDiceRollTimestamp.value, data.lastDiceRollTimestamp)
        _coins.value = maxOf(_coins.value, data.coins)
        val mergedUpgrades = _petUpgrades.value.toMutableMap()
        for ((key, value) in data.petUpgrades) {
            val local = mergedUpgrades[key] ?: 0
            mergedUpgrades[key] = maxOf(local, value)
        }
        _petUpgrades.value = mergedUpgrades
        // Merge dice inventory by ID dedup
        val localDiceIds = _diceInventory.value.map { it.id }.toSet()
        val newDice = data.diceInventory.filter { it.id !in localDiceIds }
        _diceInventory.value = _diceInventory.value + newDice
        // Merge active dice effects
        val existingEffects = _activeDiceEffects.value.toMutableList()
        for (incoming in data.activeDiceEffects) {
            val existing = existingEffects.indexOfFirst { it.diceTypeId == incoming.diceTypeId }
            if (existing != -1) {
                existingEffects[existing] = existingEffects[existing].copy(
                    rollsRemaining = maxOf(existingEffects[existing].rollsRemaining, incoming.rollsRemaining)
                )
            } else {
                existingEffects.add(incoming)
            }
        }
        val strengthOrder = com.example.prtracker.data.SpecialDiceType.strengthOrder
        existingEffects.sortByDescending { effect ->
            strengthOrder.indexOfFirst { it.id == effect.diceTypeId }.let { if (it == -1) Int.MAX_VALUE else it }
        }
        _activeDiceEffects.value = existingEffects
        // Merge species tier counts (sum values)
        val mergedCounts = _speciesTierCounts.value.toMutableMap()
        for ((key, value) in data.speciesTierCounts) {
            mergedCounts[key] = (mergedCounts[key] ?: 0) + value
        }
        _speciesTierCounts.value = mergedCounts
    }

    fun rollDice(): com.example.prtracker.data.RollResult {
        _totalRolls.value += 1
        _rollsSinceEpicOrAbove.value += 1
        _rollsSinceLegendary.value += 1
        _rollsSinceMythical.value += 1
        _rollsSinceDivine.value += 1

        val luckLevel = getUpgradeLevel(com.example.prtracker.data.PetUpgrade.LUCK)
        val luckMultiplier = 1.0 + luckLevel * 0.20

        val luckyRollLevel = getUpgradeLevel(com.example.prtracker.data.PetUpgrade.LUCKY_ROLL)
        val isLuckyRoll = luckyRollLevel > 0 &&
                _totalRolls.value % 5 == 0

        // Look up active dice effect (strongest first) — needed before SUPER check
        val activeDice = _activeDiceEffects.value.firstOrNull()
        val activeDiceType = activeDice?.diceType
        val isSuperDiceActive = activeDiceType == com.example.prtracker.data.SpecialDiceType.SUPER_DICE

        // SUPER DICE — 1/200k chance of SECRET, 1/1000 chance of EXCLUSIVE, else guaranteed SUPER
        if (isSuperDiceActive) {
            // SECRET from SUPER DICE — 1/200k
            if (Math.random() < (1.0 / 200_000.0)) {
                val secretSpecies = com.example.prtracker.data.PetCatalog.speciesForRarity(com.example.prtracker.data.PetRarity.SECRET).random()
                val secretTier = if (isLuckyRoll) {
                    when {
                        luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                        luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                        luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                        luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                        luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                        else                 -> com.example.prtracker.data.PetTier.NORMAL.name
                    }
                } else com.example.prtracker.data.PetTier.NORMAL.name
                val secretTierEnum = com.example.prtracker.data.PetTier.fromName(secretTier)
                val rollReward = (1_000_000_000_000_000L * secretTierEnum.coinMultiplier)
                val pet = com.example.prtracker.data.Pet(speciesId = secretSpecies.id, name = secretSpecies.name, rarity = com.example.prtracker.data.PetRarity.SECRET.name, stars = 1, tier = secretTier, rollNumber = _totalRolls.value)
                _petInventory.value = _petInventory.value + pet
                incrementSpeciesTierCount(pet.speciesId, pet.tier)
                _coins.value += rollReward
                _rollsSinceEpicOrAbove.value = 0; _rollsSinceLegendary.value = 0; _rollsSinceMythical.value = 0; _rollsSinceDivine.value = 0
                decrementActiveDiceEffects()
                _lastDiceRollTimestamp.value = System.currentTimeMillis()
                savePetData()
                return com.example.prtracker.data.RollResult(pet, emptyMap(), isLuckyRoll)
            }
            if (Math.random() < (1.0 / 1000.0)) {
                // EXCLUSIVE from SUPER DICE
                val exSpecies = com.example.prtracker.data.PetCatalog.speciesForRarity(com.example.prtracker.data.PetRarity.EXCLUSIVE).random()
                val exTier = if (isLuckyRoll) {
                    when {
                        luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                        luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                        luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                        luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                        luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                        else                 -> com.example.prtracker.data.PetTier.NORMAL.name
                    }
                } else com.example.prtracker.data.PetTier.NORMAL.name
                val pet = com.example.prtracker.data.Pet(speciesId = exSpecies.id, name = exSpecies.name, rarity = com.example.prtracker.data.PetRarity.EXCLUSIVE.name, stars = 1, tier = exTier, rollNumber = _totalRolls.value)
                _petInventory.value = _petInventory.value + pet
                incrementSpeciesTierCount(pet.speciesId, pet.tier)
                _rollsSinceEpicOrAbove.value = 0; _rollsSinceLegendary.value = 0; _rollsSinceMythical.value = 0; _rollsSinceDivine.value = 0
                decrementActiveDiceEffects()
                _lastDiceRollTimestamp.value = System.currentTimeMillis()
                savePetData()
                return com.example.prtracker.data.RollResult(pet, emptyMap(), isLuckyRoll)
            } else {
                // Guaranteed SUPER from SUPER DICE
                val speciesList = com.example.prtracker.data.PetCatalog.speciesForRarity(com.example.prtracker.data.PetRarity.SUPER)
                val species = speciesList.random()
                val targetTier = if (isLuckyRoll) {
                    when {
                        luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                        luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                        luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                        luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                        luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                        else                 -> com.example.prtracker.data.PetTier.NORMAL.name
                    }
                } else com.example.prtracker.data.PetTier.NORMAL.name
                val pet = com.example.prtracker.data.Pet(speciesId = species.id, name = species.name, rarity = com.example.prtracker.data.PetRarity.SUPER.name, stars = 1, tier = targetTier, rollNumber = _totalRolls.value)
                _petInventory.value = _petInventory.value + pet
                incrementSpeciesTierCount(pet.speciesId, pet.tier)
                _coins.value += 50_000_000_000L
                _rollsSinceEpicOrAbove.value = 0; _rollsSinceLegendary.value = 0; _rollsSinceMythical.value = 0; _rollsSinceDivine.value = 0
                decrementActiveDiceEffects()
                _lastDiceRollTimestamp.value = System.currentTimeMillis()
                // Auto-sell SUPER if checked
                val autoSellSuper = _miniGameSettings.value.autoSellRarities
                if (autoSellSuper.contains(pet.rarity)) {
                    _coins.value += pet.coinValue().toLong()
                    _petInventory.value = _petInventory.value.filter { it.id != pet.id }
                }
                savePetData()
                return com.example.prtracker.data.RollResult(pet, emptyMap(), isLuckyRoll)
            }
        }

        // EXCLUSIVE check — 1 in 1,000,000 natural, or 1 in 100,000 with MYTHIC dice
        val exclusiveChance = if (activeDiceType == com.example.prtracker.data.SpecialDiceType.MYTHIC) 1.0 / 100000.0 else 1.0 / 1000000.0
        if (Math.random() < exclusiveChance) {
            val exSpecies = com.example.prtracker.data.PetCatalog.speciesForRarity(com.example.prtracker.data.PetRarity.EXCLUSIVE).random()
            val exTier = if (isLuckyRoll) {
                when {
                    luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                    luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                    luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                    luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                    luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                    else                 -> com.example.prtracker.data.PetTier.NORMAL.name
                }
            } else com.example.prtracker.data.PetTier.NORMAL.name
            val pet = com.example.prtracker.data.Pet(speciesId = exSpecies.id, name = exSpecies.name, rarity = com.example.prtracker.data.PetRarity.EXCLUSIVE.name, stars = 1, tier = exTier, rollNumber = _totalRolls.value)
            _petInventory.value = _petInventory.value + pet
            incrementSpeciesTierCount(pet.speciesId, pet.tier)
            _rollsSinceEpicOrAbove.value = 0; _rollsSinceLegendary.value = 0; _rollsSinceMythical.value = 0; _rollsSinceDivine.value = 0
            decrementActiveDiceEffects()
            _lastDiceRollTimestamp.value = System.currentTimeMillis()
            savePetData()
            return com.example.prtracker.data.RollResult(pet, emptyMap(), isLuckyRoll)
        }

        // SUPER check — flat 1 in 100,000 (unaffected by luck/dice/pity)
        if (Math.random() < 0.00001) {
            val speciesList = com.example.prtracker.data.PetCatalog.speciesForRarity(com.example.prtracker.data.PetRarity.SUPER)
            val species = speciesList.random()
            val targetTier = if (isLuckyRoll) {
                when {
                    luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                    luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                    luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                    luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                    luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                    else                 -> com.example.prtracker.data.PetTier.NORMAL.name
                }
            } else com.example.prtracker.data.PetTier.NORMAL.name
            val pet = com.example.prtracker.data.Pet(speciesId = species.id, name = species.name, rarity = com.example.prtracker.data.PetRarity.SUPER.name, stars = 1, tier = targetTier, rollNumber = _totalRolls.value)
            _petInventory.value = _petInventory.value + pet
            incrementSpeciesTierCount(pet.speciesId, pet.tier)
            _coins.value += 50_000_000_000L
            _rollsSinceEpicOrAbove.value = 0; _rollsSinceLegendary.value = 0; _rollsSinceMythical.value = 0; _rollsSinceDivine.value = 0
            decrementActiveDiceEffects()
            _lastDiceRollTimestamp.value = System.currentTimeMillis()
            // Auto-sell SUPER if checked
            val autoSellSuper = _miniGameSettings.value.autoSellRarities
            if (autoSellSuper.contains(pet.rarity)) {
                _coins.value += pet.coinValue().toLong()
                _petInventory.value = _petInventory.value.filter { it.id != pet.id }
            }
            savePetData()
            return com.example.prtracker.data.RollResult(pet, emptyMap(), isLuckyRoll)
        }

        // SECRET check — flat 1 in 2,000,000 natural (no pity, no luck, no dice)
        if (Math.random() < (1.0 / 2_000_000.0)) {
            val secretSpecies = com.example.prtracker.data.PetCatalog.speciesForRarity(com.example.prtracker.data.PetRarity.SECRET).random()
            val secretTier = if (isLuckyRoll) {
                when {
                    luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                    luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                    luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                    luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                    luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                    else                 -> com.example.prtracker.data.PetTier.NORMAL.name
                }
            } else com.example.prtracker.data.PetTier.NORMAL.name
            val secretTierEnum = com.example.prtracker.data.PetTier.fromName(secretTier)
            val rollReward = (1_000_000_000_000_000L * secretTierEnum.coinMultiplier)
            val pet = com.example.prtracker.data.Pet(speciesId = secretSpecies.id, name = secretSpecies.name, rarity = com.example.prtracker.data.PetRarity.SECRET.name, stars = 1, tier = secretTier, rollNumber = _totalRolls.value)
            _petInventory.value = _petInventory.value + pet
            incrementSpeciesTierCount(pet.speciesId, pet.tier)
            _coins.value += rollReward
            _rollsSinceEpicOrAbove.value = 0; _rollsSinceLegendary.value = 0; _rollsSinceMythical.value = 0; _rollsSinceDivine.value = 0
            decrementActiveDiceEffects()
            _lastDiceRollTimestamp.value = System.currentTimeMillis()
            savePetData()
            return com.example.prtracker.data.RollResult(pet, emptyMap(), isLuckyRoll)
        }

        val roll = Math.random()

        val effectiveChances = mutableMapOf<com.example.prtracker.data.PetRarity, Double>()
        for (rarity in com.example.prtracker.data.PetRarity.entries) {
            effectiveChances[rarity] = if (activeDiceType?.baseChances != null) {
                activeDiceType.baseChances[rarity] ?: 0.0
            } else {
                rarity.dropChance
            }
        }

        // Apply luck upgrade multiplier to all non-COMMON rarities
        if (luckMultiplier > 1.0) {
            for (rarity in com.example.prtracker.data.PetRarity.entries) {
                if (rarity != com.example.prtracker.data.PetRarity.COMMON) {
                    effectiveChances[rarity] = (effectiveChances[rarity] ?: 0.0) * luckMultiplier
                }
            }
        }

        // Soft pity — only for non-custom dice distributions (custom ones already have generous rates)
        if (activeDiceType?.baseChances == null && _rollsSinceEpicOrAbove.value > 150) {
            val bonus = (_rollsSinceEpicOrAbove.value - 150) * 0.01 * luckMultiplier
            effectiveChances[com.example.prtracker.data.PetRarity.EPIC] =
                (effectiveChances[com.example.prtracker.data.PetRarity.EPIC] ?: 0.0) + bonus
        }

        // Hard pity — guaranteed legendary at 401 rolls
        if (_rollsSinceLegendary.value >= 401) {
            effectiveChances[com.example.prtracker.data.PetRarity.LEGENDARY] = 1.0
        }

        // Hard pity — guaranteed mythical at 2001 rolls
        if (_rollsSinceMythical.value >= 2001) {
            effectiveChances[com.example.prtracker.data.PetRarity.MYTHICAL] = 1.0
        }

        // Hard pity — guaranteed divine at 5001 rolls
        if (_rollsSinceDivine.value >= 5001) {
            effectiveChances[com.example.prtracker.data.PetRarity.DIVINE] = 1.0
        }

        var divineOverrideOneInX: Int? = null

        // Apply active dice effect filter (only for dice without custom baseChances)
        if (activeDiceType != null && activeDiceType.baseChances == null) {
            for (rarity in com.example.prtracker.data.PetRarity.entries) {
                if (rarity.ordinal < activeDiceType.minRarity.ordinal || rarity.ordinal > activeDiceType.maxRarity.ordinal) {
                    effectiveChances[rarity] = 0.0
                }
            }
            // MYTHIC dice: 1/9 chance per roll to upgrade MYTHICAL → DIVINE
            if (activeDiceType == com.example.prtracker.data.SpecialDiceType.MYTHIC && Math.random() < (1.0 / 9.0)) {
                for (rarity in com.example.prtracker.data.PetRarity.entries) effectiveChances[rarity] = 0.0
                effectiveChances[com.example.prtracker.data.PetRarity.DIVINE] = 1.0
                divineOverrideOneInX = 9
            }
            // BANISHING dice: 1/1000 chance of DIVINE
            if (activeDiceType == com.example.prtracker.data.SpecialDiceType.BANISHING && Math.random() < (1.0 / 1000.0)) {
                for (rarity in com.example.prtracker.data.PetRarity.entries) effectiveChances[rarity] = 0.0
                effectiveChances[com.example.prtracker.data.PetRarity.DIVINE] = 1.0
                divineOverrideOneInX = 1000
            }
        }

        // DIVINE chances for custom-distribution dice (baseChances != null)
        // REFINING dice: 1/100 chance of DIVINE
        if (activeDiceType == com.example.prtracker.data.SpecialDiceType.REFINING && Math.random() < (1.0 / 100.0)) {
            for (rarity in com.example.prtracker.data.PetRarity.entries) effectiveChances[rarity] = 0.0
            effectiveChances[com.example.prtracker.data.PetRarity.DIVINE] = 1.0
            divineOverrideOneInX = 100
        }
        // ASCENDANT dice: 1/20 chance of DIVINE
        if (activeDiceType == com.example.prtracker.data.SpecialDiceType.ASCENDANT && Math.random() < (1.0 / 20.0)) {
            for (rarity in com.example.prtracker.data.PetRarity.entries) effectiveChances[rarity] = 0.0
            effectiveChances[com.example.prtracker.data.PetRarity.DIVINE] = 1.0
            divineOverrideOneInX = 20
        }
        // LEGENDARY dice: 1/8 chance of DIVINE
        if (activeDiceType == com.example.prtracker.data.SpecialDiceType.LEGENDARY && Math.random() < (1.0 / 8.0)) {
            for (rarity in com.example.prtracker.data.PetRarity.entries) effectiveChances[rarity] = 0.0
            effectiveChances[com.example.prtracker.data.PetRarity.DIVINE] = 1.0
            divineOverrideOneInX = 8
        }

        var selectedRarity: com.example.prtracker.data.PetRarity
        var chancesUsedForRoll: Map<com.example.prtracker.data.PetRarity, Double> = effectiveChances

        if (isLuckyRoll) {
            val luckyRollRarityBoost = 1.0 + luckyRollLevel * 0.25
            val boostedChances = effectiveChances.mapValues { (rarity, chance) ->
                if (rarity != com.example.prtracker.data.PetRarity.COMMON) chance * luckyRollRarityBoost else chance
            }
            chancesUsedForRoll = boostedChances
            val totalChance = boostedChances.values.sum()
            var normalizedRoll = roll * totalChance
            selectedRarity = com.example.prtracker.data.PetRarity.COMMON

            for ((rarity, chance) in boostedChances) {
                normalizedRoll -= chance
                if (normalizedRoll <= 0.0) {
                    selectedRarity = rarity
                    break
                }
            }
        } else {
            val totalChance = effectiveChances.values.sum()
            var normalizedRoll = roll * totalChance
            selectedRarity = com.example.prtracker.data.PetRarity.COMMON

            for ((rarity, chance) in effectiveChances) {
                normalizedRoll -= chance
                if (normalizedRoll <= 0.0) {
                    selectedRarity = rarity
                    break
                }
            }
        }

        val speciesList = com.example.prtracker.data.PetCatalog.speciesForRarity(selectedRarity)
        val species = speciesList.random()

        val targetTier = if (isLuckyRoll) {
            when {
                luckyRollLevel > 200 -> com.example.prtracker.data.PetTier.RED_MATTER.name
                luckyRollLevel > 150 -> com.example.prtracker.data.PetTier.DARK_MATTER.name
                luckyRollLevel > 100 -> com.example.prtracker.data.PetTier.RAINBOW.name
                luckyRollLevel > 50  -> com.example.prtracker.data.PetTier.GOLDEN.name
                luckyRollLevel > 0   -> com.example.prtracker.data.PetTier.SILVER.name
                else                 -> com.example.prtracker.data.PetTier.NORMAL.name
            }
        } else {
            com.example.prtracker.data.PetTier.NORMAL.name
        }

        val existingPets = _petInventory.value.filter {
            it.speciesId == species.id && it.tier == targetTier
        }
        val pet: com.example.prtracker.data.Pet
        val upgradeTarget = existingPets.firstOrNull { it.stars < 5 }
        if (upgradeTarget != null) {
            val newStars = upgradeTarget.stars + 1
            pet = upgradeTarget.copy(stars = newStars, rollNumber = _totalRolls.value)
            _petInventory.value = _petInventory.value.map { if (it.id == upgradeTarget.id) pet else it }
        } else {
            pet = com.example.prtracker.data.Pet(
                speciesId = species.id,
                name = species.name,
                rarity = selectedRarity.name,
                stars = 1,
                tier = targetTier,
                rollNumber = _totalRolls.value
            )
            _petInventory.value = _petInventory.value + pet
        }

        incrementSpeciesTierCount(pet.speciesId, pet.tier)

        val upgradeMult = 1.0 + getUpgradeLevel(com.example.prtracker.data.PetUpgrade.COIN_MULTIPLIER) * 0.20
        val petCoinMult = petXpMultiplier().toDouble()
        val isPremiumRarity = selectedRarity == com.example.prtracker.data.PetRarity.SUPER ||
            selectedRarity == com.example.prtracker.data.PetRarity.EXCLUSIVE ||
            selectedRarity == com.example.prtracker.data.PetRarity.SECRET
        _coins.value += if (isPremiumRarity) pet.coinValue().toLong()
        else (pet.coinValue().toLong() * upgradeMult * petCoinMult).toLong()

        if (selectedRarity == com.example.prtracker.data.PetRarity.EPIC ||
            selectedRarity == com.example.prtracker.data.PetRarity.LEGENDARY ||
            selectedRarity == com.example.prtracker.data.PetRarity.MYTHICAL ||
            selectedRarity == com.example.prtracker.data.PetRarity.DIVINE) {
            _rollsSinceEpicOrAbove.value = 0
        }
        if (selectedRarity == com.example.prtracker.data.PetRarity.LEGENDARY) {
            _rollsSinceLegendary.value = 0
        }
        if (selectedRarity == com.example.prtracker.data.PetRarity.MYTHICAL) {
            _rollsSinceMythical.value = 0
        }
        if (selectedRarity == com.example.prtracker.data.PetRarity.DIVINE) {
            _rollsSinceDivine.value = 0
        }

        // Decrement active dice effects after the roll
        decrementActiveDiceEffects()

        _lastDiceRollTimestamp.value = System.currentTimeMillis()

        // Auto-sell: if rarity is in autoSellRarities, sell immediately (EXCLUSIVE, SECRET always kept)
        val autoSell = _miniGameSettings.value.autoSellRarities
        if (selectedRarity != com.example.prtracker.data.PetRarity.EXCLUSIVE && selectedRarity != com.example.prtracker.data.PetRarity.SECRET && autoSell.contains(pet.rarity)) {
            val isSuper = selectedRarity == com.example.prtracker.data.PetRarity.SUPER
            val sellValue = if (isSuper) pet.coinValue().toLong()
            else (pet.coinValue().toLong() * upgradeMult * petCoinMult).toLong()
            _coins.value += sellValue
            _petInventory.value = _petInventory.value.filter { it.id != pet.id }
            _equippedPetIds.value = _equippedPetIds.value.filter { it != pet.id }
        }

        savePetData()
        return com.example.prtracker.data.RollResult(pet, chancesUsedForRoll, isLuckyRoll, displayOneInX = divineOverrideOneInX)
    }

    fun rollDiceMultiple(count: Int): List<com.example.prtracker.data.RollResult> {
        return (0 until count).map { rollDice() }
    }

    fun fusePet(petId: String) {
        val pet = _petInventory.value.find { it.id == petId } ?: return
        val rarity = com.example.prtracker.data.PetRarity.fromName(pet.rarity)
        if (rarity == com.example.prtracker.data.PetRarity.SUPER || rarity == com.example.prtracker.data.PetRarity.EXCLUSIVE || rarity == com.example.prtracker.data.PetRarity.SECRET) return
        val currentTier = com.example.prtracker.data.PetTier.fromName(pet.tier)
        val nextTier = com.example.prtracker.data.PetTier.nextTier(currentTier)

        if (nextTier != null) {
            // Standard fusion: evolve to next tier
            val existingAtTarget = _petInventory.value.filter {
                it.speciesId == pet.speciesId && it.tier == nextTier.name
            }
            val upgradeTarget = existingAtTarget.firstOrNull { it.stars < 5 }
            if (upgradeTarget != null) {
                val upgraded = upgradeTarget.copy(stars = upgradeTarget.stars + 1)
                _petInventory.value = _petInventory.value.map {
                    when (it.id) {
                        pet.id -> null
                        upgradeTarget.id -> upgraded
                        else -> it
                    }
                }.filterNotNull()
            } else {
                val fusedPet = pet.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    stars = 1,
                    tier = nextTier.name
                )
                _petInventory.value = _petInventory.value.map {
                    if (it.id == pet.id) fusedPet else it
                }
            }
            incrementSpeciesTierCount(pet.speciesId, nextTier.name)
        } else {
            // Max tier reached (SUPER, EXCLUSIVE, or RED_MATTER): increase stars via same-tier fusion
            val existingSameTier = _petInventory.value.filter {
                it.speciesId == pet.speciesId && it.tier == pet.tier && it.id != pet.id
            }
            val starTarget = existingSameTier.firstOrNull { it.stars < 5 }
            if (starTarget != null) {
                val upgraded = starTarget.copy(stars = starTarget.stars + 1)
                _petInventory.value = _petInventory.value.map {
                    when (it.id) {
                        pet.id -> null
                        starTarget.id -> upgraded
                        else -> it
                    }
                }.filterNotNull()
            } else {
                val fusedPet = pet.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    stars = 1
                )
                _petInventory.value = _petInventory.value.map {
                    if (it.id == pet.id) fusedPet else it
                }
            }
            incrementSpeciesTierCount(pet.speciesId, pet.tier)
        }
        _equippedPetIds.value = _equippedPetIds.value.filter { it != petId }
        savePetData()
    }

    fun fuseAllPets(): Int {
        val fusable = _petInventory.value.filter { pet ->
            pet.stars == 5
                && com.example.prtracker.data.PetTier.nextTier(com.example.prtracker.data.PetTier.fromName(pet.tier)) != null
                && com.example.prtracker.data.PetRarity.fromName(pet.rarity) != com.example.prtracker.data.PetRarity.SUPER
                && com.example.prtracker.data.PetRarity.fromName(pet.rarity) != com.example.prtracker.data.PetRarity.EXCLUSIVE
                && com.example.prtracker.data.PetRarity.fromName(pet.rarity) != com.example.prtracker.data.PetRarity.SECRET
        }
        if (fusable.isEmpty()) return 0

        var current = _petInventory.value
        var fusedCount = 0
        val consumedIds = mutableSetOf<String>()

        for (pet in fusable) {
            val currentTier = com.example.prtracker.data.PetTier.fromName(pet.tier)
            val nextTier = com.example.prtracker.data.PetTier.nextTier(currentTier)

            if (nextTier != null) {
                val existingAtTarget = current.filter {
                    it.speciesId == pet.speciesId && it.tier == nextTier.name
                }
                val upgradeTarget = existingAtTarget.firstOrNull { it.stars < 5 }
                if (upgradeTarget != null) {
                    val upgraded = upgradeTarget.copy(stars = upgradeTarget.stars + 1)
                    current = current.filter { it.id != pet.id && it.id != upgradeTarget.id } + upgraded
                    consumedIds.add(pet.id)
                    consumedIds.add(upgradeTarget.id)
                } else {
                    val fusedPet = pet.copy(id = java.util.UUID.randomUUID().toString(), stars = 1, tier = nextTier.name)
                    current = current.map { if (it.id == pet.id) fusedPet else it }
                    consumedIds.add(pet.id)
                }
                incrementSpeciesTierCount(pet.speciesId, nextTier.name)
            } else {
                val existingSameTier = current.filter {
                    it.speciesId == pet.speciesId && it.tier == pet.tier && it.id != pet.id
                }
                val starTarget = existingSameTier.firstOrNull { it.stars < 5 }
                if (starTarget != null) {
                    val upgraded = starTarget.copy(stars = starTarget.stars + 1)
                    current = current.filter { it.id != pet.id && it.id != starTarget.id } + upgraded
                    consumedIds.add(pet.id)
                    consumedIds.add(starTarget.id)
                } else {
                    val fusedPet = pet.copy(id = java.util.UUID.randomUUID().toString(), stars = 1)
                    current = current.map { if (it.id == pet.id) fusedPet else it }
                    consumedIds.add(pet.id)
                }
                incrementSpeciesTierCount(pet.speciesId, pet.tier)
            }
            fusedCount++
        }

        _petInventory.value = current
        _equippedPetIds.value = _equippedPetIds.value.filter { it !in consumedIds }
        savePetData()
        return fusedCount
    }

    fun fusePremiumPets(ids: List<String>): com.example.prtracker.data.Pet? {
        if (ids.size != 3) return null
        val pets = ids.mapNotNull { id -> _petInventory.value.find { it.id == id } }
        if (pets.size != 3) return null
        val rarities = pets.map { com.example.prtracker.data.PetRarity.fromName(it.rarity) }
        if (rarities.any { it != com.example.prtracker.data.PetRarity.SUPER && it != com.example.prtracker.data.PetRarity.EXCLUSIVE && it != com.example.prtracker.data.PetRarity.SECRET }) return null
        if (pets.map { it.speciesId }.distinct().size != 1) return null
        if (pets.map { it.tier }.distinct().size != 1) return null
        val currentTier = com.example.prtracker.data.PetTier.fromName(pets.first().tier)
        val nextTier = com.example.prtracker.data.PetTier.nextTier(currentTier) ?: return null
        val fusedPet = com.example.prtracker.data.Pet(
            id = java.util.UUID.randomUUID().toString(),
            speciesId = pets.first().speciesId,
            name = pets.first().name,
            rarity = pets.first().rarity,
            tier = nextTier.name,
            stars = 1,
            obtainedAt = System.currentTimeMillis(),
            rollNumber = 0,
            isFavorited = false
        )
        _petInventory.value = _petInventory.value.filter { it.id !in ids } + fusedPet
        incrementSpeciesTierCount(fusedPet.speciesId, nextTier.name)
        _equippedPetIds.value = _equippedPetIds.value.filter { it !in ids }
        savePetData()
        return fusedPet
    }

        fun mergeExercises(local: List<Exercise>, incoming: List<Exercise>): List<Exercise> {
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

     fun mergeGoals(local: List<Goal>, incoming: List<Goal>): List<Goal> {
        val localIds = local.map { it.id }.toSet()
        val newGoals = incoming.filter { it.id !in localIds }
        return local + newGoals
    }

     fun mergeWeightEntries(local: List<WeightEntry>, incoming: List<WeightEntry>): List<WeightEntry> {
        val localIds = local.map { it.id }.toSet()
        val newEntries = incoming.filter { it.id !in localIds }
        return (local + newEntries).sortedByDescending { it.date }
    }

     fun mergePotionInventory(
        local: Map<String, Int>,
        incoming: Map<String, Int>
    ): Map<String, Int> {
        val merged = local.toMutableMap()
        for ((key, count) in incoming) {
            val localCount = merged.getOrDefault(key, 0)
            merged[key] = maxOf(localCount, count)
        }
        return merged
    }
}

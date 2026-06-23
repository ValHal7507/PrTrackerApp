package com.example.prtracker.data

import kotlin.math.pow

object XpEngine {

    fun xpPerRep(difficulty: ExerciseDifficulty): Int = when (difficulty) {
        ExerciseDifficulty.EASY    -> 20
        ExerciseDifficulty.MEDIUM  -> 50
        ExerciseDifficulty.HARD    -> 120
        ExerciseDifficulty.EXTREME -> 300
    }

    fun xpPerSecond(difficulty: ExerciseDifficulty): Int = when (difficulty) {
        ExerciseDifficulty.EASY    -> 8
        ExerciseDifficulty.MEDIUM  -> 20
        ExerciseDifficulty.HARD    -> 50
        ExerciseDifficulty.EXTREME -> 120
    }

    fun xpForEntry(
        entryValue: Int,
        exerciseType: String,
        difficulty: ExerciseDifficulty
    ): Long {
        if (entryValue <= 0) return 0L
        return if (exerciseType == "hold") {
            entryValue.toLong() * xpPerSecond(difficulty)
        } else {
            entryValue.toLong() * xpPerRep(difficulty)
        }
    }

    /** XP needed to advance FROM `level` TO `level + 1`. Max level = 200. */
    fun xpNeededForNextLevel(level: Int): Long {
        if (level >= MAX_LEVEL) return Long.MAX_VALUE
        return (450.0 * level.toDouble().pow(0.87)).toLong()
    }

    const val MAX_LEVEL = 200

    fun cumulativeXpToReach(targetLevel: Int): Long {
        if (targetLevel <= 1) return 0L
        var total = 0L
        for (lvl in 1 until targetLevel) {
            total += xpNeededForNextLevel(lvl)
        }
        return total
    }

    fun levelFromTotalXp(totalXp: Long): Int {
        var level = 1
        var accumulated = 0L
        while (level < MAX_LEVEL) {
            val needed = xpNeededForNextLevel(level)
            if (accumulated + needed > totalXp) break
            accumulated += needed
            level++
        }
        return level
    }

    fun xpInCurrentLevel(totalXp: Long): Long {
        val level = levelFromTotalXp(totalXp)
        val cumulative = cumulativeXpToReach(level)
        return totalXp - cumulative
    }

    fun xpNeededForCurrentLevelUp(totalXp: Long): Long {
        val level = levelFromTotalXp(totalXp)
        return xpNeededForNextLevel(level)
    }

    fun computeTotalXpFromExercises(exercises: List<Exercise>): Long {
        var total = 0L
        for (exercise in exercises) {
            val difficulty = exercise.parsedDifficulty()
            for (entry in exercise.entries) {
                total += xpForEntry(entry.value, exercise.type, difficulty)
            }
        }
        return total
    }
}

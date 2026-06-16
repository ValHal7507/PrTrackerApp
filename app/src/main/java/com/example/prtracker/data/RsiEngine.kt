package com.example.prtracker.data

import java.util.Calendar
import kotlin.math.abs

object RsiEngine {

    fun findClosestWeight(
        entryTimestamp: Long,
        weightEntries: List<WeightEntry>
    ): WeightEntry? {
        if (weightEntries.isEmpty()) return null
        return weightEntries.minByOrNull { abs(it.date - entryTimestamp) }
    }

    fun buildTelemetry(
        exercise: Exercise,
        weightEntries: List<WeightEntry>,
        unitSetting: String
    ): List<LeverageTelemetry> {
        return exercise.entries.mapNotNull { entry ->
            val closestWeight = findClosestWeight(entry.date, weightEntries)
                ?: return@mapNotNull null

            val weightKg = if (unitSetting == "lbs")
                closestWeight.weight / 2.20462f
            else
                closestWeight.weight

            val rsi = when (exercise.type) {
                "reps" -> (entry.value * weightKg).toInt()
                "hold" -> (entry.value * weightKg).toInt()
                else -> return@mapNotNull null
            }

            LeverageTelemetry(
                entryId = entry.id,
                dateString = entry.date.toDateString(),
                rawValue = entry.value,
                pairedWeight = weightKg,
                rsiScore = rsi
            )
        }.sortedBy { it.dateString }
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
}

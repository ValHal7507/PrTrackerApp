package com.example.prtracker.data

object RunningPREngine {
    fun computePRs(entries: List<RunEntry>): RunningPRs {
        if (entries.isEmpty()) return RunningPRs()

        var bestPaceSecondsPerKm: Float? = null
        var bestDistanceMeters: Float? = null
        var bestDurationSeconds: Int? = null
        var best500mSeconds: Int? = null
        var best1kmSeconds: Int? = null
        var best2kmSeconds: Int? = null
        var best5kmSeconds: Int? = null
        var best10kmSeconds: Int? = null
        var bestCaloriesInRun: Int? = null
        var totalDistanceMeters = 0f

        for (entry in entries) {
            totalDistanceMeters += entry.distanceMeters

            if (bestDistanceMeters == null || entry.distanceMeters > bestDistanceMeters) {
                bestDistanceMeters = entry.distanceMeters
            }

            if (bestDurationSeconds == null || entry.durationSeconds > bestDurationSeconds) {
                bestDurationSeconds = entry.durationSeconds
            }

            if (entry.distanceMeters >= 500f) {
                val pace = entry.paceSecondsPerKm
                if (bestPaceSecondsPerKm == null || pace < bestPaceSecondsPerKm) {
                    bestPaceSecondsPerKm = pace
                }

                val estimated500m = (500f / entry.distanceMeters * entry.durationSeconds).toInt()
                if (best500mSeconds == null || estimated500m < best500mSeconds) {
                    best500mSeconds = estimated500m
                }
            }

            if (entry.distanceMeters >= 1000f) {
                val estimated1km = (1000f / entry.distanceMeters * entry.durationSeconds).toInt()
                if (best1kmSeconds == null || estimated1km < best1kmSeconds) {
                    best1kmSeconds = estimated1km
                }
            }

            if (entry.distanceMeters >= 2000f) {
                val estimated2km = (2000f / entry.distanceMeters * entry.durationSeconds).toInt()
                if (best2kmSeconds == null || estimated2km < best2kmSeconds) {
                    best2kmSeconds = estimated2km
                }
            }

            if (entry.distanceMeters >= 5000f) {
                val estimated5km = (5000f / entry.distanceMeters * entry.durationSeconds).toInt()
                if (best5kmSeconds == null || estimated5km < best5kmSeconds) {
                    best5kmSeconds = estimated5km
                }
            }

            if (entry.distanceMeters >= 10000f) {
                val estimated10km = (10000f / entry.distanceMeters * entry.durationSeconds).toInt()
                if (best10kmSeconds == null || estimated10km < best10kmSeconds) {
                    best10kmSeconds = estimated10km
                }
            }

            val cals = entry.caloriesBurned
            if (bestCaloriesInRun == null || cals > bestCaloriesInRun) {
                bestCaloriesInRun = cals
            }
        }

        return RunningPRs(
            bestPaceSecondsPerKm = bestPaceSecondsPerKm,
            bestDistanceMeters = bestDistanceMeters,
            bestDurationSeconds = bestDurationSeconds,
            best500mSeconds = best500mSeconds,
            best1kmSeconds = best1kmSeconds,
            best2kmSeconds = best2kmSeconds,
            best5kmSeconds = best5kmSeconds,
            best10kmSeconds = best10kmSeconds,
            bestCaloriesInRun = bestCaloriesInRun,
            totalDistanceMeters = totalDistanceMeters,
            totalRuns = entries.size
        )
    }

    fun isNewPR(existing: RunningPRs, entry: RunEntry): Boolean {
        if (existing.totalRuns == 0) return true
        val pace = entry.paceSecondsPerKm
        if (existing.bestPaceSecondsPerKm == null || pace < existing.bestPaceSecondsPerKm) return true
        if (existing.bestDistanceMeters == null || entry.distanceMeters > existing.bestDistanceMeters) return true
        if (existing.bestDurationSeconds == null || entry.durationSeconds > existing.bestDurationSeconds) return true
        if (entry.distanceMeters >= 500f) {
            val estimated500m = (500f / entry.distanceMeters * entry.durationSeconds).toInt()
            if (existing.best500mSeconds == null || estimated500m < existing.best500mSeconds) return true
        }
        if (entry.distanceMeters >= 1000f) {
            val estimated1km = (1000f / entry.distanceMeters * entry.durationSeconds).toInt()
            if (existing.best1kmSeconds == null || estimated1km < existing.best1kmSeconds) return true
        }
        if (entry.distanceMeters >= 2000f) {
            val estimated2km = (2000f / entry.distanceMeters * entry.durationSeconds).toInt()
            if (existing.best2kmSeconds == null || estimated2km < existing.best2kmSeconds) return true
        }
        if (entry.distanceMeters >= 5000f) {
            val estimated5km = (5000f / entry.distanceMeters * entry.durationSeconds).toInt()
            if (existing.best5kmSeconds == null || estimated5km < existing.best5kmSeconds) return true
        }
        if (entry.distanceMeters >= 10000f) {
            val estimated10km = (10000f / entry.distanceMeters * entry.durationSeconds).toInt()
            if (existing.best10kmSeconds == null || estimated10km < existing.best10kmSeconds) return true
        }
        val cals = entry.caloriesBurned
        if (existing.bestCaloriesInRun == null || cals > existing.bestCaloriesInRun) return true
        return false
    }

    fun formatPace(secondsPerKm: Float): String {
        if (secondsPerKm <= 0f) return "--:-- /km"
        val minutes = (secondsPerKm / 60f).toInt()
        val secs = (secondsPerKm % 60f).toInt()
        return "%d:%02d /km".format(minutes, secs)
    }

    fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "0:00"
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, mins, secs)
        } else {
            "%02d:%02d".format(mins, secs)
        }
    }

    fun formatDistance(meters: Float): String {
        if (meters <= 0f) return "0m"
        return if (meters >= 1000f) {
            "%.2f km".format(meters / 1000f)
        } else {
            "%.0fm".format(meters)
        }
    }
}

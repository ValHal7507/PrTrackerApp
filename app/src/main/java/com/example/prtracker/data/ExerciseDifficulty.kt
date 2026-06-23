package com.example.prtracker.data

enum class ExerciseDifficulty(val label: String) {
    EASY("EASY"),
    MEDIUM("MEDIUM"),
    HARD("HARD"),
    EXTREME("EXTREME")
}

fun Exercise.parsedDifficulty(): ExerciseDifficulty =
    runCatching { ExerciseDifficulty.valueOf(difficulty) }.getOrDefault(ExerciseDifficulty.MEDIUM)

object ExerciseClassifier {

    private val knownExercises: Map<String, ExerciseDifficulty> = mapOf(
        "decline push" to ExerciseDifficulty.MEDIUM,
        "pike push" to ExerciseDifficulty.HARD,
        "hollow hold" to ExerciseDifficulty.MEDIUM,
        "hollow body hold" to ExerciseDifficulty.MEDIUM,
        "knee push" to ExerciseDifficulty.EASY,
        "negative pull" to ExerciseDifficulty.EASY,
        "dead hang" to ExerciseDifficulty.EASY,
        "arch body" to ExerciseDifficulty.EASY,
        "plank" to ExerciseDifficulty.EASY,
        "australian row" to ExerciseDifficulty.EASY,

        "pull up" to ExerciseDifficulty.MEDIUM,
        "pull-up" to ExerciseDifficulty.MEDIUM,
        "chin up" to ExerciseDifficulty.MEDIUM,
        "chin-up" to ExerciseDifficulty.MEDIUM,
        "dip" to ExerciseDifficulty.HARD,
        "diamond push" to ExerciseDifficulty.MEDIUM,
        "push up" to ExerciseDifficulty.MEDIUM,
        "push-up" to ExerciseDifficulty.MEDIUM,
        "hanging leg raise" to ExerciseDifficulty.MEDIUM,
        "wall handstand" to ExerciseDifficulty.MEDIUM,
        "crow pose" to ExerciseDifficulty.MEDIUM,
        "crow hold" to ExerciseDifficulty.MEDIUM,
        "elbow lever" to ExerciseDifficulty.MEDIUM,
        "hollow" to ExerciseDifficulty.MEDIUM,
        "ring row" to ExerciseDifficulty.MEDIUM,
        "inverted row" to ExerciseDifficulty.MEDIUM,
        "tuck front lever" to ExerciseDifficulty.MEDIUM,

        "muscle up" to ExerciseDifficulty.HARD,
        "muscle-up" to ExerciseDifficulty.HARD,
        "handstand push" to ExerciseDifficulty.HARD,
        "hspu" to ExerciseDifficulty.HARD,
        "l sit" to ExerciseDifficulty.HARD,
        "l-sit" to ExerciseDifficulty.HARD,
        "pseudo planche" to ExerciseDifficulty.HARD,
        "dragon flag" to ExerciseDifficulty.HARD,
        "front lever" to ExerciseDifficulty.HARD,
        "back lever" to ExerciseDifficulty.HARD,
        "skin the cat" to ExerciseDifficulty.HARD,
        "90 degree push" to ExerciseDifficulty.HARD,
        "straddle front lever" to ExerciseDifficulty.HARD,
        "one arm dead hang" to ExerciseDifficulty.HARD,

        "one arm push" to ExerciseDifficulty.EXTREME,
        "one-arm push" to ExerciseDifficulty.EXTREME,
        "one arm pull" to ExerciseDifficulty.EXTREME,
        "one-arm pull" to ExerciseDifficulty.EXTREME,
        "tuck planche" to ExerciseDifficulty.EXTREME,
        "planche lean" to ExerciseDifficulty.EXTREME,
        "straddle planche" to ExerciseDifficulty.EXTREME,
        "full planche" to ExerciseDifficulty.EXTREME,
        "planche push" to ExerciseDifficulty.EXTREME,
        "human flag" to ExerciseDifficulty.EXTREME,
        "iron cross" to ExerciseDifficulty.EXTREME,
        "maltese" to ExerciseDifficulty.EXTREME,
        "victorian cross" to ExerciseDifficulty.EXTREME,
        "manna" to ExerciseDifficulty.EXTREME,
        "v-sit" to ExerciseDifficulty.EXTREME,
        "v sit" to ExerciseDifficulty.EXTREME,
    )

    private val extremeKeywords = listOf(
        "one arm", "one-arm", "planche", "human flag", "iron cross",
        "maltese", "victorian", "manna", "v-sit", "v sit"
    )
    private val hardKeywords = listOf(
        "muscle up", "muscle-up", "handstand push", "hspu", "l-sit", "l sit",
        "pseudo planche", "dragon flag", "front lever", "back lever",
        "skin the cat", "90 degree", "one arm dead hang", "dip"
    )
    private val mediumKeywords = listOf(
        "pull up", "pull-up", "chin up", "chin-up", "push up", "push-up",
        "hanging", "wall handstand", "crow", "elbow lever", "hollow", "row",
        "tuck", "handstand"
    )
    private val easyKeywords = listOf(
        "knee push", "dead hang", "plank", "arch body",
        "australian", "negative", "beginner", "assisted"
    )

    fun classify(exerciseName: String): ExerciseDifficulty {
        val lower = exerciseName.lowercase().trim()

        val knownMatch = knownExercises.entries
            .filter { (key, _) -> lower.contains(key) }
            .maxByOrNull { (key, _) -> key.length }
        if (knownMatch != null) return knownMatch.value

        if (extremeKeywords.any { lower.contains(it) }) return ExerciseDifficulty.EXTREME
        if (hardKeywords.any { lower.contains(it) }) return ExerciseDifficulty.HARD
        if (mediumKeywords.any { lower.contains(it) }) return ExerciseDifficulty.MEDIUM
        if (easyKeywords.any { lower.contains(it) }) return ExerciseDifficulty.EASY

        return ExerciseDifficulty.MEDIUM
    }
}

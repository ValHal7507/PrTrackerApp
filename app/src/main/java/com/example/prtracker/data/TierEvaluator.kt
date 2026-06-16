package com.example.prtracker.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.SecondaryAccent
import com.example.prtracker.ui.theme.TierBootSequence
import com.example.prtracker.ui.theme.TierGravityRebel
import com.example.prtracker.ui.theme.TierKineticFlow
import com.example.prtracker.ui.theme.TierNeuralBridge
import com.example.prtracker.ui.theme.TierOverclockV1
import com.example.prtracker.ui.theme.TierQuantumLever
import com.example.prtracker.ui.theme.TierRawCarbon
import com.example.prtracker.ui.theme.TierStructuralPatch
import com.example.prtracker.ui.theme.TierSystemOverride

@Immutable
data class TierResult(
    val currentTier: Int,
    val tierName: String,
    val tierColor: Color,
    val overallProgress: Float,
    val nextTierNumber: Int,
    val nextTierName: String,
    val nextTierCriteria: List<CriterionStatus>
)

@Immutable
data class CriterionStatus(
    val label: String,
    val userBest: Int,
    val required: Int,
    val met: Boolean
)

object TierEvaluator {

    private enum class ExerciseCategory {
        ONEARM_MUSCLEUP, ONEARM_PULLUP, VICTORIAN, PLANCHE, FRONTLEVER,
        MUSCLEUP, HSPU, HANDSTAND, LSIT, HANG, PLANK, DIPS, PUSHUP, PULLUP
    }

    private val ALIASES: Map<ExerciseCategory, List<String>> = mapOf(
        ExerciseCategory.PULLUP to listOf(
            "pull up", "pullup", "pull ups", "pullups",
            "chin up", "chinup", "chin ups", "chinups"
        ),
        ExerciseCategory.PUSHUP to listOf(
            "push up", "pushup", "push ups", "pushups",
            "press up", "pressup"
        ),
        ExerciseCategory.DIPS to listOf(
            "dip", "dips", "parallel bar dip", "parallel bar dips",
            "bar dip", "bar dips", "tricep dip", "tricep dips"
        ),
        ExerciseCategory.PLANK to listOf(
            "plank", "plank hold"
        ),
        ExerciseCategory.HANG to listOf(
            "dead hang", "deadhang", "hang", "active hang",
            "active dead hang"
        ),
        ExerciseCategory.LSIT to listOf(
            "l sit", "lsit", "l-sit", "l sit hold"
        ),
        ExerciseCategory.HANDSTAND to listOf(
            "handstand", "wall handstand", "handstand hold",
            "freestanding handstand"
        ),
        ExerciseCategory.HSPU to listOf(
            "handstand push up", "handstand pushup", "hspu",
            "handstand push ups", "handstand press"
        ),
        ExerciseCategory.MUSCLEUP to listOf(
            "muscle up", "muscleup", "muscle ups", "muscleups",
            "muscle-up", "strict muscle up", "strict muscleup"
        ),
        ExerciseCategory.FRONTLEVER to listOf(
            "front lever", "frontlever", "front lever hold",
            "tuck front lever", "advanced tuck front lever",
            "straddle front lever", "full front lever"
        ),
        ExerciseCategory.PLANCHE to listOf(
            "planche", "planche hold", "tuck planche",
            "straddle planche", "full planche"
        ),
        ExerciseCategory.VICTORIAN to listOf(
            "victorian", "victorian cross", "victorian hold"
        ),
        ExerciseCategory.ONEARM_PULLUP to listOf(
            "one arm pull up", "one arm pullup", "one-arm pull-up",
            "one arm pull ups", "oafu", "oapu", "single arm pull up"
        ),
        ExerciseCategory.ONEARM_MUSCLEUP to listOf(
            "one arm muscle up", "one arm muscleup",
            "one-arm muscle-up", "single arm muscle up"
        )
    )

    private val normalizedAliases: Map<ExerciseCategory, List<String>> by lazy {
        ALIASES.mapValues { (_, aliases) -> aliases.map { normalize(it) } }
    }

    private data class CriterionDef(
        val category: ExerciseCategory,
        val threshold: Int,
        val label: String
    )

    private data class TierDef(
        val number: Int,
        val name: String,
        val color: Color,
        val criteria: List<CriterionDef>
    )

    private val TIERS: List<TierDef> = listOf(
        TierDef(1, "RAW_CARBON", TierRawCarbon, listOf(
            CriterionDef(ExerciseCategory.PULLUP, 3, "Pull-ups \u2265 3 reps"),
            CriterionDef(ExerciseCategory.PUSHUP, 8, "Push-ups \u2265 8 reps"),
            CriterionDef(ExerciseCategory.HANG, 20, "Dead Hang \u2265 20 sec")
        )),
        TierDef(2, "BOOT_SEQUENCE", TierBootSequence, listOf(
            CriterionDef(ExerciseCategory.PULLUP, 8, "Pull-ups \u2265 8 reps"),
            CriterionDef(ExerciseCategory.PUSHUP, 15, "Push-ups \u2265 15 reps"),
            CriterionDef(ExerciseCategory.PLANK, 45, "Plank Hold \u2265 45 sec")
        )),
        TierDef(3, "STRUCTURAL_PATCH", TierStructuralPatch, listOf(
            CriterionDef(ExerciseCategory.PULLUP, 12, "Pull-ups \u2265 12 reps"),
            CriterionDef(ExerciseCategory.DIPS, 10, "Parallel Bar Dips \u2265 10 reps"),
            CriterionDef(ExerciseCategory.LSIT, 5, "L-Sit Hold \u2265 5 sec")
        )),
        TierDef(4, "OVERCLOCK_V1", TierOverclockV1, listOf(
            CriterionDef(ExerciseCategory.PULLUP, 16, "Pull-ups \u2265 16 reps"),
            CriterionDef(ExerciseCategory.DIPS, 18, "Parallel Bar Dips \u2265 18 reps"),
            CriterionDef(ExerciseCategory.LSIT, 15, "L-Sit Hold \u2265 15 sec")
        )),
        TierDef(5, "NEURAL_BRIDGE", TierNeuralBridge, listOf(
            CriterionDef(ExerciseCategory.PULLUP, 20, "Pull-ups \u2265 20 reps"),
            CriterionDef(ExerciseCategory.HANDSTAND, 20, "Wall Handstand \u2265 20 sec"),
            CriterionDef(ExerciseCategory.FRONTLEVER, 5, "Tuck Front Lever \u2265 5 sec")
        )),
        TierDef(6, "KINETIC_FLOW", TierKineticFlow, listOf(
            CriterionDef(ExerciseCategory.MUSCLEUP, 1, "Strict Muscle-up \u2265 1 rep"),
            CriterionDef(ExerciseCategory.DIPS, 28, "Parallel Bar Dips \u2265 28 reps"),
            CriterionDef(ExerciseCategory.FRONTLEVER, 5, "Adv. Tuck Front Lever \u2265 5 sec")
        )),
        TierDef(7, "CHASSIS_APEX", PrimaryAccent, listOf(
            CriterionDef(ExerciseCategory.MUSCLEUP, 4, "Strict Muscle-ups \u2265 4 reps"),
            CriterionDef(ExerciseCategory.HSPU, 3, "Handstand Push-ups \u2265 3 reps"),
            CriterionDef(ExerciseCategory.FRONTLEVER, 5, "Straddle Front Lever \u2265 5 sec")
        )),
        TierDef(8, "GRAVITY_REBEL", TierGravityRebel, listOf(
            CriterionDef(ExerciseCategory.MUSCLEUP, 8, "Strict Muscle-ups \u2265 8 reps"),
            CriterionDef(ExerciseCategory.HANDSTAND, 15, "Freestanding Handstand \u2265 15 sec"),
            CriterionDef(ExerciseCategory.FRONTLEVER, 4, "Full Front Lever \u2265 4 sec")
        )),
        TierDef(9, "QUANTUM_LEVER", TierQuantumLever, listOf(
            CriterionDef(ExerciseCategory.ONEARM_PULLUP, 1, "One-Arm Pull-up \u2265 1 rep"),
            CriterionDef(ExerciseCategory.PLANCHE, 4, "Straddle Planche \u2265 4 sec"),
            CriterionDef(ExerciseCategory.FRONTLEVER, 10, "Full Front Lever \u2265 10 sec")
        )),
        TierDef(10, "SYSTEM_OVERRIDE", TierSystemOverride, listOf(
            CriterionDef(ExerciseCategory.ONEARM_MUSCLEUP, 1, "One-Arm Muscle-up \u2265 1 rep"),
            CriterionDef(ExerciseCategory.PLANCHE, 5, "Full Planche \u2265 5 sec"),
            CriterionDef(ExerciseCategory.VICTORIAN, 3, "Victorian Cross \u2265 3 sec")
        ))
    )

    private fun normalize(s: String): String {
        return s.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    private fun matchCategory(name: String): ExerciseCategory? {
        val normalizedName = normalize(name)
        if (normalizedName.isEmpty()) return null

        for (category in ExerciseCategory.entries) {
            val aliases = normalizedAliases[category] ?: continue

            for (alias in aliases) {
                if (normalizedName.contains(alias)) {
                    return category
                }
            }

            for (alias in aliases) {
                val threshold = if (alias.length <= 6) 1 else 2
                if (levenshtein(normalizedName, alias) <= threshold) {
                    return category
                }
            }
        }
        return null
    }

    private fun getUserBest(
        category: ExerciseCategory,
        exercises: List<Exercise>
    ): Int {
        return exercises
            .filter { matchCategory(it.name) == category }
            .maxOfOrNull { it.entries.maxOfOrNull { e -> e.value } ?: 0 }
            ?: 0
    }

    fun evaluate(exercises: List<Exercise>): TierResult {
        val bestMap = ExerciseCategory.entries.associateWith { category ->
            getUserBest(category, exercises)
        }

        var currentTier = 0
        var currentTierIndex = -1

        for (i in TIERS.indices.reversed()) {
            val tier = TIERS[i]
            val metCount = tier.criteria.count { criterion ->
                (bestMap[criterion.category] ?: 0) >= criterion.threshold
            }
            if (metCount >= 2) {
                currentTier = tier.number
                currentTierIndex = i
                break
            }
        }

        val overallProgress: Float
        val nextTierNumber: Int
        val nextTierName: String
        val nextTierCriteria: List<CriterionStatus>

        if (currentTier >= 10) {
            overallProgress = 1f
            nextTierNumber = 0
            nextTierName = ""
            nextTierCriteria = emptyList()
        } else {
            val nextIndex = currentTierIndex + 1
            val baseProgress = currentTier * 0.1f
            val partialMetCount = if (nextIndex < TIERS.size) {
                val nextTier = TIERS[nextIndex]
                nextTier.criteria.count { criterion ->
                    (bestMap[criterion.category] ?: 0) >= criterion.threshold
                }
            } else 0
            overallProgress = (baseProgress + (partialMetCount / 3f) * 0.1f).coerceIn(0f, 1f)

            if (nextIndex < TIERS.size) {
                val nextTier = TIERS[nextIndex]
                nextTierNumber = nextTier.number
                nextTierName = nextTier.name
                nextTierCriteria = nextTier.criteria.map { criterion ->
                    val userBest = bestMap[criterion.category] ?: 0
                    CriterionStatus(
                        label = criterion.label,
                        userBest = userBest,
                        required = criterion.threshold,
                        met = userBest >= criterion.threshold
                    )
                }
            } else {
                nextTierNumber = 0
                nextTierName = ""
                nextTierCriteria = emptyList()
            }
        }

        val tierName: String
        val tierColor: Color

        if (currentTier == 0) {
            tierName = "UNRANKED"
            tierColor = PrimaryAccent.copy(alpha = 0.5f)
        } else {
            val activeTier = TIERS[currentTierIndex]
            tierName = activeTier.name
            tierColor = if (activeTier.number == 7) PrimaryAccent else activeTier.color
        }

        return TierResult(
            currentTier = currentTier,
            tierName = tierName,
            tierColor = tierColor,
            overallProgress = overallProgress,
            nextTierNumber = nextTierNumber,
            nextTierName = nextTierName,
            nextTierCriteria = nextTierCriteria
        )
    }
}

package com.example.prtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Exercise
import com.example.prtracker.data.Goal
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.util.UUID

@Composable
fun AddGoalScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val exercises by viewModel.exercises.collectAsState()
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var targetValue by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("daily") }
    var showDropdown by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NEW GOAL",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryAccent
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EXERCISE",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(
                            BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.5f)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showDropdown = !showDropdown }
                        .padding(16.dp)
                ) {
                    Text(
                        text = selectedExercise?.let { "${it.name} (${if (it.type == "reps") "REPS" else "HOLD"})" }
                            ?: "Select an exercise...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedExercise != null) TextPrimary else TextSecondary
                    )
                }

                if (showDropdown && exercises.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Background)
                            .border(
                                BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.3f)),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            items(exercises) { exercise ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedExercise = exercise
                                            showDropdown = false
                                        }
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "${exercise.name} (${if (exercise.type == "reps") "REPS" else "HOLD"})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedExercise?.id == exercise.id) PrimaryAccent else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = targetValue,
                onValueChange = { targetValue = it.filter { c -> c.isDigit() } },
                label = {
                    Text(
                        if (selectedExercise?.type == "hold") "Target Seconds" else "Target Reps"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = selectedExercise != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryAccent,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = PrimaryAccent,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    disabledTextColor = TextSecondary,
                    disabledLabelColor = TextSecondary.copy(alpha = 0.5f),
                    disabledBorderColor = TextSecondary.copy(alpha = 0.15f),
                    disabledContainerColor = Background
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PERIOD",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("daily" to "DAILY", "weekly" to "WEEKLY", "monthly" to "MONTHLY").forEach { (value, label) ->
                    val selected = period == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                if (selected) PrimaryAccent.copy(alpha = 0.15f)
                                else CardBackground
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (selected) PrimaryAccent
                                    else TextSecondary.copy(alpha = 0.3f)
                                ),
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { period = value }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) PrimaryAccent else TextSecondary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedExercise != null && targetValue.isNotBlank()) {
                val unit = if (selectedExercise!!.type == "reps") "reps" else "seconds"
                val periodLabel = period.lowercase()
                GlowingCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Complete ${targetValue} ${unit} every ${periodLabel}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NeonButton(
                text = "SET GOAL",
                onClick = {
                    if (selectedExercise != null && targetValue.isNotBlank()) {
                        val goal = Goal(
                            id = UUID.randomUUID().toString(),
                            exerciseId = selectedExercise!!.id,
                            exerciseName = selectedExercise!!.name,
                            targetValue = targetValue.toInt(),
                            period = period,
                            createdAt = System.currentTimeMillis(),
                            type = selectedExercise!!.type
                        )
                        viewModel.addGoal(goal)
                        navController.popBackStack()
                    }
                },
                enabled = selectedExercise != null && targetValue.isNotBlank() && targetValue.toIntOrNull() ?: 0 > 0
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}



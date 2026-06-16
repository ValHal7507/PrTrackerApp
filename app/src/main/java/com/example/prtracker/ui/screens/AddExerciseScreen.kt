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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Exercise
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
fun AddExerciseScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("reps") }
    var goal by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NEW EXERCISE",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryAccent
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise name") },
                placeholder = { Text("e.g. Push-ups", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryAccent,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = PrimaryAccent,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TYPE",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("reps" to "REPS", "hold" to "HOLD (seconds)").forEach { (value, label) ->
                    val selected = type == value
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
                            .clickable { type = value }
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

            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it.filter { c -> c.isDigit() } },
                label = { Text("Set a target to track progress (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryAccent,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedLabelColor = PrimaryAccent,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            NeonButton(
                text = "CONFIRM",
                onClick = {
                    if (name.isNotBlank()) {
                        val exercise = Exercise(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = type,
                            entries = emptyList()
                        )
                        viewModel.addExercise(exercise)
                        navController.popBackStack()
                    }
                },
                enabled = name.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

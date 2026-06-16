package com.example.prtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalReachedColor
import com.example.prtracker.ui.theme.PinnedAccentSecondary
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary

@Composable
fun SetGoalDialog(
    exerciseName: String,
    exerciseType: String,
    currentPR: Int,
    currentGoal: Int?,
    onSave: (Int) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(currentGoal?.toString() ?: "") }
    val isEdit = currentGoal != null

    val inputNumber = inputValue.toIntOrNull()
    val isValid = inputNumber != null && inputNumber > 0

    Dialog(onDismissRequest = onDismiss) {
        val borderBrush = remember {
            Brush.linearGradient(listOf(PrimaryAccent, PrimaryAccent.copy(alpha = 0.3f)))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Background)
                .border(
                    BorderStroke(1.dp, borderBrush),
                    RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (isEdit) "EDIT GOAL" else "SET GOAL",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryAccent,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryAccent,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (exerciseType == "reps") "Target reps" else "Target seconds",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your current PR is $currentPR ${if (exerciseType == "reps") "reps" else "seconds"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                if (inputValue.isNotEmpty() && !isValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please enter a valid number",
                        style = MaterialTheme.typography.labelSmall,
                        color = PinnedAccentSecondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            color = PrimaryAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isValid) PrimaryAccent.copy(alpha = 0.15f)
                                else PrimaryAccent.copy(alpha = 0.05f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isValid) Brush.linearGradient(listOf(PrimaryAccent, Color.Transparent))
                                    else Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Transparent))
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                inputNumber?.let { onSave(it) }
                            },
                            enabled = isValid
                        ) {
                            Text(
                                text = "SAVE",
                                color = if (isValid) PrimaryAccent else Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                if (isEdit) {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onRemove,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "REMOVE GOAL",
                            color = PinnedAccentSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

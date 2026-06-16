package com.example.prtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.WeightEntry
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.util.UUID

@Composable
fun LogWeightScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current
    var textValue by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val weightValue = textValue.toFloatOrNull() ?: 0f

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = PrimaryAccent)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LOG WEIGHT",
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryAccent
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (textValue.isEmpty()) "0.0" else "%.1f".format(weightValue),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp
                        ),
                        color = PrimaryAccent,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = appSettings.weightUnit,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val v = (textValue.toFloatOrNull() ?: 0f) - 0.1f
                        textValue = if (v < 0f) "0.0" else "%.1f".format(v)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "Decrease",
                        tint = TextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.size(32.dp))

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { value ->
                        val filtered = value.filter { c ->
                            c.isDigit() || c == '.'
                        }
                        val dotCount = filtered.count { it == '.' }
                        if (filtered.isEmpty()) {
                            textValue = ""
                        } else if (dotCount <= 1 && filtered.replace(".", "").length <= 5) {
                            textValue = filtered
                        }
                    },
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        textAlign = TextAlign.Center,
                        color = PrimaryAccent
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = PrimaryAccent,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    )
                )

                Spacer(modifier = Modifier.size(32.dp))

                IconButton(
                    onClick = {
                        val v = (textValue.toFloatOrNull() ?: 0f) + 0.1f
                        textValue = "%.1f".format(v)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Increase",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
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

            Spacer(modifier = Modifier.weight(1f))

            NeonButton(
                text = "LOG WEIGHT",
                onClick = {
                    val weight = textValue.toFloatOrNull() ?: 0f
                    if (weight > 0f) {
                        if (appSettings.hapticEnabled) {
                            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager).defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                            }
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        if (appSettings.soundEnabled) SoundEngine.playLogEntry()
                        val entry = WeightEntry(
                            id = UUID.randomUUID().toString(),
                            weight = weight,
                            date = System.currentTimeMillis(),
                            note = note.trim()
                        )
                        viewModel.addWeightEntry(entry)
                        navController.popBackStack()
                    }
                },
                enabled = (textValue.toFloatOrNull() ?: 0f) > 0f
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.MainActivity
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import kotlinx.coroutines.delay

@Composable
fun NotificationSettingsScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()

    var morningHour by remember(appSettings.morningReminderHour) {
        mutableStateOf(appSettings.morningReminderHour)
    }
    var morningMinute by remember(appSettings.morningReminderMinute) {
        mutableStateOf(appSettings.morningReminderMinute)
    }
    var eveningHour by remember(appSettings.eveningReviewHour) {
        mutableStateOf(appSettings.eveningReviewHour)
    }
    var eveningMinute by remember(appSettings.eveningReviewMinute) {
        mutableStateOf(appSettings.eveningReviewMinute)
    }

    var showMorningSaved by remember { mutableStateOf(false) }
    var showEveningSaved by remember { mutableStateOf(false) }

    LaunchedEffect(showMorningSaved) {
        if (showMorningSaved) {
            delay(2000L)
            showMorningSaved = false
        }
    }

    LaunchedEffect(showEveningSaved) {
        if (showEveningSaved) {
            delay(2000L)
            showEveningSaved = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryAccent
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NOTIFICATIONS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "WORKOUT REMINDER",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily training reminder time",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TimePickerRow(
                        label = "HR",
                        value = morningHour,
                        onDecrement = { morningHour = if (morningHour == 0) 23 else morningHour - 1 },
                        onIncrement = { morningHour = if (morningHour == 23) 0 else morningHour + 1 }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TimePickerRow(
                        label = "MIN",
                        value = morningMinute,
                        onDecrement = { morningMinute = if (morningMinute == 0) 55 else (morningMinute - 5).coerceAtLeast(0) },
                        onIncrement = { morningMinute = if (morningMinute >= 55) 0 else (morningMinute + 5).coerceAtMost(55) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonButton(
                        text = "SAVE REMINDER TIME",
                        onClick = {
                            viewModel.setMorningReminderTime(morningHour, morningMinute)
                            MainActivity.rescheduleNotifications(context)
                            showMorningSaved = true
                        }
                    )

                    if (showMorningSaved) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SAVED \u2713",
                            color = GoalComplete,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "DAILY REVIEW",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evening session summary time",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TimePickerRow(
                        label = "HR",
                        value = eveningHour,
                        onDecrement = { eveningHour = if (eveningHour == 0) 23 else eveningHour - 1 },
                        onIncrement = { eveningHour = if (eveningHour == 23) 0 else eveningHour + 1 }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TimePickerRow(
                        label = "MIN",
                        value = eveningMinute,
                        onDecrement = { eveningMinute = if (eveningMinute == 0) 55 else (eveningMinute - 5).coerceAtLeast(0) },
                        onIncrement = { eveningMinute = if (eveningMinute >= 55) 0 else (eveningMinute + 5).coerceAtMost(55) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonButton(
                        text = "SAVE REVIEW TIME",
                        onClick = {
                            viewModel.setEveningReviewTime(eveningHour, eveningMinute)
                            MainActivity.rescheduleNotifications(context)
                            showEveningSaved = true
                        }
                    )

                    if (showEveningSaved) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SAVED \u2713",
                            color = GoalComplete,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onDecrement) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = PrimaryAccent
            )
        }
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 40.sp),
            color = PrimaryAccent,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onIncrement) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                tint = PrimaryAccent
            )
        }
    }
}

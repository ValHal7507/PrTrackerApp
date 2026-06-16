package com.example.prtracker.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.NeonButton
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.io.File

@Composable
fun SyncExportScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showShared by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val exercisesCount = viewModel.exercises.value.size
    val goalsCount = viewModel.goals.value.size
    val weightCount = viewModel.weightEntries.value.size
    val presetCount = viewModel.workoutPresets.value.size
    val runCount = viewModel.runEntries.value.size
    val historyCount = viewModel.workoutHistory.value.size

    fun shareFile() {
        try {
            val json = viewModel.generateExportJson()
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            val file = File(shareDir, "prtracker_backup.json")
            file.writeText(json)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PRTracker data"))
            showShared = true
        } catch (e: Exception) {
            errorMessage = e.message ?: "Export failed"
        }
    }

    if (showShared) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\u2713",
                style = MaterialTheme.typography.displayLarge,
                color = PrimaryAccent
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "DATA SHARED",
                style = MaterialTheme.typography.displayLarge,
                color = PrimaryAccent,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$exercisesCount exercises \u00b7 $goalsCount goals \u00b7 $weightCount weight \u00b7 $presetCount presets \u00b7 $runCount runs \u00b7 $historyCount workouts",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            NeonButton(
                text = "DONE",
                onClick = { navController.popBackStack() }
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "EXPORT DATA",
                style = MaterialTheme.typography.displayLarge,
                color = PrimaryAccent,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = PrimaryAccent.copy(alpha = 0.3f),
                            topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.8f, 2.dp.toPx()),
                            cornerRadius = CornerRadius(1.dp.toPx())
                        )
                    },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share your data with another device",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlowingCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = PrimaryAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$exercisesCount exercises \u00b7 $goalsCount goals \u00b7 $weightCount weight \u00b7 $presetCount presets \u00b7 $runCount runs \u00b7 $historyCount workouts",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color(0xFFFF003C),
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            NeonButton(
                text = "SHARE FILE",
                onClick = { shareFile() }
            )

            Spacer(modifier = Modifier.weight(1f))

            NeonButton(
                text = "BACK",
                onClick = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

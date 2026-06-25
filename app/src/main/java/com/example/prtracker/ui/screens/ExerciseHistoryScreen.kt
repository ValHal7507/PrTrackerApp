package com.example.prtracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.exerciseAccentColor
import com.example.prtracker.ui.theme.pinnedAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ExerciseHistoryItem(
    val exerciseId: String,
    val id: String,
    val exerciseName: String,
    val value: Int,
    val type: String,
    val date: Long,
    val note: String,
    val wasPR: Boolean,
    val xpEarned: Long = 0L
)

@Composable
fun ExerciseHistoryScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val appearance = LocalAppearance.current

    val historyItems = remember(viewModel.exercises.value) {
        val allItems = mutableListOf<ExerciseHistoryItem>()
        for (exercise in viewModel.exercises.value) {
            val sortedEntries = exercise.entries.sortedBy { it.date }
            var runningMax = 0
            val entryPRMap = mutableMapOf<String, Boolean>()
            for (entry in sortedEntries) {
                if (entry.value > runningMax) {
                    entryPRMap[entry.id] = true
                } else {
                    entryPRMap[entry.id] = false
                }
                if (entry.value > runningMax) runningMax = entry.value
            }
            for (entry in exercise.entries) {
                allItems.add(
                    ExerciseHistoryItem(
                        exerciseId = exercise.id,
                        id = entry.id,
                        exerciseName = exercise.name,
                        value = entry.value,
                        type = exercise.type,
                        date = entry.date,
                        note = entry.note,
                        wasPR = entryPRMap[entry.id] ?: false,
                        xpEarned = entry.xpEarned
                    )
                )
            }
        }
        allItems.sortedByDescending { it.date }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    var searchQuery by remember { mutableStateOf("") }
    var deleteStep by remember { mutableIntStateOf(0) }
    var deletingItem by remember { mutableStateOf<ExerciseHistoryItem?>(null) }

    val filteredItems = remember(historyItems, searchQuery) {
        if (searchQuery.isBlank()) historyItems
        else historyItems.filter { it.exerciseName.contains(searchQuery, ignoreCase = true) }
    }

    deletingItem?.let { item ->
        val suffix = if (item.type == "hold") "s" else " reps"
        if (deleteStep == 1) {
            AlertDialog(
                onDismissRequest = { deleteStep = 0; deletingItem = null },
                title = { Text("Delete Entry", color = TextPrimary) },
                text = { Text("Delete this entry from \"${item.exerciseName}\"?", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { deleteStep = 2 }) {
                        Text("Delete", color = appearance.exerciseAccentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteStep = 0; deletingItem = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Background
            )
        }
        if (deleteStep == 2) {
            AlertDialog(
                onDismissRequest = { deleteStep = 0; deletingItem = null },
                title = { Text("Are You Sure?", color = TextPrimary) },
                text = { Text("The entry value of ${item.value}$suffix will be permanently lost.", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { deleteStep = 3 }) {
                        Text("Delete", color = appearance.exerciseAccentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteStep = 0; deletingItem = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Background
            )
        }
        if (deleteStep == 3) {
            AlertDialog(
                onDismissRequest = { deleteStep = 0; deletingItem = null },
                title = { Text("Final Confirmation", color = TextPrimary) },
                text = { Text("This action cannot be undone. Confirm deletion?", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteEntry(item.exerciseId, item.id)
                        deleteStep = 0
                        deletingItem = null
                    }) {
                        Text("Delete Permanently", color = Color(0xFFFF003C))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteStep = 0; deletingItem = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Background
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 48.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = appearance.exerciseAccentColor
                    )
                }
                Text(
                    text = "EXERCISE HISTORY",
                    style = MaterialTheme.typography.displayLarge,
                    color = appearance.exerciseAccentColor,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = appearance.exerciseAccentColor.copy(alpha = 0.3f),
                                topLeft = Offset(size.width * 0.1f, size.height - 2.dp.toPx()),
                                size = Size(size.width * 0.8f, 2.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx())
                            )
                        },
                    textAlign = TextAlign.Center
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search exercises...", color = TextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = appearance.exerciseAccentColor,
                    focusedBorderColor = appearance.exerciseAccentColor,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (historyItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO ENTRIES YET",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO MATCHES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val accentColor = if (item.wasPR) appearance.pinnedAccentColor else appearance.exerciseAccentColor
                        val suffix = if (item.type == "hold") "s" else " reps"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawRoundRect(
                                        color = CardBackground,
                                        cornerRadius = CornerRadius(8.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = accentColor,
                                        topLeft = Offset.Zero,
                                        size = Size(4.dp.toPx(), size.height),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 0f)
                                    )
                                }
                                .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.exerciseName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.value}$suffix",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = accentColor,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (item.xpEarned > 0L) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "+${item.xpEarned} XP",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = GoalComplete,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    if (item.note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "  \u00B7  ${item.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    IconButton(
                                        onClick = { deletingItem = item; deleteStep = 1 },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete entry",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = dateFormat.format(Date(item.date)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    if (item.wasPR) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.EmojiEvents,
                                                contentDescription = "PR",
                                                tint = appearance.pinnedAccentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "PR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = appearance.pinnedAccentColor,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

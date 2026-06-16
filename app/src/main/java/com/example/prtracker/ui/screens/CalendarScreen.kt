package com.example.prtracker.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.SoundEngine
import com.example.prtracker.data.WorkoutPreset
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.BiomechanicalRadarCard
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.Background
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.GoalComplete
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.pinnedSecondaryColor
import com.example.prtracker.ui.theme.PrimaryAccent
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.ui.theme.systemSecondaryColor
import com.example.prtracker.ui.theme.TextPrimary
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.viewmodel.PRViewModel
import java.util.Calendar

private enum class CalendarCellType {
    WORKOUT,
    REST,
    MISSED,
    FUTURE,
    EMPTY_PAD
}

private data class CalendarDay(
    val dateString: String,
    val dayNumber: Int = 0,
    val cellType: CalendarCellType = CalendarCellType.EMPTY_PAD
)

private data class SelectedDayInfo(
    val dateString: String,
    val dayNumber: Int,
    val cellType: CalendarCellType,
    val exercises: List<Triple<String, Int, String>>,
    val viewMode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: PRViewModel,
    navController: NavHostController
) {
    val exercises by viewModel.exercises.collectAsState()
    val restDays by viewModel.restDays.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val presets by viewModel.workoutPresets.collectAsState()
    val appearance = LocalAppearance.current

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.hapticEvent.collect {
            if (viewModel.appSettings.value.hapticEnabled) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
    }

    val today = remember { Calendar.getInstance() }
    val currentYear = today.get(Calendar.YEAR)
    val currentMonth = today.get(Calendar.MONTH)
    var displayedYear by remember { mutableIntStateOf(currentYear) }
    var displayedMonth by remember { mutableIntStateOf(currentMonth) }

    val todayStr = remember { System.currentTimeMillis().toDateStringCalendar() }

    val workoutDays = remember(exercises) {
        exercises.flatMap { it.entries }
            .map { it.date.toDateStringCalendar() }
            .toHashSet()
    }

    val workoutDaysMap = remember(exercises, appSettings.calendarDayViewMode) {
        val map = mutableMapOf<String, List<Triple<String, Int, String>>>()
        val mode = appSettings.calendarDayViewMode
        for (ex in exercises) {
            val valuesPerDate = mutableMapOf<String, MutableList<Int>>()
            for (entry in ex.entries) {
                val dateStr = entry.date.toDateStringCalendar()
                valuesPerDate.getOrPut(dateStr) { mutableListOf() }.add(entry.value)
            }
            for ((dateStr, values) in valuesPerDate) {
                if (values.isEmpty()) continue
                val result = if (mode == "sum") values.sum() else values.max()
                val list = map.getOrPut(dateStr) { emptyList() }
                map[dateStr] = list + Triple(ex.name, result, ex.type)
            }
        }
        map
    }

    val restDaySet = remember(restDays) {
        restDays.toHashSet()
    }

    val calendarDays = remember(displayedYear, displayedMonth, workoutDays, restDaySet) {
        buildCalendarDays(displayedYear, displayedMonth, workoutDays, restDaySet)
    }

    val monthNames = arrayOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    var selectedDay by remember { mutableStateOf<SelectedDayInfo?>(null) }

    val animatedStreak by animateIntAsState(
        targetValue = currentStreak,
        animationSpec = tween(800),
        label = "streak"
    )

    val restDaysThisMonth = remember(displayedYear, displayedMonth, restDays) {
        val prefix = "%04d-%02d".format(displayedYear, displayedMonth + 1)
        restDays.count { it.startsWith(prefix) }
    }

    val isTodayRestDay = remember(restDays) { todayStr in restDays }

    val headerGlowBrush = remember {
        Brush.horizontalGradient(
            listOf(Color.Transparent, appearance.systemAccentColor, Color.Transparent)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp)
            ) {
                Text(
                    text = "SYSTEM CHRONO // OVERVIEW",
                    style = MaterialTheme.typography.displayLarge,
                    color = appearance.systemAccentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = appearance.systemAccentColor.copy(alpha = 0.3f),
                                topLeft = Offset(
                                    size.width * 0.1f,
                                    size.height - 2.dp.toPx()
                                ),
                                size = Size(size.width * 0.8f, 2.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx())
                            )
                        },
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    GlowingCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "\uD83D\uDD25  CURRENT STREAK: ${animatedStreak} DAYS",
                                style = MaterialTheme.typography.titleLarge,
                                color = appearance.systemAccentColor,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            if (currentStreak > 0) {
                                Text(
                                    text = "($restDaysThisMonth rest days this month)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            } else {
                                Text(
                                    text = "NO ACTIVE STREAK",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    BiomechanicalRadarCard(exercises = exercises)
                }

                Spacer(modifier = Modifier.height(8.dp))

                MovementAnalysisSection(
                    presets = presets,
                    navController = navController
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            displayedMonth--
                            if (displayedMonth < 0) {
                                displayedMonth = 11
                                displayedYear--
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = appearance.systemAccentColor
                        )
                    }

                    AnimatedContent(
                        targetState = displayedYear to displayedMonth,
                        transitionSpec = {
                            val direction = if (targetState.first > initialState.first ||
                                (targetState.first == initialState.first && targetState.second > initialState.second)
                            ) 1 else -1
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { fullWidth -> direction * fullWidth }
                            ) togetherWith
                                slideOutHorizontally(
                                    animationSpec = tween(300),
                                    targetOffsetX = { fullWidth -> -direction * fullWidth }
                                )
                        },
                        label = "monthYear"
                    ) { (year, month) ->
                        Text(
                            text = "${monthNames[month]} $year",
                            style = MaterialTheme.typography.headlineSmall,
                            color = appearance.systemAccentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = {
                            displayedMonth++
                            if (displayedMonth > 11) {
                                displayedMonth = 0
                                displayedYear++
                            }
                        },
                        enabled = displayedYear < currentYear ||
                                (displayedYear == currentYear && displayedMonth < currentMonth)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = if (displayedYear < currentYear ||
                                (displayedYear == currentYear && displayedMonth < currentMonth)
                            ) appearance.systemAccentColor else TextSecondary.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("SU", "MO", "TU", "WE", "TH", "FR", "SA").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                calendarDays.chunked(7).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        row.forEach { day ->
                            val isToday = day.dateString == todayStr
                            val dayExercises = workoutDaysMap[day.dateString] ?: emptyList()
                            Box(modifier = Modifier.weight(1f)) {
                                DayCell(
                                    day = day,
                                    isToday = isToday,
                                    onClick = {
                                        when (day.cellType) {
                                            CalendarCellType.WORKOUT -> {
                                                selectedDay = SelectedDayInfo(
                                                    dateString = day.dateString,
                                                    dayNumber = day.dayNumber,
                                                    cellType = day.cellType,
                                                    exercises = dayExercises,
                                                    viewMode = appSettings.calendarDayViewMode
                                                )
                                            }
                                            CalendarCellType.REST -> {
                                                selectedDay = SelectedDayInfo(
                                                    dateString = day.dateString,
                                                    dayNumber = day.dayNumber,
                                                    cellType = day.cellType,
                                                    exercises = emptyList(),
                                                    viewMode = appSettings.calendarDayViewMode
                                                )
                                            }
                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                        repeat(7 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                if (isTodayRestDay) {
                    Button(
                        onClick = {
                            if (appSettings.soundEnabled) SoundEngine.playCalendarRestDay()
                            viewModel.toggleTodayAsRestDay()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "UNMARK REST DAY",
                            color = appearance.pinnedSecondaryColor,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                BorderStroke(1.dp, appearance.pinnedSecondaryColor),
                                RoundedCornerShape(16.dp)
                            )
                    )
                } else {
                    Button(
                        onClick = {
                            if (appSettings.soundEnabled) SoundEngine.playCalendarRestDay()
                            viewModel.toggleTodayAsRestDay()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appearance.systemAccentColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = "MARK TODAY AS REST DAY",
                            color = appearance.systemAccentColor,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(appearance.systemAccentColor, Color.Transparent)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                    )
                }
            }
        }

        selectedDay?.let { info ->
            DayInfoDialog(
                info = info,
                onDismiss = { selectedDay = null }
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val appearance = LocalAppearance.current
    val cellModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)

    when (day.cellType) {
        CalendarCellType.EMPTY_PAD -> {
            Box(modifier = cellModifier)
        }
        CalendarCellType.FUTURE -> {
            Box(
                modifier = cellModifier
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isToday) {
                            Modifier.border(
                                2.dp,
                                appearance.systemAccentColor,
                                RoundedCornerShape(8.dp)
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${day.dayNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.2f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }
        CalendarCellType.MISSED -> {
            val missedColor = if (isToday) TextPrimary else TextSecondary.copy(alpha = 0.5f)
            Box(
                modifier = cellModifier
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isToday) {
                            Modifier.border(
                                2.dp,
                                appearance.systemAccentColor,
                                RoundedCornerShape(8.dp)
                            )
                        } else Modifier
                    )
                    .drawBehind {
                        if (!isToday) {
                            val underlineY = size.height - 4.dp.toPx()
                            drawLine(
                                color = appearance.pinnedSecondaryColor.copy(alpha = 0.2f),
                                start = Offset(size.width * 0.2f, underlineY),
                                end = Offset(size.width * 0.8f, underlineY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${day.dayNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = missedColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }
        CalendarCellType.REST -> {
            val bgColor = appearance.systemSecondaryColor.copy(alpha = 0.4f)
            Box(
                modifier = cellModifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .then(
                        if (isToday) {
                            Modifier.border(
                                2.dp,
                                appearance.systemAccentColor,
                                RoundedCornerShape(8.dp)
                            )
                        } else Modifier
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${day.dayNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = appearance.systemSecondaryColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "\u23F8",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            }
        }
        CalendarCellType.WORKOUT -> {
            val borderGlowColor = remember {
                Brush.linearGradient(
                    listOf(appearance.systemAccentColor, appearance.systemAccentColor.copy(alpha = 0.3f))
                )
            }
            Box(
                modifier = cellModifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBackground)
                    .border(1.5.dp, borderGlowColor, RoundedCornerShape(8.dp))
                    .then(
                        if (isToday) {
                            Modifier.border(
                                2.dp,
                                appearance.systemAccentColor,
                                RoundedCornerShape(8.dp)
                            )
                        } else Modifier
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${day.dayNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(appearance.systemAccentColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayInfoDialog(
    info: SelectedDayInfo,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppearance.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text(
                text = if (info.cellType == CalendarCellType.REST) {
                    "REST DAY"
                } else {
                    "WORKOUT DAY"
                },
                color = if (info.cellType == CalendarCellType.REST) appearance.systemSecondaryColor
                else appearance.systemAccentColor,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                Text(
                    text = info.dateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (info.cellType == CalendarCellType.REST) {
                    Text(
                        text = "Rest day \u2014 chain preserved",
                        style = MaterialTheme.typography.bodyLarge,
                        color = appearance.systemSecondaryColor,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    info.exercises.forEach { (name, value, type) ->
                        val suffix = if (type == "hold") "s" else " reps"
                        val prefix = if (info.viewMode == "sum") "Total" else "PR"
                        Text(
                            text = "$name \u2014 $prefix: $value$suffix",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (info.exercises.isEmpty()) {
                        Text(
                            text = "No exercises logged",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = appearance.systemAccentColor, fontFamily = FontFamily.Monospace)
            }
        }
    )
}

private fun buildCalendarDays(
    year: Int,
    month: Int,
    workoutDays: Set<String>,
    restDaySet: Set<String>
): List<CalendarDay> {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val todayStr = System.currentTimeMillis().toDateStringCalendar()

    val days = mutableListOf<CalendarDay>()

    for (i in 0 until firstDayOfWeek) {
        days.add(CalendarDay("pad_start_$i", 0, CalendarCellType.EMPTY_PAD))
    }

    for (day in 1..daysInMonth) {
        val dateStr = "%04d-%02d-%02d".format(year, month + 1, day)
        val cellType = when {
            dateStr in workoutDays -> CalendarCellType.WORKOUT
            dateStr in restDaySet -> CalendarCellType.REST
            dateStr > todayStr -> CalendarCellType.FUTURE
            else -> CalendarCellType.MISSED
        }
        days.add(CalendarDay(dateStr, day, cellType))
    }

    val remainder = days.size % 7
    if (remainder > 0) {
        for (i in 0 until (7 - remainder)) {
            days.add(CalendarDay("pad_end_$i", 0, CalendarCellType.EMPTY_PAD))
        }
    }

    return days
}

private fun Long.toDateStringCalendar(): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovementAnalysisSection(
    presets: List<WorkoutPreset>,
    navController: NavHostController
) {
    val appearance = LocalAppearance.current
    var expanded by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf<WorkoutPreset?>(null) }

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
        Text(
            text = "MOVEMENT ANALYSIS",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedPreset?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("PRESET", fontFamily = FontFamily.Monospace) },
                placeholder = { Text("SELECT PRESET", fontFamily = FontFamily.Monospace) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = PrimaryAccent,
                    focusedLabelColor = PrimaryAccent,
                    unfocusedLabelColor = TextSecondary
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (presets.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "NO PRESETS \u2014 CREATE ONE FIRST",
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    presets.forEach { preset ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = preset.name,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            onClick = {
                                selectedPreset = preset
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    selectedPreset?.let { preset ->
                        navController.navigate(Routes.presetAnalysis(preset.id))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedPreset != null,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPreset != null) appearance.systemAccentColor.copy(alpha = 0.15f) else Color(0xFF2A2A3E),
                    disabledContainerColor = Color(0xFF2A2A3E)
                )
            ) {
                Text(
                    text = "ANALYZE MOVEMENT \u2192",
                    color = if (selectedPreset != null) appearance.systemAccentColor else TextSecondary,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (selectedPreset != null) {
                val borderBrush = remember {
                    Brush.linearGradient(listOf(appearance.systemAccentColor, Color.Transparent))
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            BorderStroke(1.dp, borderBrush),
                            RoundedCornerShape(16.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

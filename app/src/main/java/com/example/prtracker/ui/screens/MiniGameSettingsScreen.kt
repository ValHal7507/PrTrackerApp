package com.example.prtracker.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PetRarity
import com.example.prtracker.data.PetUpgrade
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel

@Composable
fun MiniGameSettingsScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val accent = LocalAppearance.current.systemAccentColor
    val gold = Color(0xFFFFD700)
    val miniGameSettings by viewModel.miniGameSettings.collectAsState()
    val petUpgrades by viewModel.petUpgrades.collectAsState()
    val multiRollLevel = petUpgrades["multi_roll"] ?: 0
    val maxUnlocked = PetUpgrade.multiRollCount(multiRollLevel)

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = accent
                    )
                }
                Text(
                    text = "MINIGAME SETTINGS",
                    color = accent,
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Auto-Sell section ────────────────────────────────────────────
            GlowingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "AUTO-SELL",
                        color = gold,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sold on roll — still shown in showcase",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val autoSellSet = miniGameSettings.autoSellRarities

                    PetRarity.entries
                        .filter { it != PetRarity.EXCLUSIVE && it != PetRarity.SECRET }
                        .forEach { rarity ->
                            val isOn = autoSellSet.contains(rarity.name)
                            val rarityColor = Color(rarity.colorHex)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isOn) gold.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isOn) gold.copy(alpha = 0.3f)
                                        else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.setAutoSellRarity(rarity.name, !isOn)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rarity dot
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(rarityColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = rarity.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                // Checkmark box
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            1.dp,
                                            if (isOn) gold else TextSecondary.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .background(
                                            if (isOn) gold.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isOn) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = gold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                    Spacer(modifier = Modifier.height(6.dp))
                    // EXCLUSIVE row — always excluded
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7B35C1).copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(PetRarity.EXCLUSIVE.colorHex))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "EXCLUSIVE",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "ALWAYS KEPT",
                            color = TextSecondary.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    // SECRET row — always excluded
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(PetRarity.SECRET.colorHex).copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(PetRarity.SECRET.colorHex))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SECRET",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "ALWAYS KEPT",
                            color = TextSecondary.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Freeze Screen section ──────────────────────────────────────
            GlowingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FREEZE SCREEN",
                        color = gold,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Screen freezes for 5 seconds on roll — pause auto-roll",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val freezeSet = miniGameSettings.freezeRarities

                    PetRarity.entries.forEach { rarity ->
                        val isOn = freezeSet.contains(rarity.name)
                        val rarityColor = Color(rarity.colorHex)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isOn) gold.copy(alpha = 0.08f)
                                    else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isOn) gold.copy(alpha = 0.3f)
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.setFreezeRarity(rarity.name, !isOn)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(rarityColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = rarity.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        1.dp,
                                        if (isOn) gold else TextSecondary.copy(alpha = 0.5f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .background(
                                        if (isOn) gold.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isOn) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Roll Count section ───────────────────────────────────────────
            GlowingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ROLL COUNT",
                        color = gold,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "How many dice per roll (requires MULTI_ROLL upgrade)",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val rollOptions = listOf(1, 2, 3, 5)
                    val requiredLevels = listOf(0, 1, 2, 3)
                    val currentChoice = miniGameSettings.selectedRollCount

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rollOptions.forEachIndexed { index, count ->
                            val requiredLevel = requiredLevels[index]
                            val isUnlocked = multiRollLevel >= requiredLevel
                            val isSelected = if (currentChoice == 0) {
                                count == maxUnlocked
                            } else {
                                count == currentChoice
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected && isUnlocked -> gold.copy(alpha = 0.15f)
                                            else -> CardBackground
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            isSelected && isUnlocked -> gold.copy(alpha = 0.5f)
                                            else -> TextSecondary.copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = isUnlocked) {
                                        viewModel.setSelectedRollCount(count)
                                    }
                                    .padding(vertical = 14.dp)
                            ) {
                                if (isUnlocked) {
                                    Text(
                                        text = "${count}x",
                                        color = if (isSelected) gold else Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = TextSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${count}x",
                                        color = TextSecondary.copy(alpha = 0.4f),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Unlock hints
                    val missingLevels = rollOptions.zip(requiredLevels)
                        .filter { (count, reqLevel) -> multiRollLevel < reqLevel }
                    if (missingLevels.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val locked = missingLevels.joinToString(", ") { (count, _) -> "${count}x" }
                            Text(
                                text = "Upgrade MULTI_ROLL to unlock $locked",
                                color = TextSecondary.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "All roll counts unlocked",
                                color = gold.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

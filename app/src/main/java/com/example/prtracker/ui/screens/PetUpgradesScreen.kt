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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.prtracker.data.PetUpgrade
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel

private fun formatCoins(value: Long): String =
    java.text.NumberFormat.getIntegerInstance().format(value)

@Composable
fun PetUpgradesScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val appearance = LocalAppearance.current
    val accent = appearance.systemAccentColor

    val coins by viewModel.coins.collectAsState()
    val petUpgrades by viewModel.petUpgrades.collectAsState()
    val equippedPetIds by viewModel.equippedPetIds.collectAsState()
    val maxSlots = viewModel.maxEquipSlots()

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
        ) {
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
                    text = "PET UPGRADES",
                    color = accent,
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "\uD83E\uDE99",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatCoins(coins),
                    color = Color(0xFFFFD700),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 8.dp, bottom = 32.dp
                )
            ) {
                items(PetUpgrade.entries) { upgrade ->
                    UpgradeCard(
                        upgrade = upgrade,
                        currentLevel = petUpgrades[upgrade.id] ?: 0,
                        coins = coins,
                        accent = accent,
                        equippedCount = equippedPetIds.size,
                        onPurchase = { viewModel.purchaseUpgrade(upgrade) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    upgrade: PetUpgrade,
    currentLevel: Int,
    coins: Long,
    accent: Color,
    equippedCount: Int = 0,
    onPurchase: () -> Unit
) {
    val isEquipSlots = upgrade == PetUpgrade.EQUIP_SLOTS
    val maxLevel = upgrade.maxLevel()
    val isMaxed = maxLevel != null && currentLevel >= maxLevel
    val nextCost = if (isMaxed) Long.MAX_VALUE else upgrade.nextLevelCost(currentLevel)
    val canAfford = coins >= nextCost && !isMaxed

    GlowingCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = upgrade.displayName,
                        color = accent,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEquipSlots) "SLOTS: ${currentLevel + 2}/5" else "OWNED: $currentLevel",
                            color = Color(0xFF6B8CAE),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isMaxed) {
                            Text(
                                text = "MAX",
                                color = Color(0xFF00FF85),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Button(
                    onClick = { onPurchase() },
                    enabled = canAfford,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMaxed) Color(0xFF00FF85).copy(alpha = 0.15f) else if (canAfford) Color(0xFFFFD700).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                        disabledContainerColor = if (isMaxed) Color(0xFF00FF85).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = if (isMaxed) "MAX" else formatCoins(nextCost),
                        color = if (isMaxed) Color(0xFF00FF85) else if (canAfford) Color(0xFFFFD700) else Color.Gray,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = upgrade.description,
                color = Color(0xFF6B8CAE),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            val displaySegments = if (isEquipSlots) 3 else 20
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(displaySegments) { index ->
                    val filled = index < currentLevel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (filled) accent.copy(alpha = 0.7f)
                                else Color(0xFF6B8CAE).copy(alpha = 0.15f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            val effectText = when (upgrade) {
                PetUpgrade.LUCK -> "Luck: ${"%.1f".format(1.0 + currentLevel * 0.20)}x multiplier (+20% per level)"
                PetUpgrade.ROLL_SPEED -> {
                    val delay = maxOf(0, 1600 - currentLevel * 72)
                    if (delay == 0) "Speed: 0ms (MAX)"
                    else "Speed: ${delay}ms delay (-72ms per level)"
                }
                PetUpgrade.LUCKY_ROLL -> {
                    val boost = if (currentLevel > 0) 1.0 + currentLevel * 0.25 else 1.0
                    "Rarity boost: ${"%.2f".format(boost)}x on lucky rolls (+0.25x per level)"
                }
                PetUpgrade.COIN_MULTIPLIER -> "Coins: ${"%.1f".format(1.0 + currentLevel * 0.20)}x per roll (+0.20x per level)"
                PetUpgrade.EQUIP_SLOTS -> "Equipped: $equippedCount/${currentLevel + 2} slots used"
            }
            Text(
                text = effectText,
                color = Color(0xFF6B8CAE).copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

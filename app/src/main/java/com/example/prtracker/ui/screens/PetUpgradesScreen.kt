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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.PetUpgrade
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel

private fun formatCoins(value: Long): String {
    fun f(v: Long, u: Long, s: String) =
        if (v % u == 0L) "${v / u}$s" else String.format("%.3f$s", v / u.toDouble())
    return when {
        value >= 1_000_000_000_000_000L -> f(value, 1_000_000_000_000_000L, "Qd")
        value >= 1_000_000_000_000L     -> f(value, 1_000_000_000_000L, "T")
        value >= 1_000_000_000L         -> f(value, 1_000_000_000L, "B")
        value >= 1_000_000L             -> f(value, 1_000_000L, "M")
        value >= 1_000L                 -> f(value, 1_000L, "K")
        else                            -> value.toString()
    }
}

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
                        onPurchase = { count -> viewModel.purchaseUpgradeMultiple(upgrade, count) }
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
    onPurchase: (Int) -> Unit
) {
    val isEquipSlots = upgrade == PetUpgrade.EQUIP_SLOTS
    val maxLevel = upgrade.maxLevel()
    val isMaxed = maxLevel != null && currentLevel >= maxLevel
    val maxQuantity = if (isMaxed) 0 else upgrade.maxPurchaseableLevels(currentLevel, coins)
        .coerceAtMost(if (maxLevel != null) (maxLevel - currentLevel).coerceAtLeast(0) else 50)

    var quantityText by remember { mutableStateOf("1") }
    var quantity by remember { mutableIntStateOf(1) }

    fun syncQuantity(newText: String) {
        quantityText = newText
        val parsed = newText.toIntOrNull()
        if (parsed != null) {
            val clamped = parsed.coerceIn(1, maxQuantity.coerceAtLeast(1))
            quantity = if (maxQuantity > 0) clamped else 1
        }
    }

    if (maxQuantity > 0 && quantity > maxQuantity) {
        quantity = maxQuantity
        quantityText = maxQuantity.toString()
    }

    val totalCost = if (quantity <= 0 || isMaxed) Long.MAX_VALUE
    else upgrade.totalCostForLevels(currentLevel, quantity)
    val canAfford = coins >= totalCost && !isMaxed && quantity > 0

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
                PetUpgrade.MULTI_ROLL -> {
                    val currentRolls = PetUpgrade.multiRollCount(currentLevel)
                    if (currentLevel >= 3) "MAX: ${currentRolls}x dice per roll"
                    else "Currently: ${currentRolls}x dice \u2192 Next: ${PetUpgrade.multiRollCount(currentLevel + 1)}x dice"
                }
            }
            Text(
                text = effectText,
                color = Color(0xFF6B8CAE).copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )

            if (!isMaxed) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val new = (quantity - 1).coerceAtLeast(1)
                                quantity = new
                                quantityText = new.toString()
                            },
                            enabled = quantity > 1,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (quantity > 1) accent.copy(alpha = 0.15f)
                                    else Color.Gray.copy(alpha = 0.1f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (quantity > 1) accent else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D1526))
                                .border(
                                    width = 1.dp,
                                    color = accent.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = quantityText,
                                onValueChange = { newText ->
                                    if (newText.all { it.isDigit() } && newText.length <= 4) {
                                        syncQuantity(newText)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = accent,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(accent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = {
                                val new = (quantity + 1).coerceAtMost(maxQuantity.coerceAtLeast(1))
                                quantity = new
                                quantityText = new.toString()
                            },
                            enabled = quantity < maxQuantity,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (quantity < maxQuantity) accent.copy(alpha = 0.15f)
                                    else Color.Gray.copy(alpha = 0.1f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (quantity < maxQuantity) accent else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (quantity > 1) {
                        Text(
                            text = formatCoins(totalCost),
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { onPurchase(quantity) },
                        enabled = canAfford,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.15f),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = if (quantity > 1) "BUY $quantity" else "BUY",
                            color = if (canAfford) Color(0xFFFFD700) else Color.Gray,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (quantity > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${formatCoins(totalCost / quantity)} per level",
                            color = Color(0xFF6B8CAE).copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

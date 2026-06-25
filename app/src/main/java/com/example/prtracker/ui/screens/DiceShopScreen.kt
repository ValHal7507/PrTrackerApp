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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.SpecialDiceType
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.TextSecondary
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import java.text.NumberFormat

private fun formatCoins(value: Long): String =
    NumberFormat.getIntegerInstance().format(value)

@Composable
fun DiceShopScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val coins by viewModel.coins.collectAsState()
    val diceInventory by viewModel.diceInventory.collectAsState()
    val accent = LocalAppearance.current.systemAccentColor

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Title row with coin balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = accent
                    )
                }
                Text(
                    text = "DICE SHOP",
                    color = accent,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace
                )
                // Coin balance pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "\uD83E\uDE99", fontSize = 16.sp)
                    Text(
                        text = formatCoins(coins),
                        color = Color(0xFFFFD700),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dice list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(SpecialDiceType.entries) { diceType ->
                    val ownedCount = diceInventory.count { it.typeId == diceType.id }
                    DiceShopCard(
                        diceType = diceType,
                        coins = coins,
                        ownedCount = ownedCount,
                        onBuy = { count -> viewModel.buyDice(diceType.id, count) }
                    )
                }

                // Placeholder for future dice additions
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                accent.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            )
                            .background(CardBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\u2014 MORE DICE COMING SOON \u2014",
                            color = accent.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Bottom spacer for scroll
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun DiceShopCard(
    diceType: SpecialDiceType,
    coins: Long,
    ownedCount: Int,
    onBuy: (Int) -> Unit
) {
    val diceColor = diceType.toColor()
    val accent = LocalAppearance.current.systemAccentColor
    var quantity by remember(diceType) { mutableStateOf(1) }
    var quantityText by remember(diceType) { mutableStateOf("1") }
    val totalCost = diceType.price * quantity
    val canAfford = coins >= totalCost

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, diceColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Column {
            // Dice name + emoji row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = diceType.emoji, fontSize = 32.sp)
                Column {
                    Text(
                        text = diceType.displayName,
                        color = diceColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = diceType.description,
                        color = diceColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rolls count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(diceColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FOR ${diceType.rollsCount} ROLLS",
                        color = diceColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Owned count
                if (ownedCount > 0) {
                    Text(
                        text = "YOU OWN: $ownedCount",
                        color = Color(0xFF00FF85),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(diceColor.copy(alpha = 0.1f))
                        .clickable(enabled = quantity > 1) {
                            quantity--
                            quantityText = quantity.toString()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u2212",
                        color = if (quantity > 1) diceColor else diceColor.copy(alpha = 0.3f),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { newVal ->
                        quantityText = newVal
                        val parsed = newVal.toIntOrNull()
                        if (parsed != null) {
                            val clamped = parsed.coerceAtLeast(1)
                            quantity = clamped
                            if (clamped != parsed) {
                                quantityText = clamped.toString()
                            }
                        }
                    },
                    modifier = Modifier.width(50.dp).height(36.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        color = diceColor,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Plus button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(diceColor.copy(alpha = 0.1f))
                        .clickable {
                            quantity++
                            quantityText = quantity.toString()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = diceColor,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buy row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "\uD83E\uDE99", fontSize = 14.sp)
                    Text(
                        text = formatCoins(totalCost),
                        color = if (canAfford) Color(0xFFFFD700) else Color(0xFF6B8CAE),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    if (quantity > 1) {
                        Text(
                            text = "(\u00D7$quantity)",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Buy button
                Button(
                    onClick = { onBuy(quantity) },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = diceColor.copy(alpha = 0.3f),
                        disabledContainerColor = accent.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (canAfford) "BUY $quantity" else "CAN'T AFFORD",
                        color = if (canAfford) diceColor else Color(0xFF6B8CAE),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

package com.example.prtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun DiceInventoryScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val diceInventory by viewModel.diceInventory.collectAsState()
    val accent = LocalAppearance.current.systemAccentColor
    val grouped = remember(diceInventory) {
        diceInventory.groupBy { it.typeId }.mapValues { it.value.size }
    }
    val sortedEntries = remember(grouped) {
        grouped.entries.toList().sortedBy { (typeId, _) ->
            SpecialDiceType.fromId(typeId)?.let { SpecialDiceType.strengthOrder.indexOf(it) } ?: Int.MAX_VALUE
        }
    }
    var typeToActivate by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Title row
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
                    text = "DICE INVENTORY (${diceInventory.size})",
                    color = accent,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (grouped.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "\uD83C\uDFB2", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO SPECIAL DICE YET",
                            color = accent.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\u2014 VISIT THE SHOP \u2014",
                            color = accent.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedEntries, key = { it.key }) { (typeId, count) ->
                        val diceType = SpecialDiceType.fromId(typeId)
                        if (diceType != null) {
                            DiceGridCell(
                                diceType = diceType,
                                count = count,
                                onClick = { typeToActivate = typeId }
                            )
                        }
                    }
                }
            }
        }

        // Floating USE popup
        typeToActivate?.let { typeId ->
            val diceType = SpecialDiceType.fromId(typeId) ?: return@let
            val diceColor = diceType.toColor()
            val maxCount = grouped[typeId] ?: 1
            var quantity by remember(typeId) { mutableStateOf(1) }
            var quantityText by remember(typeId) { mutableStateOf("1") }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { typeToActivate = null },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, diceColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .width(220.dp)
                        .clickable(enabled = false) { /* block click-through */ }
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = diceType.emoji,
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = diceType.displayName,
                            color = diceColor,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${diceType.rollsCount} rolls \u00B7 ${diceType.description}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Quantity selector
                        Text(
                            text = "QUANTITY",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Minus button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
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
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            // Editable quantity field
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { newVal ->
                                    quantityText = newVal
                                    val parsed = newVal.toIntOrNull()
                                    if (parsed != null) {
                                        val clamped = parsed.coerceIn(1, maxCount)
                                        quantity = clamped
                                        if (clamped != parsed) {
                                            quantityText = clamped.toString()
                                        }
                                    }
                                },
                                modifier = Modifier.width(60.dp).height(44.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = diceColor,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            // Plus button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(diceColor.copy(alpha = 0.1f))
                                    .clickable(enabled = quantity < maxCount) {
                                        quantity++
                                        quantityText = quantity.toString()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    color = if (quantity < maxCount) diceColor else diceColor.copy(alpha = 0.3f),
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "/ $maxCount",
                            color = TextSecondary.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        if (maxCount > 1) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "MAX = ${maxCount * diceType.rollsCount} ROLLS",
                                color = diceColor.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // USE / MAX buttons row
                        if (maxCount > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(diceColor.copy(alpha = 0.15f))
                                        .border(1.dp, diceColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.useDiceByType(typeId, quantity.coerceIn(1, maxCount))
                                            typeToActivate = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "USE $quantity",
                                        color = diceColor,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(diceColor.copy(alpha = 0.2f))
                                        .border(1.dp, diceColor, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.useDiceByType(typeId, maxCount)
                                            typeToActivate = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "MAX",
                                        color = diceColor,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(diceColor.copy(alpha = 0.15f))
                                    .border(1.dp, diceColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.useDiceByType(typeId, quantity.coerceIn(1, maxCount))
                                        typeToActivate = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "USE $quantity",
                                    color = diceColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { typeToActivate = null }) {
                            Text(
                                text = "CANCEL",
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiceGridCell(
    diceType: SpecialDiceType,
    count: Int,
    onClick: () -> Unit
) {
    val diceColor = diceType.toColor()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, diceColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = diceType.emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\u00D7$count",
                color = diceColor,
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

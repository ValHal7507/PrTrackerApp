package com.example.prtracker.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Pet
import com.example.prtracker.data.PetCatalog
import com.example.prtracker.data.PetRarity
import com.example.prtracker.data.PetTier
import com.example.prtracker.data.coinValue
import com.example.prtracker.data.xpMultiplier
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import kotlin.math.roundToInt

internal enum class InventorySortMode(val label: String) {
    TYPE("TYPE"), RARITY("RARITY"), VALUE("VALUE"), XP("XP")
}

private fun formatCoins(value: Int): String =
    java.text.NumberFormat.getIntegerInstance().format(value)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PetInventoryScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val appearance = LocalAppearance.current
    val accent = appearance.systemAccentColor

    val petInventory by viewModel.petInventory.collectAsState()
    val equippedPetIds by viewModel.equippedPetIds.collectAsState()
    val maxSlots = viewModel.maxEquipSlots()

    var selectedPetId by remember { mutableStateOf<String?>(null) }
    val selectedPet = selectedPetId?.let { id -> petInventory.find { it.id == id } }

    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(InventorySortMode.VALUE) }
    var sortAscending by remember { mutableStateOf(false) }
    var showSellAllDialog by remember { mutableStateOf(false) }
    var showFuseAllDialog by remember { mutableStateOf(false) }

    val filteredPets = petInventory.filter { pet ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.trim().lowercase()
            val species = PetCatalog.allSpecies.find { it.id == pet.speciesId }
            species?.name?.lowercase()?.contains(q) == true ||
                    pet.rarity.lowercase().contains(q) ||
                    pet.tier.lowercase().replace("_", " ").contains(q)
        }
    }
    val sortedPets = filteredPets.sortedWith(
        compareByDescending<Pet> { it.isFavorited }
            .thenByDescending {
                when (sortMode) {
                    InventorySortMode.TYPE   -> PetTier.fromName(it.tier).order
                    InventorySortMode.RARITY -> PetRarity.fromName(it.rarity).ordinal
                    InventorySortMode.VALUE  -> it.coinValue()
                    InventorySortMode.XP     -> (it.xpMultiplier() * 1000).toInt()
                }
            }
    ).let { list -> if (sortAscending) list.reversed() else list }

    val unfavoritedCount = petInventory.count { !it.isFavorited }
    val fusableCount = petInventory.count {
        it.stars == 5 && PetTier.fromName(it.tier) != PetTier.RED_MATTER
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        text = "INVENTORY (${petInventory.size})",
                        color = accent,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ── Search field ────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = {
                    Text(
                        text = "Search name, rarity, or type...",
                        color = Color(0xFF6B8CAE),
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = accent.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF6B8CAE),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color(0xFF6B8CAE).copy(alpha = 0.3f),
                    cursorColor = accent,
                    focusedTextColor = Color(0xFFE8F4FD),
                    unfocusedTextColor = Color(0xFFE8F4FD),
                    focusedContainerColor = Color(0xFF0D1526),
                    unfocusedContainerColor = Color(0xFF0D1526)
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                )
            )

            // ── Sort controls ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var sortExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { sortExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = accent
                        )
                    ) {
                        Text(
                            text = "SORT: ${sortMode.label}",
                            color = accent,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        InventorySortMode.entries.forEach { mode ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.label,
                                        color = if (mode == sortMode) accent else Color(0xFFE8F4FD),
                                        fontFamily = FontFamily.Monospace
                                    )
                                },
                                onClick = {
                                    sortMode = mode
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { sortAscending = !sortAscending },
                    modifier = Modifier
                        .size(44.dp)
                        .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (sortAscending)
                            Icons.Default.ArrowUpward
                        else
                            Icons.Default.ArrowDownward,
                        contentDescription = if (sortAscending) "Ascending" else "Descending",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── Grid or inline pet detail ───────────────────────────────────
            if (selectedPet != null) {
                PetDetailView(
                    pet = selectedPet,
                    accent = accent,
                    isEquipped = equippedPetIds.contains(selectedPet.id),
                    canEquip = equippedPetIds.size < maxSlots,
                    onEquip = { viewModel.equipPet(selectedPet.id) },
                    onUnequip = { viewModel.unequipPet(selectedPet.id) },
                    onFuse = {
                        viewModel.fusePet(selectedPet.id)
                        selectedPetId = null
                    },
                    onSell = {
                        viewModel.sellPet(selectedPet.id)
                        selectedPetId = null
                    },
                    onFavorite = { viewModel.toggleFavorite(selectedPet.id) },
                    onDismiss = { selectedPetId = null }
                )
            } else {
                if (filteredPets.isEmpty() && petInventory.isNotEmpty()) {
                    Text(
                        text = "NO MATCHES",
                        color = Color(0xFF6B8CAE),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(
                            items = sortedPets,
                            key = { it.id }
                        ) { pet ->
                            PetCollectionCard(
                                pet = pet,
                                accent = accent,
                                isEquipped = equippedPetIds.contains(pet.id),
                                onClick = { selectedPetId = pet.id },
                                onLongClick = { viewModel.toggleFavorite(pet.id) }
                            )
                        }
                    }
                }

                // ── Bulk action buttons ──────────────────────────────────────
                if (fusableCount > 0 || unfavoritedCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (fusableCount > 0) {
                            Button(
                                onClick = { showFuseAllDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)
                                )
                            ) {
                                Text(
                                    text = "FUSE ALL ($fusableCount)",
                                    color = Color(0xFFFFD700),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (unfavoritedCount > 0) {
                            Button(
                                onClick = { showSellAllDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF4444).copy(alpha = 0.15f)
                                )
                            ) {
                                Text(
                                    text = "SELL ALL ($unfavoritedCount)",
                                    color = Color(0xFFFF4444),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        } // end Column
    } // end root Box

    // ── Sell-all confirmation dialog ─────────────────────────────────────────
    if (showSellAllDialog) {
        val unfavorited = petInventory.filter { !it.isFavorited }
        val totalValue = unfavorited.sumOf { it.coinValue().toLong() }
        AlertDialog(
            onDismissRequest = { showSellAllDialog = false },
            containerColor = Color(0xFF0D1526),
            title = {
                Text(
                    text = "SELL ${unfavorited.size} PETS?",
                    color = Color(0xFFFF4444),
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "You will receive ${formatCoins(totalValue.toInt())} coins. Favorited pets will be kept.",
                    color = Color(0xFF6B8CAE),
                    fontFamily = FontFamily.Monospace
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSellAllDialog = false
                        viewModel.sellAllUnfavorited()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4444).copy(alpha = 0.2f)
                    )
                ) {
                    Text("SELL", color = Color(0xFFFF4444), fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSellAllDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B8CAE).copy(alpha = 0.1f)
                    )
                ) {
                    Text("CANCEL", color = Color(0xFF6B8CAE), fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // ── Fuse-all confirmation dialog ─────────────────────────────────────────
    if (showFuseAllDialog) {
        val fusable = petInventory.filter {
            it.stars == 5 && PetTier.fromName(it.tier) != PetTier.RED_MATTER
        }
        AlertDialog(
            onDismissRequest = { showFuseAllDialog = false },
            containerColor = Color(0xFF0D1526),
            title = {
                Text(
                    text = "FUSE ${fusable.size} PETS?",
                    color = Color(0xFFFFD700),
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "All 5-star pets will be fused to the next tier. Existing pets at the target tier may receive a star upgrade.",
                    color = Color(0xFF6B8CAE),
                    fontFamily = FontFamily.Monospace
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFuseAllDialog = false
                        viewModel.fuseAllPets()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    )
                ) {
                    Text("FUSE", color = Color(0xFFFFD700), fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showFuseAllDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B8CAE).copy(alpha = 0.1f)
                    )
                ) {
                    Text("CANCEL", color = Color(0xFF6B8CAE), fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

// ── Shared composables (used by both PetInventoryScreen and DiceRollScreen) ──

@Composable
internal fun PetDetailView(
    pet: Pet,
    accent: Color,
    isEquipped: Boolean,
    canEquip: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onFuse: () -> Unit,
    onSell: () -> Unit,
    onFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val tier = remember(pet.tier) { PetTier.fromName(pet.tier) }
    val tierColor = Color(tier.colorHex)
    val rarityColor = Color(PetRarity.fromName(pet.rarity).colorHex)
    val species = PetCatalog.allSpecies.find { it.id == pet.speciesId }
    val nextTier = remember(tier) { PetTier.nextTier(tier) }
    val canFuse = pet.stars >= 5 && nextTier != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .background(tierColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .border(2.dp, tierColor, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = species?.emoji ?: "?", fontSize = 72.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pet.name,
            color = tierColor,
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(rarityColor.copy(alpha = 0.2f))
                    .border(1.dp, rarityColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = pet.rarity,
                    color = rarityColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(tierColor.copy(alpha = 0.2f))
                    .border(1.dp, tierColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tier.label,
                    color = tierColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = buildString {
                repeat(pet.stars) { append("\u2605") }
                repeat(5 - pet.stars) { append("\u2606") }
            },
            color = if (pet.stars >= 3) Color(0xFFFFD700) else tierColor,
            fontSize = 28.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(20.dp))

        val xpMult = pet.xpMultiplier()
        if (xpMult > 1.0f) {
            Text(
                text = "XP MULT: ${String.format("%.2fx", xpMult)}",
                color = Color(0xFF00FF85),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Equip / unequip
        Button(
            onClick = { if (isEquipped) onUnequip() else onEquip() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = isEquipped || canEquip,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEquipped)
                    Color(0xFF00FF85).copy(alpha = 0.15f)
                else
                    Color(0xFF00F5FF).copy(alpha = 0.1f),
                disabledContainerColor = Color(0xFF6B8CAE).copy(alpha = 0.05f)
            )
        ) {
            Text(
                text = if (isEquipped) "UNEQUIP" else "EQUIP",
                color = when {
                    isEquipped -> Color(0xFF00FF85)
                    canEquip   -> Color(0xFF00F5FF)
                    else       -> Color(0xFF6B8CAE)
                },
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Favourite
        Button(
            onClick = { onFavorite() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (pet.isFavorited)
                    Color(0xFFFFD700).copy(alpha = 0.15f)
                else
                    Color(0xFF6B8CAE).copy(alpha = 0.1f)
            )
        ) {
            Icon(
                imageVector = if (pet.isFavorited) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = "Favorite",
                tint = if (pet.isFavorited) Color(0xFFFFD700) else Color(0xFF6B8CAE),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (pet.isFavorited) "FAVORITED" else "FAVORITE",
                color = if (pet.isFavorited) Color(0xFFFFD700) else Color(0xFF6B8CAE),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fuse
        if (canFuse) {
            Button(
                onClick = { onFuse() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tierColor.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = "FUSE \u2192 ${nextTier!!.label}",
                    color = tierColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sell
        var showSellDialog by remember { mutableStateOf(false) }

        Button(
            onClick = { showSellDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4444).copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = "SELL FOR ${formatCoins(pet.coinValue())} COINS",
                color = Color(0xFFFF4444),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
        }

        if (showSellDialog) {
            AlertDialog(
                onDismissRequest = { showSellDialog = false },
                containerColor = Color(0xFF0D1526),
                title = {
                    Text(
                        text = "SELL ${pet.name}?",
                        color = Color(0xFFFF4444),
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Text(
                        text = "You will receive ${formatCoins(pet.coinValue())} coins.",
                        color = Color(0xFF6B8CAE),
                        fontFamily = FontFamily.Monospace
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSellDialog = false
                            onSell()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4444).copy(alpha = 0.2f)
                        )
                    ) {
                        Text("SELL", color = Color(0xFFFF4444), fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showSellDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B8CAE).copy(alpha = 0.1f)
                        )
                    ) {
                        Text("CANCEL", color = Color(0xFF6B8CAE), fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TAP TO DISMISS",
            color = Color(0xFF6B8CAE),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PetCollectionCard(
    pet: Pet,
    accent: Color,
    isEquipped: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tier = remember(pet.tier) { PetTier.fromName(pet.tier) }
    val tierColor = Color(tier.colorHex)
    val rarity = remember(pet.rarity) { PetRarity.fromName(pet.rarity) }
    val rarityColor = Color(rarity.colorHex)
    val species = PetCatalog.allSpecies.find { it.id == pet.speciesId }
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "petTier")

    val borderBrush = remember(rarityColor) {
        Brush.linearGradient(listOf(rarityColor.copy(alpha = 0.7f), rarityColor))
    }

    val bgTint = when (tier) {
        PetTier.NORMAL      -> CardBackground
        PetTier.SILVER      -> Color(0xFFC0C0C0).copy(alpha = 0.08f)
        PetTier.GOLDEN      -> Color(0xFFFFD700).copy(alpha = 0.10f)
        PetTier.RAINBOW     -> Color(0xFFFF44FF).copy(alpha = 0.08f)
        PetTier.DARK_MATTER -> Color(0xFF6A0DAD).copy(alpha = 0.12f)
        PetTier.RED_MATTER  -> Color(0xFFDC143C).copy(alpha = 0.10f)
    }

    val contentModifier = when (tier) {
        PetTier.SILVER -> Modifier.drawBehind {
            val shimmerX = (size.width * 0.3f) + (size.width * 0.4f *
                    ((System.currentTimeMillis() % 3000L) / 3000f))
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(shimmerX - 20.dp.toPx(), 0f),
                end = Offset(shimmerX + 20.dp.toPx(), size.height),
                strokeWidth = 8.dp.toPx()
            )
        }
        PetTier.RAINBOW -> {
            val hue by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rainbowBgHue"
            )
            Modifier.background(Color.hsl(hue, 0.6f, 0.1f), RoundedCornerShape(12.dp))
        }
        PetTier.DARK_MATTER -> Modifier.drawBehind {
            drawCircle(
                color = Color(0xFF9B30FF).copy(alpha = 0.08f),
                radius = size.minDimension * 0.6f
            )
        }
        PetTier.RED_MATTER -> {
            val shakeX by infiniteTransition.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heatShake"
            )
            Modifier.offset { IntOffset(shakeX.roundToInt(), 0) }
        }
        else -> Modifier
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .background(bgTint, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderBrush, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .then(contentModifier),
        contentAlignment = Alignment.Center
    ) {
        if (pet.isFavorited) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorited",
                tint = Color(0xFFFFD700),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
        if (isEquipped) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Equipped",
                tint = Color(0xFF00FF85),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = species?.emoji ?: "?", fontSize = 28.sp)
            Text(
                text = buildString {
                    repeat(pet.stars) { append("\u2605") }
                    repeat(5 - pet.stars) { append("\u2606") }
                },
                color = if (pet.stars >= 3) Color(0xFFFFD700) else tierColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            if (tier != PetTier.NORMAL) {
                Text(
                    text = tier.label,
                    color = tierColor,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

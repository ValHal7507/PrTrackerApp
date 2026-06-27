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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.prtracker.data.Pet
import com.example.prtracker.data.PetCatalog
import com.example.prtracker.data.PetRarity
import com.example.prtracker.data.PetSpecies
import com.example.prtracker.data.PetTier
import com.example.prtracker.data.xpMultiplier
import com.example.prtracker.ui.components.GlowingCard
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel

private val goldColor = Color(0xFFFFD700)
private val grayColor = Color(0xFF6B8CAE)
private val dimColor = Color(0xFF444444)
private val overlayColor = Color(0xCC000000)

@Composable
fun PetIndexScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val accent = LocalAppearance.current.systemAccentColor
    val speciesTierCounts by viewModel.speciesTierCounts.collectAsState()
    val petInventory by viewModel.petInventory.collectAsState()

    var selectedTier by remember { mutableStateOf(PetTier.NORMAL) }

    // Sort: rarity ordinal then alphabetical name
    val sortedSpecies = remember {
        PetCatalog.allSpecies.sortedWith(compareBy<PetSpecies> { it.rarity.ordinal }.thenBy { it.name })
    }

    // Detail overlay state
    var selectedPet by remember { mutableStateOf<PetSpecies?>(null) }
    var selectedTierForDetail by remember { mutableStateOf(PetTier.NORMAL) }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = accent
                    )
                }
                Text(
                    text = "PET INDEX",
                    color = accent,
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Tier filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PetTier.entries.forEach { tier ->
                    val isSelected = tier == selectedTier
                    val pillColor = Color(tier.colorHex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) pillColor.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSelected) pillColor else pillColor.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTier = tier }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tier.label.split(" ").first(), // "NORMAL", "SILVER", etc.
                            color = if (isSelected) pillColor else pillColor.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Species grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedSpecies) { species ->
                    val key = "${species.id}_${selectedTier.name}"
                    val count = speciesTierCounts[key] ?: 0
                    val isUnlocked = count > 0
                    val rarityColor = Color(species.rarity.colorHex)

                    PetIndexCard(
                        species = species,
                        count = count,
                        isUnlocked = isUnlocked,
                        rarityColor = rarityColor,
                        onClick = {
                            selectedPet = species
                            selectedTierForDetail = selectedTier
                        }
                    )
                }
            }
        }

        // Full-screen pet detail overlay
        AnimatedVisibility(
            visible = selectedPet != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedPet?.let { species ->
                val key = "${species.id}_${selectedTierForDetail.name}"
                val count = speciesTierCounts[key] ?: 0

                PetDetailOverlay(
                    species = species,
                    tier = selectedTierForDetail,
                    count = count,
                    accent = accent,
                    petInventory = petInventory,
                    onDismiss = { selectedPet = null }
                )
            }
        }
    }
}

@Composable
private fun PetIndexCard(
    species: PetSpecies,
    count: Int,
    isUnlocked: Boolean,
    rarityColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1526))
            .border(
                1.5.dp,
                if (isUnlocked) rarityColor else dimColor,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pet emoji or lock
            Text(
                text = if (isUnlocked) species.emoji else "\u2753", // ❓
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Pet name (truncated)
            Text(
                text = species.name,
                color = if (isUnlocked) Color(0xFFE8F4FD) else dimColor,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
        // Count badge
        if (isUnlocked) {
            Text(
                text = "×$count",
                color = rarityColor,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun PetDetailOverlay(
    species: PetSpecies,
    tier: PetTier,
    count: Int,
    accent: Color,
    petInventory: List<Pet>,
    onDismiss: () -> Unit
) {
    var selectedStar by remember { mutableIntStateOf(1) }
    val rarityColor = Color(species.rarity.colorHex)
    val tierColor = Color(tier.colorHex)
    val isSuper = species.rarity == PetRarity.SUPER
    val isExclusive = species.rarity == PetRarity.EXCLUSIVE
    val isSecret = species.rarity == PetRarity.SECRET
    val isPremium = isSuper || isExclusive || isSecret

    // Compute best non-SUPER/EXCLUSIVE/SECRET XP multiplier from inventory
    val bestNonSuperMult = remember(petInventory) {
        petInventory
            .filter { PetRarity.fromName(it.rarity) != PetRarity.SUPER && PetRarity.fromName(it.rarity) != PetRarity.EXCLUSIVE && PetRarity.fromName(it.rarity) != PetRarity.SECRET }
            .maxOfOrNull { it.xpMultiplier(petInventory) } ?: 1.0f
    }

    // Compute XP multiplier for selected star (non-premium) or formula result (premium)
    val xpMultiplier = remember(selectedStar, bestNonSuperMult, isPremium) {
        if (isSuper) {
            1.1f * tier.xpMult * bestNonSuperMult
        } else if (isExclusive) {
            2.0f * tier.xpMult * bestNonSuperMult
        } else if (isSecret) {
            5.0f * tier.xpMult * bestNonSuperMult
        } else {
            val starMult = 1.0f + (selectedStar - 1) * 0.05f
            (species.xpMult ?: species.rarity.baseXpMult) * tier.xpMult * starMult
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayColor)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        GlowingCard(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable { /* consume */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = grayColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Pet emoji
                Text(
                    text = species.emoji,
                    fontSize = 56.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pet name
                Text(
                    text = species.name,
                    color = Color(0xFFE8F4FD),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace
                )

                // Rarity + Tier badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(rarityColor.copy(alpha = 0.2f))
                            .border(1.dp, rarityColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = species.rarity.label,
                            color = rarityColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tierColor.copy(alpha = 0.2f))
                            .border(1.dp, tierColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tier.label,
                            color = tierColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Total obtained
                Text(
                    text = "TOTAL OBTAINED: $count",
                    color = grayColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isSuper) {
                    // SUPER: show formula breakdown instead of star selector
                    Text(
                        text = "XP FORMULA",
                        color = grayColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "1.1 \u00D7 ${String.format("%.2f", tier.xpMult)}(${tier.label}) \u00D7 ${String.format("%.2f", bestNonSuperMult)}(best)",
                        color = grayColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "XP MULTIPLIER: ${String.format("%.2f", xpMultiplier)}\u00D7",
                        color = accent,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace
                    )
        } else if (isExclusive) {
            // EXCLUSIVE: show formula breakdown (no stars)
            Text(
                text = "XP FORMULA",
                color = grayColor,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "2.0 \u00D7 ${String.format("%.2f", tier.xpMult)}(${tier.label}) \u00D7 ${String.format("%.2f", bestNonSuperMult)}(best)",
                color = grayColor,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "XP MULTIPLIER: ${String.format("%.2f", xpMultiplier)}\u00D7",
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
        } else if (isSecret) {
            // SECRET: show formula breakdown (no stars)
            Text(
                text = "XP FORMULA",
                color = grayColor,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "5.0 \u00D7 ${String.format("%.2f", tier.xpMult)}(${tier.label}) \u00D7 ${String.format("%.2f", bestNonSuperMult)}(best)",
                color = grayColor,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "XP MULTIPLIER: ${String.format("%.2f", xpMultiplier)}\u00D7",
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
        }
            }
        }
    }
}

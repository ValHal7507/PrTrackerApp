package com.example.prtracker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.navigation.NavHostController
import com.example.prtracker.data.Pet
import com.example.prtracker.data.PetCatalog
import com.example.prtracker.data.PetRarity
import com.example.prtracker.data.PetTier
import com.example.prtracker.data.SpecialDiceType
import com.example.prtracker.data.coinValue
import com.example.prtracker.data.xpMultiplier
import com.example.prtracker.data.ActiveDiceEffect
import com.example.prtracker.navigation.Routes
import com.example.prtracker.ui.components.GridBackground
import com.example.prtracker.ui.theme.CardBackground
import com.example.prtracker.ui.theme.LocalAppearance
import com.example.prtracker.ui.theme.systemAccentColor
import com.example.prtracker.viewmodel.PRViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class DiceRollState { IDLE, ROLLING, REVEAL, PET_DETAIL }

private fun formatCoins(value: Long): String = when {
    value >= 1_000_000_000_000L -> String.format("%.1fT", value / 1_000_000_000_000.0)
    value >= 1_000_000_000L -> String.format("%.1fB", value / 1_000_000_000.0)
    value >= 100_000_000L   -> "${value / 1_000_000}M"
    value >= 10_000_000L    -> String.format("%.1fM", value / 1_000_000.0)
    value >= 1_000_000L     -> "${value / 1_000_000}M"
    value >= 100_000L       -> "${value / 1_000}K"
    value >= 10_000L        -> String.format("%.1fK", value / 1_000.0)
    value >= 1_000L         -> "${value / 1_000}K"
    else                    -> value.toString()
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DiceRollScreen(
    navController: NavHostController,
    viewModel: PRViewModel
) {
    val appearance = LocalAppearance.current
    val accent = appearance.systemAccentColor

    val petInventory by viewModel.petInventory.collectAsState()
    val totalRolls by viewModel.totalRolls.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val autoRoll by viewModel.autoRoll.collectAsState()
    val petUpgrades by viewModel.petUpgrades.collectAsState()
    val equippedPetIds by viewModel.equippedPetIds.collectAsState()
    val activeDiceEffects by viewModel.activeDiceEffects.collectAsState()
    val diceInventory by viewModel.diceInventory.collectAsState()
    val petMult = viewModel.petXpMultiplier()
    val maxSlots = viewModel.maxEquipSlots()

    val rollSpeedLevel = petUpgrades["roll_speed"] ?: 0
    val rollDelay = maxOf(0L, 1600L - rollSpeedLevel * 72L)

    val luckyRollLevel = petUpgrades["lucky_roll"] ?: 0
    val rollsUntilLucky = if (luckyRollLevel > 0) {
        val remainder = totalRolls.toInt() % 5
        if (remainder == 0) 5 else 5 - remainder
    } else 0

    val activeEffect = activeDiceEffects.firstOrNull()
    val activeEffectDiceType = activeEffect?.let { SpecialDiceType.fromId(it.diceTypeId) }
    val activeDiceColor = activeEffectDiceType?.toColor()
    val rollsLeft = activeEffect?.rollsRemaining ?: 0

    var rollState by remember { mutableStateOf(DiceRollState.IDLE) }
    var lastRolledPet by remember { mutableStateOf<Pet?>(null) }
    var lastRollChances by remember { mutableStateOf<Map<PetRarity, Double>>(emptyMap()) }
    var selectedPetId by remember { mutableStateOf<String?>(null) }
    val selectedPetForDetail = selectedPetId?.let { id -> petInventory.find { it.id == id } }

    var rotationX by remember { mutableFloatStateOf(0f) }
    var rotationY by remember { mutableFloatStateOf(0f) }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "diceRotX"
    )
    val animatedRotationY by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "diceRotY"
    )

    val rarityColor = lastRolledPet?.let {
        Color(PetRarity.fromName(it.rarity).colorHex)
    } ?: accent

    val animatableCoins = remember { Animatable(0f) }
    LaunchedEffect(coins) {
        animatableCoins.animateTo(
            targetValue = coins.toFloat(),
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
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
                    text = "PET DICE ROLL",
                    color = accent,
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Monospace
                )
            }

            // ── Stats row (rolls + XP mult on left, coins on right) ──────────
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 2.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side — rolls count + XP multiplier
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$totalRolls ROLLS",
                        color = Color(0xFF6B8CAE),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    if (petMult > 1.0f) {
                        val multText = String.format("%.2fx", petMult)
                        Text(
                            text = "XP $multText",
                            color = Color(0xFF00FF85),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Center — auto-roll toggle
                IconButton(
                    onClick = { viewModel.toggleAutoRoll() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Auto Roll",
                        tint = if (autoRoll) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                }

                // Right side — coin balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(com.example.prtracker.navigation.Routes.PET_UPGRADES) }
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "\uD83E\uDE99",
                        fontSize = 14.sp
                    )
                    val coinText = formatCoins(animatableCoins.value.toLong())
                    val coinFontSize = when {
                        coinText.length > 12 -> 10.sp
                        coinText.length > 9  -> 11.sp
                        else                 -> MaterialTheme.typography.labelMedium.fontSize
                    }
                    Text(
                        text = coinText,
                        color = Color(0xFFFFD700),
                        fontSize = coinFontSize,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ── Lucky roll indicator ─────────────────────────────────────────
            if (luckyRollLevel > 0) {
                val isLuckyReady = rollsUntilLucky == 1
                val luckyText = if (isLuckyReady) "LUCKY ROLL!" else "LUCKY IN ${rollsUntilLucky - 1}"
                Text(
                    text = luckyText,
                    color = if (isLuckyReady) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                )
            }

            // ── Dice + Inventory + Shop buttons ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dice inventory button
                val diceInvCount = diceInventory.size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Routes.DICE_INVENTORY) }
                        .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = "\uD83C\uDFB2", fontSize = 14.sp)
                    Text(
                        text = "DICE ($diceInvCount)",
                        color = accent,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Pet inventory button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Routes.PET_INVENTORY) }
                        .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Inventory",
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "INVENTORY (${petInventory.size})",
                        color = accent,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Dice shop button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Routes.DICE_SHOP) }
                        .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = "\uD83D\uDED2", fontSize = 14.sp)
                    Text(
                        text = "SHOP",
                        color = accent,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ── Equipped pets row ────────────────────────────────────────────
            if (equippedPetIds.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EQUIPPED: ",
                        color = Color(0xFF6B8CAE),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                    equippedPetIds.forEach { petId ->
                        val pet = petInventory.find { it.id == petId }
                        if (pet != null) {
                            val species = PetCatalog.allSpecies.find { it.id == pet.speciesId }
                            Text(
                                text = species?.emoji ?: "?",
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clickable { viewModel.unequipPet(petId) }
                            )
                        }
                    }
                }
            }

            // ── Active dice effects display ───────────────────────────────────
            if (activeDiceEffects.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE: ",
                        color = Color(0xFF6B8CAE),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                    activeDiceEffects.forEach { effect ->
                        val dtype = SpecialDiceType.fromId(effect.diceTypeId)
                        if (dtype != null) {
                            val dc = dtype.toColor()
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(dc.copy(alpha = 0.2f))
                                    .border(1.dp, dc.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${dtype.emoji} ${effect.rollsRemaining}",
                                    color = dc,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Main content area ────────────────────────────────────────────
            val isViewingPet = rollState == DiceRollState.PET_DETAIL

            if (isViewingPet) {
                selectedPetForDetail?.let { pet ->
                    PetDetailView(
                        pet = pet,
                        inventory = petInventory,
                        accent = accent,
                        isEquipped = equippedPetIds.contains(pet.id),
                        canEquip = equippedPetIds.size < maxSlots,
                        onEquip = { viewModel.equipPet(pet.id) },
                        onUnequip = { viewModel.unequipPet(pet.id) },
                        onFuse = {
                            viewModel.fusePet(pet.id)
                            rollState = DiceRollState.IDLE
                            selectedPetId = null
                        },
                        onSell = {
                            viewModel.sellPet(pet.id)
                            rollState = DiceRollState.IDLE
                            selectedPetId = null
                        },
                        onFavorite = {
                            viewModel.toggleFavorite(pet.id)
                        },
                        onDismiss = {
                            rollState = DiceRollState.IDLE
                            selectedPetId = null
                        }
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Reveal area (top, weighted)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (rollState == DiceRollState.REVEAL) {
                            lastRolledPet?.let { pet ->
                                RevealView(
                                    pet = pet,
                                    rarityColor = rarityColor,
                                    accent = accent,
                                    rollChances = lastRollChances,
                                    onDismiss = { rollState = DiceRollState.IDLE }
                                )
                            }
                        }
                    }

                    // Dice area (bottom, fixed height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DiceView(
                            rotationX = animatedRotationX,
                            rotationY = animatedRotationY,
                            isRolling = rollState == DiceRollState.ROLLING,
                            accent = accent,
                            isLuckyReady = rollsUntilLucky == 1 && luckyRollLevel > 0,
                            activeDiceColor = activeDiceColor,
                            isSuperDice = activeEffectDiceType == SpecialDiceType.SUPER_DICE,
                            rollsLeft = if (activeEffect != null) rollsLeft else 0,
                            onRoll = if (rollState != DiceRollState.ROLLING) {
                                {
                                    rollState = DiceRollState.ROLLING
                                    rotationX += 720f
                                    rotationY += 1080f
                                }
                            } else null
                        )
                    }
                }
            }
        } // end main Column

        // ── Roll trigger ─────────────────────────────────────────────────────
        LaunchedEffect(rollState) {
            if (rollState == DiceRollState.ROLLING) {
                delay(rollDelay)
                val result = viewModel.rollDice()
                lastRolledPet = result.pet
                lastRollChances = result.effectiveChances
                rollState = DiceRollState.REVEAL
            }
        }

        // ── Auto-roll loop ───────────────────────────────────────────────────
        LaunchedEffect(autoRoll) {
            while (autoRoll) {
                delay(100)
                when (rollState) {
                    DiceRollState.IDLE -> {
                        rollState = DiceRollState.ROLLING
                        rotationX += 720f
                        rotationY += 1080f
                    }
                    DiceRollState.REVEAL -> {
                        delay(rollDelay)
                        val result = viewModel.rollDice()
                        lastRolledPet = result.pet
                        lastRollChances = result.effectiveChances
                    }
                    DiceRollState.PET_DETAIL -> {
                        viewModel.rollDice()
                        delay(2000)
                    }
                    DiceRollState.ROLLING -> { /* wait for animation */ }
                }
            }
        }

    } // end root Box
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun DiceView(
    rotationX: Float,
    rotationY: Float,
    isRolling: Boolean,
    accent: Color,
    isLuckyReady: Boolean,
    activeDiceColor: Color? = null,
    isSuperDice: Boolean = false,
    rollsLeft: Int = 0,
    onRoll: (() -> Unit)? = null
) {
    val diceColor = activeDiceColor ?: if (isLuckyReady) Color(0xFFC0C0C0) else accent
    val borderColor = activeDiceColor ?: diceColor
    val superColor = Color(0xFF001B3D)

    val infiniteTransition = rememberInfiniteTransition(label = "diceSuper")
    val superPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "superPulse"
    )
    val superShakeX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "superShake"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .then(if (onRoll != null) Modifier.clickable { onRoll() } else Modifier)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .background(diceColor.copy(alpha = if (isRolling) 0.2f else 0.15f), RoundedCornerShape(24.dp))
                .border(2.dp, borderColor, RoundedCornerShape(24.dp))
                .then(
                    if (isSuperDice) {
                        Modifier
                            .drawBehind {
                                drawCircle(
                                    color = superColor.copy(alpha = superPulseAlpha),
                                    radius = size.minDimension * 0.65f
                                )
                                drawCircle(
                                    color = superColor.copy(alpha = superPulseAlpha * 0.5f),
                                    radius = size.minDimension * 0.8f
                                )
                            }
                            .offset { IntOffset(superShakeX.roundToInt(), 0) }
                    } else if (activeDiceColor != null) Modifier.drawBehind {
                        val pulseAlpha = 0.15f + 0.1f * kotlin.math.sin(
                            System.currentTimeMillis() / 300.0
                        ).toFloat()
                        drawCircle(
                            color = activeDiceColor.copy(alpha = pulseAlpha),
                            radius = size.width * 0.55f
                        )
                    } else if (isLuckyReady) Modifier.drawBehind {
                        val shimmerX = (size.width * 0.3f) + (size.width * 0.4f *
                                ((System.currentTimeMillis() % 3000L) / 3000f))
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(shimmerX - 20.dp.toPx(), 0f),
                            end = Offset(shimmerX + 20.dp.toPx(), size.height),
                            strokeWidth = 8.dp.toPx()
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = if (onRoll != null) "Roll Dice" else "Dice",
                tint = diceColor,
                modifier = Modifier
                    .size(80.dp)
                    .then(
                        if (isRolling) Modifier.graphicsLayer {
                            this.rotationX = rotationX
                            this.rotationY = rotationY
                        } else Modifier
                    )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                isRolling -> "ROLLING..."
                rollsLeft > 0 -> "ROLL ($rollsLeft LEFT)"
                else -> "TAP TO ROLL"
            },
            color = diceColor,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun RevealView(
    pet: Pet,
    rarityColor: Color,
    accent: Color,
    rollChances: Map<PetRarity, Double>,
    onDismiss: () -> Unit
) {
    val isSuper = PetRarity.fromName(pet.rarity) == PetRarity.SUPER
    val superColor = Color(PetRarity.SUPER.colorHex)
    val infiniteTransition = rememberInfiniteTransition(label = "revealSuper")

    val revealBoxModifier = if (isSuper) {
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.10f,
            targetValue = 0.30f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "superGlow"
        )
        val shakeX by infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "superShake"
        )
        Modifier
            .size(120.dp)
            .shadow(24.dp, RoundedCornerShape(24.dp))
            .background(superColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(2.dp, superColor, RoundedCornerShape(24.dp))
            .drawBehind {
                drawCircle(
                    color = superColor.copy(alpha = pulseAlpha),
                    radius = size.minDimension * 0.65f
                )
                drawCircle(
                    color = superColor.copy(alpha = pulseAlpha * 0.5f),
                    radius = size.minDimension * 0.8f
                )
            }
            .offset { IntOffset(shakeX.roundToInt(), 0) }
    } else {
        Modifier
            .size(120.dp)
            .shadow(24.dp, RoundedCornerShape(24.dp))
            .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(2.dp, rarityColor, RoundedCornerShape(24.dp))
    }

    val displayColor = if (isSuper) superColor else rarityColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .clickable { onDismiss() }
            .padding(24.dp)
    ) {
        Box(
            modifier = revealBoxModifier,
            contentAlignment = Alignment.Center
        ) {
            val species = PetCatalog.allSpecies.find { it.id == pet.speciesId }
            Text(text = species?.emoji ?: "?", fontSize = if (isSuper) 56.sp else 48.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pet.name,
            color = displayColor,
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(displayColor.copy(alpha = 0.2f))
                    .border(1.dp, displayColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = pet.rarity,
                    color = displayColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
            val tier = PetTier.fromName(pet.tier)
            val tierColor = Color(tier.colorHex)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(tierColor.copy(alpha = 0.2f))
                    .border(1.dp, tierColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tier.label,
                    color = tierColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (!isSuper) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildString {
                    repeat(pet.stars) { append("\u2605") }
                    repeat(5 - pet.stars) { append("\u2606") }
                },
                color = if (pet.stars >= 3) Color(0xFFFFD700) else displayColor,
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (rollChances.isNotEmpty()) {
            val totalWeight = rollChances.values.sum()
            val rarityChance = rollChances[PetRarity.fromName(pet.rarity)] ?: 0.0
            val oneInX = if (rarityChance > 0) {
                (totalWeight / rarityChance).toInt().coerceAtLeast(2)
            } else 2
            Text(
                text = "1 in $oneInX CHANCE",
                color = displayColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isSuper) {
            Text(
                text = "\u2B50 LEGENDARY DISCOVERY \u2B50",
                color = superColor,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TAP TO DISMISS",
            color = Color(0xFF6B8CAE),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

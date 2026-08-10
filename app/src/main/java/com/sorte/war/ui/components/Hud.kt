package com.sorte.war.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.MapData
import com.sorte.war.model.Phase
import com.sorte.war.ui.screens.clickableNoRipple
import com.sorte.war.ui.theme.Crimson
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.PanelNavyLight
import com.sorte.war.ui.theme.SurfaceHigh
import com.sorte.war.ui.theme.TacticalBlue
import com.sorte.war.ui.theme.TacticalGreen
import com.sorte.war.ui.theme.TacticalStroke
import com.sorte.war.ui.theme.TacticalTeal
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary

private fun phaseName(phase: Phase) = when (phase) {
    Phase.REFORCO -> "REFORÇO"
    Phase.ATAQUE -> "ATAQUE"
    Phase.DESLOCAMENTO -> "MOVIMENTO"
    Phase.FIM_DE_JOGO -> "FIM DE JOGO"
}

private fun phaseAccent(phase: Phase) = when (phase) {
    Phase.REFORCO -> TacticalGreen
    Phase.ATAQUE -> Crimson
    Phase.DESLOCAMENTO -> TacticalBlue
    Phase.FIM_DE_JOGO -> Gold
}

@Composable
fun TopBar(
    engine: GameEngine,
    refresh: Int,
    soundEnabled: Boolean,
    compact: Boolean = false,
    onToggleSound: () -> Unit,
    onObjective: () -> Unit,
    onCards: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION") refresh
    val current = engine.currentPlayer
    val accent = phaseAccent(engine.phase)
    val countries = engine.ownedCount(current.id)
    val armies = engine.territoriesOf(current.id).sumOf { engine.armiesOf[it] }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101C26), Color(0xFF09121A), NightNavy)
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.28f))
            .padding(horizontal = 9.dp, vertical = if (compact) 4.dp else 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.46f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.Black.copy(alpha = 0.16f))
                    .border(1.dp, Gold.copy(alpha = 0.34f), RoundedCornerShape(11.dp))
                    .padding(horizontal = 7.dp, vertical = if (compact) 4.dp else 6.dp)
            ) {
                AvatarPortrait(
                    avatar = com.sorte.war.model.Avatar.byId(current.avatarId),
                    sizeDp = if (compact) 28 else 35,
                    ringColor = Color(current.colorArgb)
                )
                Spacer(Modifier.width(7.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        current.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = if (compact) 11.sp else 13.sp,
                        maxLines = 1
                    )
                    Text(
                        "${countries} territ.  •  ⚔ ${armies}",
                        color = TextSecondary,
                        fontSize = if (compact) 7.sp else 8.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(0.54f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accent.copy(alpha = 0.075f))
                    .border(1.2.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(11.dp))
                    .padding(horizontal = 6.dp, vertical = if (compact) 4.dp else 6.dp)
            ) {
                Text(
                    "FASE ATUAL",
                    color = TextSecondary,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    phaseName(engine.phase),
                    color = accent,
                    fontSize = if (compact) 12.sp else 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                if (engine.phase == Phase.REFORCO) {
                    Text(
                        "${engine.reinforcements} reforços disponíveis",
                        color = TextSecondary,
                        fontSize = 7.sp
                    )
                } else {
                    Text(
                        when (engine.phase) {
                            Phase.ATAQUE -> "Selecione origem e alvo"
                            Phase.DESLOCAMENTO -> "Reposicione suas forças"
                            Phase.FIM_DE_JOGO -> "Campanha concluída"
                            else -> ""
                        },
                        color = TextSecondary,
                        fontSize = 7.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(if (compact) 4.dp else 6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                HudAction(
                    icon = if (soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    label = if (soundEnabled) "SOM" else "MUDO",
                    compact = compact,
                    onClick = onToggleSound
                )
                if (current.isHuman) {
                    HudAction(
                        icon = Icons.Filled.Flag,
                        label = "OBJETIVO",
                        compact = compact,
                        onClick = onObjective
                    )
                    HudAction(
                        icon = Icons.Filled.Style,
                        label = "CARTAS ${current.cards.size}",
                        compact = compact,
                        onClick = onCards
                    )
                }
            }
        }

        Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
        PlayersStrip(engine, compact)
    }
}

@Composable
private fun HudAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(SurfaceHigh.copy(alpha = 0.80f))
            .border(1.dp, Gold.copy(alpha = 0.30f), RoundedCornerShape(9.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = 5.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Gold,
            modifier = Modifier.size(if (compact) 13.dp else 15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = if (compact) 7.sp else 8.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun PlayersStrip(engine: GameEngine, compact: Boolean) {
    val total = MapData.territories.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        engine.players.forEach { player ->
            val active = player.id == engine.currentPlayerIndex && !player.eliminated
            val countries = engine.ownedCount(player.id)
            val armies = engine.territoriesOf(player.id).sumOf { engine.armiesOf[it] }
            val playerColor = Color(player.colorArgb)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .alpha(if (player.eliminated) 0.38f else 1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (active) playerColor.copy(alpha = 0.10f)
                        else Color.Black.copy(alpha = 0.15f)
                    )
                    .border(
                        if (active) 1.5.dp else 1.dp,
                        if (active) TacticalTeal else TacticalStroke.copy(alpha = 0.70f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = if (compact) 3.dp else 4.dp)
            ) {
                AvatarPortrait(
                    avatar = com.sorte.war.model.Avatar.byId(player.avatarId),
                    sizeDp = if (compact) 19 else 24,
                    ringColor = playerColor
                )
                Spacer(Modifier.width(5.dp))
                Column {
                    Text(
                        if (player.isHuman) "Você" else player.name,
                        color = if (active) TacticalTeal else TextPrimary,
                        fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
                        fontSize = if (compact) 8.sp else 9.sp,
                        maxLines = 1,
                        textDecoration = if (player.eliminated) TextDecoration.LineThrough else null
                    )
                    Text(
                        "$countries/$total  •  ⚔ $armies",
                        color = TextSecondary,
                        fontSize = if (compact) 7.sp else 8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    engine: GameEngine,
    refresh: Int,
    statusMessage: String?,
    selectedTerritory: Int?,
    compact: Boolean = false,
    onNextPhase: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION") refresh
    val human = engine.currentPlayer.isHuman
    val accent = phaseAccent(engine.phase)
    val actionLabel = when (engine.phase) {
        Phase.REFORCO -> "IR PARA ATAQUE"
        Phase.ATAQUE -> "ENCERRAR ATAQUE"
        Phase.DESLOCAMENTO -> "ENCERRAR TURNO"
        Phase.FIM_DE_JOGO -> ""
    }
    val enabled = human && engine.phase != Phase.FIM_DE_JOGO &&
        !(engine.phase == Phase.REFORCO && !engine.canAdvanceFromReinforce())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF09121A), NightNavy)
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.28f))
            .padding(horizontal = 9.dp, vertical = if (compact) 5.dp else 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.16f))
                .border(1.dp, TacticalStroke, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = if (compact) 5.dp else 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (compact) 28.dp else 36.dp)
                    .background(accent)
            )
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                if (selectedTerritory != null) {
                    val territory = MapData.territory(selectedTerritory)
                    Text(
                        "DE: ${territory.name.uppercase()} (${engine.armiesOf[selectedTerritory]})",
                        color = Gold,
                        fontSize = if (compact) 8.sp else 10.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                } else {
                    Text(
                        "COMANDO DE CAMPO",
                        color = accent,
                        fontSize = if (compact) 8.sp else 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    statusMessage ?: "Selecione um território para agir.",
                    color = TextSecondary,
                    fontSize = if (compact) 8.sp else 10.sp,
                    maxLines = if (compact) 1 else 2
                )
            }
        }

        Spacer(Modifier.height(if (compact) 5.dp else 7.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 38.dp else 49.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (enabled) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF6E4D1B),
                                Gold,
                                Color(0xFF8B6325)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(SurfaceHigh, SurfaceHigh))
                    }
                )
                .border(
                    if (enabled) 1.7.dp else 1.dp,
                    if (enabled) Gold else TacticalStroke,
                    RoundedCornerShape(11.dp)
                )
                .then(if (enabled) Modifier.clickableNoRipple(onNextPhase) else Modifier)
        ) {
            Text(
                actionLabel,
                color = if (enabled) NightNavy else TextSecondary,
                fontWeight = FontWeight.Black,
                fontSize = if (compact) 11.sp else 15.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

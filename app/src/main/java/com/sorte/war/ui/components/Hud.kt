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
import androidx.compose.material3.MaterialTheme
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
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary

private fun phaseName(p: Phase) = when (p) {
    Phase.REFORCO -> "Reforço"
    Phase.ATAQUE -> "Ataque"
    Phase.DESLOCAMENTO -> "Movimento"
    Phase.FIM_DE_JOGO -> "Fim"
}

private fun phaseAccent(p: Phase) = when (p) {
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
    val cur = engine.currentPlayer
    val accent = phaseAccent(engine.phase)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(PanelNavyLight, PanelNavy, NightNavy)
                )
            )
            .border(1.dp, TacticalStroke.copy(alpha = 0.60f))
            .padding(horizontal = 10.dp, vertical = if (compact) 4.dp else 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AvatarPortrait(
                avatar = com.sorte.war.model.Avatar.byId(cur.avatarId),
                sizeDp = if (compact) 26 else 34,
                ringColor = Color(cur.colorArgb)
            )
            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    cur.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = if (compact) 13.sp else 15.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(accent.copy(alpha = 0.14f))
                            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            phaseName(engine.phase).uppercase(),
                            color = accent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                    if (engine.phase == Phase.REFORCO) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${engine.reinforcements} reforços",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HudIconButton(
                if (soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                if (soundEnabled) "Som" else "Mudo",
                compact,
                onToggleSound
            )

            if (cur.isHuman) {
                Spacer(Modifier.width(6.dp))
                HudIconButton(Icons.Filled.Flag, "Objetivo", compact, onObjective)
                Spacer(Modifier.width(6.dp))
                HudIconButton(Icons.Filled.Style, "${cur.cards.size}", compact, onCards)
            }
        }

        Spacer(Modifier.height(if (compact) 4.dp else 7.dp))
        PlayersStrip(engine, compact)
    }
}

@Composable
private fun HudIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceHigh.copy(alpha = 0.92f))
            .border(1.dp, TacticalStroke, RoundedCornerShape(10.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = if (compact) 7.dp else 8.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Gold,
            modifier = Modifier.size(if (compact) 14.dp else 15.dp)
        )
        if (!compact) {
            Spacer(Modifier.width(5.dp))
            Text(
                label,
                color = TextPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun PlayersStrip(engine: GameEngine, compact: Boolean = false) {
    val total = com.sorte.war.model.MapData.territories.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        engine.players.forEach { p ->
            val active = p.id == engine.currentPlayerIndex && !p.eliminated
            val countries = engine.ownedCount(p.id)
            val armies = engine.territoriesOf(p.id).sumOf { engine.armiesOf[it] }
            val playerColor = Color(p.colorArgb)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .alpha(if (p.eliminated) 0.38f else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (active) playerColor.copy(alpha = 0.12f)
                        else Color.Black.copy(alpha = 0.16f)
                    )
                    .border(
                        if (active) 1.5.dp else 1.dp,
                        if (active) playerColor.copy(alpha = 0.95f)
                        else TacticalStroke.copy(alpha = 0.65f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 7.dp,
                        vertical = if (compact) 3.dp else 5.dp
                    )
            ) {
                AvatarPortrait(
                    avatar = com.sorte.war.model.Avatar.byId(p.avatarId),
                    sizeDp = if (compact) 19 else 24,
                    ringColor = playerColor
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        p.name,
                        color = TextPrimary,
                        fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
                        fontSize = 9.sp,
                        maxLines = 1,
                        textDecoration = if (p.eliminated) TextDecoration.LineThrough else null
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$countries/$total",
                            color = playerColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "⚔ $armies",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
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
    compact: Boolean = false,
    onNextPhase: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION") refresh
    val human = engine.currentPlayer.isHuman
    val actionLabel = when (engine.phase) {
        Phase.REFORCO -> "IR PARA ATAQUE"
        Phase.ATAQUE -> "ENCERRAR ATAQUE"
        Phase.DESLOCAMENTO -> "ENCERRAR TURNO"
        Phase.FIM_DE_JOGO -> ""
    }
    val enabled = human && engine.phase != Phase.FIM_DE_JOGO &&
        !(engine.phase == Phase.REFORCO && !engine.canAdvanceFromReinforce())
    val accent = phaseAccent(engine.phase)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(NightNavy, PanelNavy)
                )
            )
            .border(1.dp, TacticalStroke.copy(alpha = 0.60f))
            .padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "COMANDO",
                color = accent,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            )
            Text(
                statusMessage ?: "Selecione um território para agir.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 1 else 2
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (enabled) {
                        Brush.horizontalGradient(listOf(Gold, Color(0xFFD49B3D)))
                    } else {
                        Brush.horizontalGradient(listOf(SurfaceHigh, SurfaceHigh))
                    }
                )
                .border(
                    1.dp,
                    if (enabled) Gold.copy(alpha = 0.90f) else TacticalStroke,
                    RoundedCornerShape(12.dp)
                )
                .then(if (enabled) Modifier.clickableNoRipple(onNextPhase) else Modifier)
                .padding(horizontal = if (compact) 12.dp else 15.dp, vertical = if (compact) 8.dp else 10.dp)
        ) {
            Text(
                actionLabel,
                color = if (enabled) NightNavy else TextSecondary,
                fontWeight = FontWeight.Black,
                fontSize = if (compact) 10.sp else 11.sp,
                letterSpacing = 0.45.sp
            )
        }
    }
}

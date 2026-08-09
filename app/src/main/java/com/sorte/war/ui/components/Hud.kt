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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.Phase
import com.sorte.war.ui.screens.clickableNoRipple
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.TextSecondary

private fun phaseName(p: Phase) = when (p) {
    Phase.REFORCO -> "Reforço"
    Phase.ATAQUE -> "Ataque"
    Phase.DESLOCAMENTO -> "Deslocamento"
    Phase.FIM_DE_JOGO -> "Fim"
}

@Composable
fun TopBar(
    engine: GameEngine,
    refresh: Int,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onObjective: () -> Unit,
    onCards: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION") refresh
    val cur = engine.currentPlayer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelNavy)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AvatarPortrait(
                avatar = com.sorte.war.model.Avatar.byId(cur.avatarId),
                sizeDp = 34,
                ringColor = Color(cur.colorArgb)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(cur.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "Fase: ${phaseName(engine.phase)}" +
                        if (engine.phase == Phase.REFORCO) "  •  Reforços: ${engine.reinforcements}" else "",
                    color = Gold, style = MaterialTheme.typography.labelSmall
                )
            }
            HudIconButton(
                if (soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                if (soundEnabled) "Som" else "Mudo",
                onToggleSound
            )
            if (cur.isHuman) {
                Spacer(Modifier.width(8.dp))
                HudIconButton(Icons.Filled.Flag, "Objetivo", onObjective)
                Spacer(Modifier.width(8.dp))
                HudIconButton(Icons.Filled.Style, "Cartas (${cur.cards.size})", onCards)
            }
        }
        Spacer(Modifier.height(8.dp))
        PlayersStrip(engine)
    }
}

@Composable
private fun HudIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF22344D))
            .clickableNoRipple(onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Gold, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * Placar ao vivo: para cada exército, o comandante, quantos países domina
 * (de 42) e o total de tropas em campo.
 */
@Composable
private fun PlayersStrip(engine: GameEngine) {
    val total = com.sorte.war.model.MapData.territories.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        engine.players.forEach { p ->
            val active = p.id == engine.currentPlayerIndex && !p.eliminated
            val countries = engine.ownedCount(p.id)
            val armies = engine.territoriesOf(p.id).sumOf { engine.armiesOf[it] }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .alpha(if (p.eliminated) 0.45f else 1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (active) Color(0xFF29456B) else Color(0xFF17263A))
                    .border(
                        if (active) 1.5.dp else 1.dp,
                        if (active) Gold else Color(0x22FFFFFF),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                AvatarPortrait(
                    avatar = com.sorte.war.model.Avatar.byId(p.avatarId),
                    sizeDp = 26,
                    ringColor = Color(p.colorArgb)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        p.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        textDecoration = if (p.eliminated) TextDecoration.LineThrough else null
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$countries/$total",
                            color = Color(p.colorArgb),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "⚔ $armies",
                            color = TextSecondary,
                            fontSize = 10.sp
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
    onNextPhase: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION") refresh
    val human = engine.currentPlayer.isHuman
    val actionLabel = when (engine.phase) {
        Phase.REFORCO -> "Avançar p/ Ataque"
        Phase.ATAQUE -> "Terminar Ataque"
        Phase.DESLOCAMENTO -> "Encerrar Turno"
        Phase.FIM_DE_JOGO -> ""
    }
    val enabled = human && engine.phase != Phase.FIM_DE_JOGO &&
        !(engine.phase == Phase.REFORCO && !engine.canAdvanceFromReinforce())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelNavy)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            statusMessage ?: "",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (enabled) Gold else Color(0xFF33465F))
                .then(if (enabled) Modifier.clickableNoRipple(onNextPhase) else Modifier)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                actionLabel,
                color = if (enabled) NightNavy else TextSecondary,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}

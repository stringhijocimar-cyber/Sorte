package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.model.Avatar
import com.sorte.war.model.Rank
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.components.AvatarPortrait
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.TextSecondary

@Composable
fun StatsScreen(vm: GameViewModel) {
    val s = vm.stats
    var confirmReset by remember { mutableStateOf(false) }

    BackHandler { vm.goTo(Screen.HOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D2A47), Color(0xFF060C15))))
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .clickableNoRipple { vm.goTo(Screen.HOME) }
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "ESTATÍSTICAS",
                color = Gold, fontWeight = FontWeight.Black,
                fontSize = 20.sp, letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        // Patente
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF16263C))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarPortrait(Avatar.byId(s.favoriteAvatarId), 58, Gold)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.playerName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${s.rank.insignia}  ${s.rank.title}",
                        color = Gold, fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            val next = s.nextRank
            if (next != null) {
                Text(
                    "${s.victories} de ${next.victoriesNeeded} vitórias para ${next.title}",
                    color = TextSecondary, fontSize = 11.sp
                )
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { s.rankProgress },
                    color = Gold,
                    trackColor = Color(0xFF243B55),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )
            } else {
                Text("Patente máxima: Marechal", color = Gold, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Partidas", "${s.gamesPlayed}", Modifier.weight(1f))
            StatTile("Vitórias", "${s.victories}", Modifier.weight(1f), Gold)
            StatTile("Derrotas", "${s.defeats}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Aproveitamento", "${s.winRate}%", Modifier.weight(1f), Gold)
            StatTile("Melhor partida", "${s.bestTerritories} terr.", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Territórios conquistados", "${s.territoriesConquered}", Modifier.weight(1f))
            StatTile("Trocas de cartas", "${s.cardSetsTraded}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Combates vencidos", "${s.battlesWon}", Modifier.weight(1f))
            StatTile("Combates perdidos", "${s.battlesLost}", Modifier.weight(1f))
            StatTile("Baixas sofridas", "${s.armiesLost}", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // Todas as patentes
        Text("HIERARQUIA", color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF132033))
                .padding(vertical = 6.dp)
        ) {
            Rank.entries.forEach { r ->
                val reached = s.victories >= r.victoriesNeeded
                val current = r == s.rank
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (current) Gold.copy(alpha = 0.12f) else Color.Transparent)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        r.insignia,
                        color = if (reached) Gold else Color(0xFF41526B),
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.width(52.dp)
                    )
                    Text(
                        r.title,
                        color = if (reached) Color.White else TextSecondary,
                        fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (r.victoriesNeeded == 0) "início" else "${r.victoriesNeeded} vit.",
                        color = TextSecondary, fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (confirmReset) Color(0xFF8E2A2A) else Color(0xFF22344D))
                .clickableNoRipple {
                    if (confirmReset) { vm.resetStats(); confirmReset = false }
                    else confirmReset = true
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (confirmReset) "TOQUE DE NOVO PARA CONFIRMAR" else "Zerar estatísticas",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF132033))
            .padding(vertical = 14.dp, horizontal = 6.dp)
    ) {
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(2.dp))
        Text(
            label, color = TextSecondary, fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

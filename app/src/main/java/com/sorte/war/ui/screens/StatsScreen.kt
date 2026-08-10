package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.sorte.war.ui.theme.Crimson
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.PanelNavyLight
import com.sorte.war.ui.theme.SurfaceHigh
import com.sorte.war.ui.theme.TacticalStroke
import com.sorte.war.ui.theme.TacticalTeal
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary

@Composable
fun StatsScreen(vm: GameViewModel) {
    val s = vm.stats
    var confirmReset by remember { mutableStateOf(false) }

    BackHandler { vm.goTo(Screen.HOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF081923), Color(0xFF061018), NightNavy)
                )
            )
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .border(1.dp, Gold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .clickableNoRipple { vm.goTo(Screen.HOME) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Gold)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(
                    "ESTATÍSTICAS",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 21.sp,
                    letterSpacing = 1.9.sp
                )
                Text(
                    "DESEMPENHO DO COMANDANTE",
                    color = TacticalTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF172532), Color(0xFF0C141C))
                    )
                )
                .border(1.2.dp, Gold.copy(alpha = 0.60f), RoundedCornerShape(16.dp))
                .padding(13.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color.Black.copy(alpha = 0.18f))
                    .border(1.dp, TacticalTeal.copy(alpha = 0.50f), RoundedCornerShape(17.dp))
            ) {
                AvatarPortrait(Avatar.byId(s.favoriteAvatarId), 66, Gold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    s.playerName,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${s.rank.insignia}  ${s.rank.title.uppercase()}",
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(
                            "VITÓRIAS",
                            color = TextSecondary,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${s.victories}",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "PROGRESSO DA PATENTE",
                            color = TacticalTeal,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (s.nextRank != null) s.rankProgress else 1f },
                            color = TacticalTeal,
                            trackColor = Color(0xFF060B10),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (s.nextRank != null)
                                "${s.victories} / ${s.nextRank!!.victoriesNeeded}"
                            else "PATENTE MÁXIMA",
                            color = TextSecondary,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        StatGridRow(
            Triple("PARTIDAS", "${s.gamesPlayed}", Color(0xFF77A8C8)),
            Triple("VITÓRIAS", "${s.victories}", TacticalTeal),
            Triple("DERROTAS", "${s.defeats}", Color(0xFF8E9BA8))
        )
        Spacer(Modifier.height(8.dp))
        StatGridRow(
            Triple("APROVEITAMENTO", "${s.winRate}%", TacticalTeal),
            Triple("MELHOR PARTIDA", "${s.bestTerritories} terr.", Gold),
            Triple("CONQUISTADOS", "${s.territoriesConquered}", TacticalTeal)
        )
        Spacer(Modifier.height(8.dp))
        StatGridRow(
            Triple("COMBATES VENCIDOS", "${s.battlesWon}", TacticalTeal),
            Triple("COMBATES PERDIDOS", "${s.battlesLost}", Color(0xFF8E9BA8)),
            Triple("BAIXAS SOFRIDAS", "${s.armiesLost}", Color(0xFF8E9BA8))
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PremiumStatTile(
                "TROCAS DE CARTAS",
                "${s.cardSetsTraded}",
                Gold,
                Modifier.weight(1f)
            )
            PremiumStatTile(
                "TERRITÓRIOS",
                "${s.territoriesConquered}",
                TacticalTeal,
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).height(1.dp).background(Gold.copy(alpha = 0.35f)))
            Spacer(Modifier.width(9.dp))
            Text(
                "HIERARQUIA",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.7.sp
            )
            Spacer(Modifier.width(9.dp))
            Box(Modifier.weight(1f).height(1.dp).background(Gold.copy(alpha = 0.35f)))
        }

        Spacer(Modifier.height(9.dp))

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Rank.entries.reversed().forEach { rank ->
                val reached = s.victories >= rank.victoriesNeeded
                val current = rank == s.rank
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (current) Gold.copy(alpha = 0.10f)
                            else PanelNavy.copy(alpha = 0.86f)
                        )
                        .border(
                            if (current) 1.5.dp else 1.dp,
                            if (current) Gold else TacticalStroke,
                            RoundedCornerShape(11.dp)
                        )
                        .padding(horizontal = 11.dp, vertical = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.18f))
                            .border(
                                1.dp,
                                if (reached) Gold.copy(alpha = 0.55f) else TacticalStroke,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            rank.insignia,
                            color = if (reached) Gold else TextSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            rank.title.uppercase(),
                            color = if (current) Gold else if (reached) TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        if (current) {
                            Text(
                                "SUA PATENTE ATUAL",
                                color = TacticalTeal,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        if (rank.victoriesNeeded == 0) "INÍCIO" else "${rank.victoriesNeeded} VIT.",
                        color = if (current) TacticalTeal else TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(15.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (confirmReset) Crimson.copy(alpha = 0.16f)
                    else SurfaceHigh.copy(alpha = 0.72f)
                )
                .border(
                    1.2.dp,
                    if (confirmReset) Crimson else TacticalStroke,
                    RoundedCornerShape(12.dp)
                )
                .clickableNoRipple {
                    if (confirmReset) {
                        vm.resetStats()
                        confirmReset = false
                    } else {
                        confirmReset = true
                    }
                }
                .padding(horizontal = 13.dp, vertical = 12.dp)
        ) {
            Text(
                if (confirmReset) "!" else "×",
                color = Crimson,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (confirmReset) "TOQUE NOVAMENTE PARA CONFIRMAR" else "ZERAR ESTATÍSTICAS",
                    color = if (confirmReset) Crimson else TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                Text(
                    if (confirmReset) "Essa ação não pode ser desfeita." else "Apaga o histórico local do comandante.",
                    color = TextSecondary,
                    fontSize = 8.sp
                )
            }
        }

        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun StatGridRow(
    first: Triple<String, String, Color>,
    second: Triple<String, String, Color>,
    third: Triple<String, String, Color>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        PremiumStatTile(first.first, first.second, first.third, Modifier.weight(1f))
        PremiumStatTile(second.first, second.second, second.third, Modifier.weight(1f))
        PremiumStatTile(third.first, third.second, third.third, Modifier.weight(1f))
    }
}

@Composable
private fun PremiumStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PanelNavyLight.copy(alpha = 0.92f), PanelNavy)
                )
            )
            .border(1.dp, TacticalStroke, RoundedCornerShape(11.dp))
            .padding(horizontal = 9.dp, vertical = 9.dp)
    ) {
        Box(
            Modifier
                .width(20.dp)
                .height(2.dp)
                .background(accent)
        )
        Spacer(Modifier.height(7.dp))
        Text(
            label,
            color = TextSecondary,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 9.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = if (accent == Gold) Gold else TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}

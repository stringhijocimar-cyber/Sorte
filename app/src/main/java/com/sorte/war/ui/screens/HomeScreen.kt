package com.sorte.war.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.data.ScreenOrientationMode
import com.sorte.war.model.Avatar
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.components.AvatarPortrait
import com.sorte.war.ui.theme.Divider
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.GoldDeep
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.PanelNavyLight
import com.sorte.war.ui.theme.SurfaceHigh
import com.sorte.war.ui.theme.TacticalGreen
import com.sorte.war.ui.theme.TacticalStroke
import com.sorte.war.ui.theme.TacticalTeal
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(vm: GameViewModel) {
    val stats = vm.stats
    val save = vm.saveInfo

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF081923), Color(0xFF061018), NightNavy)
                )
            )
    ) {
        TacticalBackdrop()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Gold.copy(alpha = 0.20f), Color.Transparent)
                        )
                    )
                    .border(1.5.dp, Gold.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "WAR",
                color = Gold,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 13.sp
            )
            Text(
                "DOMÍNIO MUNDIAL",
                color = TextPrimary.copy(alpha = 0.90f),
                fontSize = 11.sp,
                letterSpacing = 5.4.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(NightNavy.copy(alpha = 0.76f))
                    .border(1.dp, TacticalTeal.copy(alpha = 0.62f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    "EDIÇÃO TÁTICA",
                    color = TacticalTeal,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.1.sp
                )
            }

            Spacer(Modifier.height(15.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF172532), Color(0xFF0D151E))
                        )
                    )
                    .border(1.2.dp, Gold.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
                    .padding(15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(Color.Black.copy(alpha = 0.18f))
                            .border(1.dp, TacticalTeal.copy(alpha = 0.50f), RoundedCornerShape(17.dp))
                    ) {
                        AvatarPortrait(
                            avatar = Avatar.byId(stats.favoriteAvatarId),
                            sizeDp = 62,
                            ringColor = Gold
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stats.playerName,
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${stats.rank.insignia}  ${stats.rank.title.uppercase()}",
                            color = Gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.55.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CommanderMiniStat("VITÓRIAS", "${stats.victories}", Modifier.weight(1f))
                            CommanderMiniStat("PARTIDAS", "${stats.gamesPlayed}", Modifier.weight(1f))
                            CommanderMiniStat("VITÓRIA", "${stats.winRate}%", Modifier.weight(1f))
                        }
                    }
                }

                val next = stats.nextRank
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "PROGRESSO DA PATENTE",
                        color = TacticalTeal,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (next != null) "${stats.victories} / ${next.victoriesNeeded}" else "MÁXIMO",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { if (next != null) stats.rankProgress else 1f },
                    color = TacticalTeal,
                    trackColor = Color(0xFF081017),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Spacer(Modifier.height(15.dp))

            if (save != null) {
                PremiumMenuButton(
                    icon = Icons.Filled.PlayArrow,
                    title = "CONTINUAR PARTIDA",
                    subtitle = "Retome sua conquista",
                    accent = TacticalTeal,
                    onClick = { vm.continueGame() }
                )
                Spacer(Modifier.height(9.dp))
            }

            PremiumMenuButton(
                icon = Icons.Filled.Public,
                title = if (save != null) "NOVA CAMPANHA" else "INICIAR CAMPANHA",
                subtitle = "Comece uma nova jornada",
                accent = Gold,
                onClick = { vm.goTo(Screen.NEW_GAME) }
            )
            Spacer(Modifier.height(9.dp))

            PremiumMenuButton(
                icon = Icons.Filled.BarChart,
                title = "ESTATÍSTICAS",
                subtitle = "Veja seu desempenho global",
                accent = Color(0xFF8AA0B3),
                onClick = { vm.goTo(Screen.STATS) }
            )
            Spacer(Modifier.height(9.dp))

            PremiumMenuButton(
                icon = Icons.Filled.Style,
                title = "ARSENAL",
                subtitle = "As 21 cartas ilustradas",
                accent = TacticalTeal,
                onClick = { vm.goTo(Screen.ARSENAL) }
            )
            Spacer(Modifier.height(9.dp))

            PremiumMenuButton(
                icon = Icons.Filled.HelpOutline,
                title = "COMO JOGAR",
                subtitle = "Aprenda as regras",
                accent = GoldDeep,
                compact = true,
                onClick = { vm.goTo(Screen.HOW_TO_PLAY) }
            )

            Spacer(Modifier.height(15.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PanelNavy.copy(alpha = 0.90f))
                    .border(1.dp, Divider, RoundedCornerShape(16.dp))
                    .padding(13.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ScreenRotation,
                        contentDescription = null,
                        tint = TacticalTeal,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "ORIENTAÇÃO DA TELA",
                        color = TacticalTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(Modifier.height(9.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScreenOrientationMode.entries.forEach { mode ->
                        val selected = vm.orientationMode == mode
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    if (selected) TacticalTeal.copy(alpha = 0.14f)
                                    else SurfaceHigh.copy(alpha = 0.74f)
                                )
                                .border(
                                    if (selected) 1.5.dp else 1.dp,
                                    if (selected) TacticalTeal else TacticalStroke,
                                    RoundedCornerShape(11.dp)
                                )
                                .clickableNoRipple { vm.setOrientation(mode) }
                                .padding(horizontal = 6.dp, vertical = 9.dp)
                        ) {
                            Text(
                                mode.label.uppercase(),
                                color = if (selected) TacticalTeal else TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (selected) "SELECIONADO" else "MODO DE TELA",
                                color = TextSecondary,
                                fontSize = 7.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(17.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Gold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Text(
                    "CONQUISTE • FORTIFIQUE • DOMINE",
                    color = Gold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Planeje sua estratégia, conquiste territórios e cumpra seu objetivo secreto.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CommanderMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.20f))
            .border(1.dp, TacticalStroke.copy(alpha = 0.62f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 3.dp)
    ) {
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(
            label,
            color = TextSecondary,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun PremiumMenuButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = if (accent == Gold) 0.20f else 0.14f),
                        PanelNavyLight,
                        Color(0xFF0A1118)
                    )
                )
            )
            .border(1.2.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
            .clickableNoRipple(onClick)
            .padding(
                horizontal = 13.dp,
                vertical = if (compact) 10.dp else 12.dp
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (compact) 36.dp else 42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.22f))
                .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(10.dp))
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (accent == Gold) Gold else TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = if (compact) 12.sp else 14.sp,
                letterSpacing = 0.7.sp
            )
            Text(
                subtitle.uppercase(),
                color = TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.45.sp
            )
        }
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun TacticalBackdrop() {
    val phase by rememberInfiniteTransition(label = "tactical-bg-v2").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val step = 46f
        val grid = TacticalTeal.copy(alpha = 0.030f)

        var x = -step + phase * step
        while (x < w + step) {
            drawLine(grid, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < h) {
            drawLine(grid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            y += step
        }

        val center = Offset(w * 0.5f, h * 0.18f)
        for (i in 1..4) {
            drawCircle(
                Gold.copy(alpha = 0.025f),
                radius = w * 0.11f * i,
                center = center,
                style = Stroke(
                    width = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 10f))
                )
            )
        }

        for (i in 0 until 38) {
            val seed = i * 29.17f
            val px = ((seed * 0.61f) % 1f) * w
            val py = ((seed * 0.31f + phase) % 1f) * h
            drawCircle(
                Gold.copy(alpha = 0.04f + (i % 3) * 0.02f),
                radius = 0.8f + (i % 2),
                center = Offset(px, py)
            )
        }

        for (k in 0 until 2) {
            val cx = w * (0.28f + 0.43f * k)
            val cy = h * (0.48f + 0.16f * k)
            val radius = w * (0.21f + 0.07f * k)
            var a = 0f
            while (a < 6.28f) {
                val px = cx + cos(a + phase * 6.28f) * radius
                val py = cy + sin(a + phase * 6.28f) * radius * 0.28f
                drawCircle(TacticalTeal.copy(alpha = 0.028f), 1f, Offset(px, py))
                a += 0.19f
            }
        }
    }
}

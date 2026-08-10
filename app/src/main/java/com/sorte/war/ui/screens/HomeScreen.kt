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
                    listOf(
                        Color(0xFF0A1D27),
                        Color(0xFF07111A),
                        NightNavy
                    )
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.10f))
                    .border(1.dp, Gold.copy(alpha = 0.42f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "WAR",
                color = Gold,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 14.sp
            )
            Text(
                "DOMÍNIO MUNDIAL",
                color = TextPrimary.copy(alpha = 0.78f),
                fontSize = 11.sp,
                letterSpacing = 5.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(TacticalTeal.copy(alpha = 0.09f))
                    .border(1.dp, TacticalTeal.copy(alpha = 0.28f), RoundedCornerShape(30.dp))
                    .padding(horizontal = 11.dp, vertical = 5.dp)
            ) {
                Text(
                    "EDIÇÃO TÁTICA",
                    color = TacticalTeal,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PanelNavyLight, PanelNavy)
                        )
                    )
                    .border(1.dp, TacticalStroke.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarPortrait(
                        avatar = Avatar.byId(stats.favoriteAvatarId),
                        sizeDp = 62,
                        ringColor = Gold
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stats.playerName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${stats.rank.insignia}  ${stats.rank.title}",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.35.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${stats.victories}",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "VITÓRIAS",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickStat(
                        label = "PARTIDAS",
                        value = "${stats.gamesPlayed}",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStat(
                        label = "APROVEIT.",
                        value = "${stats.winRate}%",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStat(
                        label = "CONQUISTAS",
                        value = "${stats.territoriesConquered}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                val next = stats.nextRank
                if (next != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "PROGRESSÃO",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${(next.victoriesNeeded - stats.victories).coerceAtLeast(0)} vitórias para ${next.title}",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { stats.rankProgress },
                        color = Gold,
                        trackColor = SurfaceHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                } else {
                    Text(
                        "PATENTE MÁXIMA ALCANÇADA",
                        color = Gold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (save != null) {
                HomeButton(
                    icon = Icons.Filled.PlayArrow,
                    title = "CONTINUAR PARTIDA",
                    subtitle = "${save.territories} territórios • ${save.playersAlive} exércitos em jogo",
                    container = TacticalGreen,
                    content = NightNavy,
                    emphasized = true,
                    onClick = { vm.continueGame() }
                )
                Spacer(Modifier.height(10.dp))
            }

            HomeButton(
                icon = Icons.Filled.Public,
                title = if (save != null) "NOVA CAMPANHA" else "INICIAR CAMPANHA",
                subtitle = "Escolha comandante, cor e dificuldade",
                container = Gold,
                content = NightNavy,
                emphasized = true,
                onClick = { vm.goTo(Screen.NEW_GAME) }
            )
            Spacer(Modifier.height(10.dp))

            HomeButton(
                icon = Icons.Filled.BarChart,
                title = "ESTATÍSTICAS",
                subtitle = "${stats.gamesPlayed} partidas • ${stats.winRate}% de vitórias",
                container = PanelNavyLight,
                content = TextPrimary,
                onClick = { vm.goTo(Screen.STATS) }
            )
            Spacer(Modifier.height(10.dp))

            HomeButton(
                icon = Icons.Filled.HelpOutline,
                title = "COMO JOGAR",
                subtitle = "Regras do War em 1 minuto",
                container = PanelNavy,
                content = TextPrimary,
                onClick = { vm.goTo(Screen.HOW_TO_PLAY) }
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PanelNavy.copy(alpha = 0.88f))
                    .border(1.dp, Divider, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ScreenRotation,
                        contentDescription = null,
                        tint = TacticalTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ORIENTAÇÃO DA TELA",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScreenOrientationMode.entries.forEach { mode ->
                        val selected = vm.orientationMode == mode
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (selected) Gold else SurfaceHigh)
                                .border(
                                    1.dp,
                                    if (selected) Gold else TacticalStroke,
                                    RoundedCornerShape(11.dp)
                                )
                                .clickableNoRipple { vm.setOrientation(mode) }
                                .padding(vertical = 9.dp)
                        ) {
                            Text(
                                mode.label,
                                color = if (selected) NightNavy else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    vm.orientationMode.description,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "CONQUISTE • FORTIFIQUE • DOMINE",
                color = Gold.copy(alpha = 0.80f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Cumpra o objetivo secreto antes dos seus adversários.",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.16f))
            .border(1.dp, TacticalStroke.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(vertical = 9.dp, horizontal = 5.dp)
    ) {
        Text(value, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
        Spacer(Modifier.height(1.dp))
        Text(
            label,
            color = TextSecondary,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun HomeButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    container: Color,
    content: Color,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .border(
                1.dp,
                if (emphasized) container.copy(alpha = 0.95f) else TacticalStroke,
                RoundedCornerShape(16.dp)
            )
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (emphasized) content.copy(alpha = 0.10f)
                    else Color.Black.copy(alpha = 0.16f)
                )
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = content,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = content.copy(alpha = 0.70f),
                fontSize = 10.sp
            )
        }
    }
}

/** Fundo tático sutil: grade cartográfica, pontos e rotas lentas. */
@Composable
private fun TacticalBackdrop() {
    val phase by rememberInfiniteTransition(label = "tactical-bg").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val grid = TacticalTeal.copy(alpha = 0.035f)
        val step = 48f

        var x = -step + (phase * step)
        while (x < w + step) {
            drawLine(grid, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            x += step
        }

        var y = 0f
        while (y < h) {
            drawLine(grid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            y += step
        }

        val center = Offset(w * 0.5f, h * 0.22f)
        val ringInk = Gold.copy(alpha = 0.035f)
        for (i in 1..3) {
            drawCircle(
                ringInk,
                radius = w * (0.17f * i),
                center = center,
                style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 9f)))
            )
        }

        for (i in 0 until 34) {
            val seed = i * 31.37f
            val px = ((seed * 0.63f) % 1f) * w
            val py = ((seed * 0.29f + phase) % 1f) * h
            drawCircle(
                TacticalTeal.copy(alpha = 0.08f + (i % 3) * 0.035f),
                radius = 1f + (i % 2),
                center = Offset(px, py)
            )
        }

        for (k in 0 until 2) {
            val cx = w * (0.28f + 0.38f * k)
            val cy = h * (0.44f + 0.18f * k)
            val radius = w * (0.24f + 0.08f * k)
            var a = 0f
            while (a < 6.28f) {
                val px = cx + cos(a + phase * 6.28f) * radius
                val py = cy + sin(a + phase * 6.28f) * radius * 0.30f
                drawCircle(Gold.copy(alpha = 0.035f), 1f, Offset(px, py))
                a += 0.20f
            }
        }
    }
}

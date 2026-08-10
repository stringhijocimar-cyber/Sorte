package com.sorte.war.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
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
            .background(Brush.verticalGradient(listOf(Color(0xFF0D2A47), Color(0xFF060C15))))
    ) {
        StarfieldBackdrop()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // ---------------- Marca ----------------
            Icon(
                Icons.Filled.Public, contentDescription = null,
                tint = Gold, modifier = Modifier.size(54.dp)
            )
            Text(
                "WAR",
                color = Gold, fontSize = 62.sp,
                fontWeight = FontWeight.Black, letterSpacing = 16.sp
            )
            Text(
                "DOMÍNIO MUNDIAL",
                color = TextSecondary, fontSize = 12.sp,
                letterSpacing = 7.sp, fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(22.dp))

            // ---------------- Cartão de perfil ----------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF16263C), Color(0xFF101B2B))
                        )
                    )
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
                            color = Color.White, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stats.rank.insignia,
                                color = Gold, fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stats.rank.title,
                                color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${stats.victories}",
                            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black
                        )
                        Text("vitórias", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                val next = stats.nextRank
                if (next != null) {
                    Text(
                        "Próxima patente: ${next.title} " +
                            "(${(next.victoriesNeeded - stats.victories).coerceAtLeast(0)} vitórias)",
                        color = TextSecondary, fontSize = 11.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = { stats.rankProgress },
                        color = Gold,
                        trackColor = Color(0xFF243B55),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                } else {
                    Text(
                        "Patente máxima alcançada, Marechal!",
                        color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ---------------- Ações ----------------
            if (save != null) {
                HomeButton(
                    icon = Icons.Filled.PlayArrow,
                    title = "CONTINUAR PARTIDA",
                    subtitle = "${save.territories} territórios • ${save.playersAlive} exércitos em jogo",
                    container = Color(0xFF2E7D32),
                    content = Color.White,
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
                onClick = { vm.goTo(Screen.NEW_GAME) }
            )
            Spacer(Modifier.height(10.dp))

            HomeButton(
                icon = Icons.Filled.BarChart,
                title = "ESTATÍSTICAS",
                subtitle = "${stats.gamesPlayed} partidas • ${stats.winRate}% de vitórias",
                container = Color(0xFF24405F),
                content = Color.White,
                onClick = { vm.goTo(Screen.STATS) }
            )
            Spacer(Modifier.height(10.dp))

            HomeButton(
                icon = Icons.Filled.HelpOutline,
                title = "COMO JOGAR",
                subtitle = "Regras do War em 1 minuto",
                container = Color(0xFF1E3A5F),
                content = Color.White,
                onClick = { vm.goTo(Screen.HOW_TO_PLAY) }
            )

            Spacer(Modifier.height(16.dp))

            // ---------------- Orientação da tela ----------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF132033))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ScreenRotation, contentDescription = null,
                        tint = TextSecondary, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ORIENTAÇÃO DA TELA",
                        color = TextSecondary, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScreenOrientationMode.entries.forEach { m ->
                        val sel = vm.orientationMode == m
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (sel) Gold else Color(0xFF22344D))
                                .clickableNoRipple { vm.setOrientation(m) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                m.label,
                                color = if (sel) NightNavy else Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    vm.orientationMode.description,
                    color = TextSecondary, fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Conquiste territórios, cumpra seu objetivo secreto\ne domine o mundo.",
                color = TextSecondary, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HomeButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    container: Color,
    content: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickableNoRipple(onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = content, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text(subtitle, color = content.copy(alpha = 0.78f), fontSize = 11.sp)
        }
    }
}

/** Fundo com pontos e arcos lentos, lembrando rotas num mapa-múndi. */
@Composable
private fun StarfieldBackdrop() {
    val t by rememberInfiniteTransition(label = "bg").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (i in 0 until 46) {
            val seed = i * 37.13f
            val px = ((seed * 0.61f) % 1f) * w
            val drift = ((seed * 0.27f + t) % 1f)
            val py = drift * h
            val r = 1f + (i % 3)
            drawCircle(
                Color(0xFF9FD0FF).copy(alpha = 0.10f + 0.10f * (i % 3)),
                radius = r,
                center = Offset(px, py)
            )
        }
        // arcos suaves
        for (k in 0 until 3) {
            val cx = w * (0.2f + 0.3f * k)
            val cy = h * (0.25f + 0.22f * k)
            val rad = w * (0.30f + 0.12f * k)
            var a = 0f
            while (a < 6.28f) {
                val x = cx + cos(a + t * 6.28f) * rad
                val y = cy + sin(a + t * 6.28f) * rad * 0.35f
                drawCircle(Color(0xFF7FB6E8).copy(alpha = 0.05f), 1.2f, Offset(x, y))
                a += 0.16f
            }
        }
    }
}

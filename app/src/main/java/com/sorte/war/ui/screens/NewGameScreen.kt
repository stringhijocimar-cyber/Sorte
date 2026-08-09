package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.sorte.war.model.Difficulty
import com.sorte.war.model.PlayerPalette
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.components.AvatarPortrait
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.TextSecondary

@Composable
fun NewGameScreen(vm: GameViewModel) {
    var name by remember { mutableStateOf(vm.stats.playerName) }
    var players by remember { mutableIntStateOf(4) }
    var colorIndex by remember { mutableIntStateOf(0) }
    var avatarIndex by remember { mutableIntStateOf(vm.stats.favoriteAvatarId) }
    var difficulty by remember { mutableStateOf(Difficulty.VETERANO) }

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
                "NOVA CAMPANHA",
                color = Gold, fontWeight = FontWeight.Black,
                fontSize = 20.sp, letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        SetupCard("Seu nome") {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 16) name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(14.dp))

        SetupCard("Seu comandante") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                Avatar.all.forEachIndexed { i, av ->
                    val sel = avatarIndex == i
                    AvatarPortrait(
                        avatar = av,
                        sizeDp = if (sel) 64 else 54,
                        ringColor = if (sel) Gold else Color(0x33FFFFFF),
                        modifier = Modifier.clickableNoRipple { avatarIndex = i }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val chosen = Avatar.byId(avatarIndex)
            Text(
                "${chosen.commander} — ${chosen.epithet}",
                color = Color.White, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(chosen.era, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(14.dp))

        SetupCard("Cor do seu exército") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PlayerPalette.ordered.forEachIndexed { i, army ->
                    val sel = colorIndex == i
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(army.argb))
                            .border(
                                width = if (sel) 3.dp else 1.dp,
                                color = if (sel) Gold else Color(0x55FFFFFF),
                                shape = CircleShape
                            )
                            .clickableNoRipple { colorIndex = i },
                        contentAlignment = Alignment.Center
                    ) {
                        if (sel) {
                            Icon(
                                Icons.Filled.Check, contentDescription = army.name,
                                tint = if (army.argb == 0xFFFDD835 || army.argb == 0xFFECEFF1)
                                    NightNavy else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        SetupCard("Número de exércitos") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (2..6).forEach { count ->
                    val sel = players == count
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (sel) Gold else Color(0xFF22344D))
                            .clickableNoRipple { players = count },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            count.toString(),
                            color = if (sel) NightNavy else Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 17.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Você + ${players - 1} oponente(s) da CPU.",
                color = TextSecondary, style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(14.dp))

        SetupCard("Dificuldade") {
            Difficulty.entries.forEach { d ->
                val sel = difficulty == d
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sel) Gold.copy(alpha = 0.18f) else Color(0xFF1B2C44))
                        .border(
                            if (sel) 2.dp else 1.dp,
                            if (sel) Gold else Color(0xFF2A3B54),
                            RoundedCornerShape(12.dp)
                        )
                        .clickableNoRipple { difficulty = d }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            d.label,
                            color = if (sel) Gold else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            d.description,
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (sel) Icon(Icons.Filled.Check, contentDescription = null, tint = Gold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (vm.saveInfo != null) {
            Text(
                "Atenção: iniciar uma nova campanha descarta a partida salva.",
                color = Color(0xFFFFB74D),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Gold)
                .clickableNoRipple {
                    vm.startGame(name, players, colorIndex, avatarIndex, difficulty)
                }
                .padding(horizontal = 18.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = NightNavy)
            Spacer(Modifier.width(10.dp))
            Text(
                "COMEÇAR A GUERRA",
                color = NightNavy, fontWeight = FontWeight.Black, letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SetupCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF132033))
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

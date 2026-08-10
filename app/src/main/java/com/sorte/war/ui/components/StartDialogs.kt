package com.sorte.war.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.model.Avatar
import com.sorte.war.model.Objective
import com.sorte.war.model.Player
import com.sorte.war.ui.Sfx
import com.sorte.war.ui.SoundManager
import com.sorte.war.ui.screens.clickableNoRipple
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/** Sorteio inicial: cada exército rola um dado e o maior começa. */
@Composable
fun StartRollDialog(
    players: List<Player>,
    rolls: IntArray,
    startingPlayer: Int,
    sound: SoundManager?,
    onClose: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sound?.play(Sfx.DICE)
        delay(1000)
        revealed = true
        sound?.play(Sfx.CONQUER, 0.7f)
    }

    AlertDialog(
        onDismissRequest = { if (revealed) onClose() },
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = onClose,
                enabled = revealed,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Começar", fontWeight = FontWeight.Bold) }
        },
        title = {
            Text(
                if (revealed) "Quem começa: ${players.getOrNull(startingPlayer)?.name ?: ""}"
                else "Sorteando quem começa...",
                color = Gold, fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Cada exército rola um dado. O maior abre a partida.",
                    color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                players.forEachIndexed { i, p ->
                    val actual = rolls.getOrElse(i) { 1 }
                    val winner = revealed && i == startingPlayer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(
                                if (winner) Gold.copy(alpha = 0.16f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        AvatarPortrait(
                            avatar = Avatar.byId(p.avatarId),
                            sizeDp = 30,
                            ringColor = Color(p.colorArgb)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                p.name.uppercase(),
                                color = if (winner) Gold else Color.White,
                                fontWeight = if (winner) FontWeight.Black else FontWeight.Normal
                            )
                            if (revealed) {
                                Text(
                                    if (winner) "INICIA A CAMPANHA" else "dado $actual",
                                    color = if (winner) Gold else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (winner) FontWeight.Black else FontWeight.Normal,
                                    letterSpacing = if (winner) 1.2.sp else 0.sp
                                )
                            }
                        }
                        DieFace(
                            value = actual,
                            win = if (winner) true else null,
                            size = 32,
                            rolling = !revealed,
                            index = i
                        )
                    }
                }
            }
        }
    )
}

/** Cartas de objetivo viradas para baixo: o jogador escolhe a sua. */
@Composable
fun ObjectiveChoiceDialog(
    options: List<Objective>,
    sound: SoundManager?,
    onChoose: (Int) -> Unit
) {
    var flipped by remember { mutableStateOf(-1) }

    AlertDialog(
        onDismissRequest = {},
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = { if (flipped >= 0) onChoose(flipped) },
                enabled = flipped >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Aceitar missão", fontWeight = FontWeight.Bold) }
        },
        title = {
            Text(
                if (flipped < 0) "Escolha sua missão" else "Sua missão secreta",
                color = Gold, fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    if (flipped < 0)
                        "Três cartas de objetivo, viradas para baixo. Toque em uma para revelar."
                    else "Confirme para manter esta missão, ou toque em outra carta.",
                    color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEachIndexed { i, _ ->
                        ObjectiveCardBack(
                            index = i,
                            selected = flipped == i,
                            modifier = Modifier
                                .weight(1f)
                                .clickableNoRipple {
                                    flipped = i
                                    sound?.play(Sfx.CLICK)
                                }
                        )
                    }
                }
                if (flipped >= 0) {
                    Spacer(Modifier.height(14.dp))
                    ObjectiveArt(options[flipped])
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3A1414), RoundedCornerShape(12.dp))
                            .border(1.5.dp, Color(0xFF7E2C2C), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            options[flipped].description,
                            color = Color(0xFFFFE2E2),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ObjectiveCardBack(index: Int, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(96.dp)
            .background(
                if (selected) Color(0xFF5A1A1A) else Color(0xFF2A1010),
                RoundedCornerShape(12.dp)
            )
            .border(
                if (selected) 2.5.dp else 1.dp,
                if (selected) Gold else Color(0xFF6B2B2B),
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Text("★", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "MISSÃO\n${index + 1}",
            color = Color(0xFFE8C7C7),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

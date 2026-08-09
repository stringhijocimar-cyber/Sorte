package com.sorte.war.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.model.PlayerPalette
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.OceanDeep
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.TextSecondary

@Composable
fun MenuScreen(vm: GameViewModel) {
    var name by remember { mutableStateOf("Você") }
    var players by remember { mutableIntStateOf(4) }
    var colorIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(OceanDeep, NightNavy))
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Public,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "WAR",
                color = Gold,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 12.sp
            )
            Text(
                "DOMÍNIO MUNDIAL",
                color = TextSecondary,
                fontSize = 14.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(36.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = PanelNavy),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Seu nome",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 16) name = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Número de exércitos (jogadores)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (2..6).forEach { count ->
                            val selected = players == count
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Gold else Color(0xFF22344D))
                                    .clickableNoRipple { players = count },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    count.toString(),
                                    color = if (selected) NightNavy else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Você + ${players - 1} oponente(s) controlado(s) pela CPU.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Cor do seu exército",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PlayerPalette.ordered.forEachIndexed { i, army ->
                            val selected = colorIndex == i
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(army.argb))
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) Gold else Color(0x55FFFFFF),
                                        shape = CircleShape
                                    )
                                    .clickableNoRipple { colorIndex = i },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = army.name,
                                        tint = if (army.argb == 0xFFFDD835 || army.argb == 0xFFECEFF1)
                                            NightNavy else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Cor escolhida: ${PlayerPalette.ordered[colorIndex].name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Partida em andamento salva no aparelho
            vm.saveInfo?.let { info ->
                Button(
                    onClick = { vm.continueGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32), contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("CONTINUAR PARTIDA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(
                            "${info.territories} territórios • ${info.playersAlive} exércitos em jogo",
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Iniciar uma nova campanha descarta a partida salva.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            }

            Button(
                onClick = { vm.startGame(name, players, colorIndex) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (vm.saveInfo != null) "NOVA CAMPANHA" else "INICIAR CAMPANHA",
                    fontWeight = FontWeight.Black, letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Conquiste territórios, cumpra seu objetivo secreto e domine o mundo.",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

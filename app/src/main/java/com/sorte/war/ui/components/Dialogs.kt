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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.BattleResult
import com.sorte.war.model.Card as GameCard
import com.sorte.war.ui.screens.clickableNoRipple
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.TextSecondary
import kotlin.math.roundToInt

/** Uma face de dado desenhada com pips. */
@Composable
fun DieFace(value: Int, win: Boolean?, size: Int = 44) {
    val bg = when (win) {
        true -> Color(0xFF2E7D32)
        false -> Color(0xFF8E2A2A)
        null -> Color(0xFFF5F0E6)
    }
    val pip = if (win == null) Color(0xFF20242C) else Color.White
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.5.dp, Color(0x33000000), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size((size - 12).dp)) {
            val w = this.size.width
            val r = w * 0.09f
            val a = w * 0.22f
            val c = w * 0.5f
            val b = w * 0.78f
            fun dot(x: Float, y: Float) = drawCircle(pip, r, Offset(x, y))
            when (value) {
                1 -> dot(c, c)
                2 -> { dot(a, a); dot(b, b) }
                3 -> { dot(a, a); dot(c, c); dot(b, b) }
                4 -> { dot(a, a); dot(b, a); dot(a, b); dot(b, b) }
                5 -> { dot(a, a); dot(b, a); dot(c, c); dot(a, b); dot(b, b) }
                6 -> { dot(a, a); dot(b, a); dot(a, c); dot(b, c); dot(a, b); dot(b, b) }
            }
        }
    }
}

@Composable
fun BattleDialog(
    result: BattleResult,
    attackerName: String,
    defenderName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Continuar", fontWeight = FontWeight.Bold) }
        },
        title = {
            Text(
                if (result.conquered) "Território conquistado!" else "Resultado do combate",
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("$attackerName (ataque)", color = Color(0xFFFF8A80), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.attackerDice.forEachIndexed { i, v ->
                        val win = winFor(result.attackerDice, result.defenderDice, i, attacker = true)
                        DieFace(v, win)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("$defenderName (defesa)", color = Color(0xFF82B1FF), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.defenderDice.forEachIndexed { i, v ->
                        val win = winFor(result.attackerDice, result.defenderDice, i, attacker = false)
                        DieFace(v, win)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Baixas — atacante: ${result.attackerLosses}   |   defensor: ${result.defenderLosses}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (result.conquered) {
                    Spacer(Modifier.height(8.dp))
                    Text("Vitória! O território agora é seu.", color = Color(0xFF7CF5A0), fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

private fun winFor(att: List<Int>, def: List<Int>, index: Int, attacker: Boolean): Boolean? {
    val comparisons = minOf(att.size, def.size)
    if (index >= comparisons) return null
    val attWins = att[index] > def[index] // empate favorece defensor
    return if (attacker) attWins else !attWins
}

@Composable
fun AdvanceDialog(option: GameEngine.AdvanceOption, onConfirm: (Int) -> Unit, onCancel: () -> Unit) {
    val maxExtra = option.max - option.min
    var extra by remember { mutableFloatStateOf(0f) }
    AlertDialog(
        onDismissRequest = { onCancel() },
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = { onConfirm(extra.roundToInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Avançar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Manter", color = TextSecondary) } },
        title = { Text("Avançar tropas", color = Gold, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Você já moveu ${option.min}. Quantas tropas adicionais deseja avançar para o território conquistado?",
                    color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Adicionais: +${extra.roundToInt()}  (total ${option.min + extra.roundToInt()})",
                    color = Color.White, fontWeight = FontWeight.Bold
                )
                Slider(value = extra, onValueChange = { extra = it }, valueRange = 0f..maxExtra.toFloat())
            }
        }
    )
}

@Composable
fun FortifyDialog(fromName: String, toName: String, maxMovable: Int, onConfirm: (Int) -> Unit, onCancel: () -> Unit) {
    var count by remember { mutableFloatStateOf(1f) }
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = { onConfirm(count.roundToInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Deslocar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar", color = TextSecondary) } },
        title = { Text("Deslocar exércitos", color = Gold, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("$fromName → $toName", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Mover: ${count.roundToInt()} exército(s)", color = TextSecondary)
                Slider(
                    value = count,
                    onValueChange = { count = it },
                    valueRange = 1f..maxMovable.toFloat().coerceAtLeast(1f)
                )
            }
        }
    )
}

@Composable
fun ObjectiveDialog(description: String, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Entendi", fontWeight = FontWeight.Bold) }
        },
        title = { Text("Seu objetivo secreto", color = Gold, fontWeight = FontWeight.Bold) },
        text = { Text(description, color = Color.White, style = MaterialTheme.typography.bodyLarge) }
    )
}

@Composable
fun CardsDialog(
    cards: List<GameCard>,
    canTradeNow: Boolean,
    nextBonus: Int,
    isValidSet: (List<GameCard>) -> Boolean,
    onTrade: (List<GameCard>) -> Unit,
    onClose: () -> Unit
) {
    val selected = remember { mutableStateListOf<Int>() }
    val chosen = selected.map { cards[it] }
    val valid = chosen.size == 3 && isValidSet(chosen)

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = { if (valid) { onTrade(chosen); selected.clear() } },
                enabled = valid && canTradeNow,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Trocar (+$nextBonus)", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Fechar", color = TextSecondary) } },
        title = { Text("Suas cartas (${cards.size})", color = Gold, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (cards.isEmpty()) {
                    Text("Você ainda não possui cartas. Conquiste um território para receber uma.", color = TextSecondary)
                } else {
                    Text(
                        "Selecione 3 cartas: três iguais, uma de cada símbolo, ou usando coringa.",
                        color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    cards.forEachIndexed { i, card ->
                        val isSel = i in selected
                        val terrName = if (card.territoryId >= 0)
                            com.sorte.war.model.MapData.territory(card.territoryId).name else "—"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (isSel) Gold.copy(alpha = 0.20f) else Color(0xFF1B2C44),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) Gold else Color(0xFF2A3B54),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickableNoRipple {
                                    if (isSel) selected.remove(i)
                                    else if (selected.size < 3) selected.add(i)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(card.symbol.label, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(96.dp))
                            Text(terrName, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (!canTradeNow) {
                        Spacer(Modifier.height(8.dp))
                        Text("A troca só é possível na fase de reforço.", color = Color(0xFFFFB74D), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    )
}

@Composable
fun VictoryDialog(winnerName: String, humanWon: Boolean, objectiveDesc: String, onMenu: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = onMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Menu principal", fontWeight = FontWeight.Bold) }
        },
        title = {
            Text(
                if (humanWon) "🏆 VITÓRIA!" else "Fim de jogo",
                color = Gold,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (humanWon) "Você dominou o mundo!" else "$winnerName venceu a partida.",
                    color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text("Objetivo cumprido:", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Text(objectiveDesc, color = TextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

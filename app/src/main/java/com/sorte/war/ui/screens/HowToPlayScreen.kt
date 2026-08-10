package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.model.MapData
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.TextSecondary

@Composable
fun HowToPlayScreen(vm: GameViewModel) {
    BackHandler { vm.goTo(Screen.HOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1D27), Color(0xFF07111A), NightNavy)))
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
                "COMO JOGAR",
                color = Gold, fontWeight = FontWeight.Black,
                fontSize = 20.sp, letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        Step(
            "1", "Objetivo secreto",
            "Cada jogador recebe uma carta de objetivo secreta: conquistar certos " +
                "continentes, tomar 24 territórios ou destruir os exércitos de uma cor. " +
                "Toque na bandeira, no topo da tela, para ver o seu. Vence quem cumprir " +
                "o objetivo primeiro — ou quem sobrar sozinho."
        )
        Step(
            "2", "Reforço",
            "No início do turno você recebe exércitos: o número de territórios seus " +
                "dividido por 2 (mínimo 3), mais o bônus dos continentes que dominar " +
                "por inteiro. Toque nos seus territórios para posicionar as tropas."
        )
        Step(
            "3", "Ataque",
            "Toque num território seu com 2 ou mais exércitos e depois num vizinho " +
                "inimigo destacado. Os dados decidem: até 3 de ataque contra até 3 de " +
                "defesa, comparados do maior para o menor. Empate favorece o defensor. " +
                "Ao conquistar, você escolhe quantas tropas avançam."
        )
        Step(
            "4", "Deslocamento",
            "Uma vez por turno você pode mover tropas entre dois territórios seus " +
                "ligados por um caminho de territórios também seus."
        )
        Step(
            "5", "Cartas",
            "Conquistando ao menos um território no turno, você ganha uma carta. " +
                "Na fase de reforço, troque 3 cartas (três iguais, uma de cada símbolo, " +
                "ou com coringa) por exércitos extras — o valor aumenta a cada troca. " +
                "Com 5 cartas na mão, a troca é obrigatória."
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "MODOS DE JOGO",
            color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        ModeCard(
            "MODO CLÁSSICO",
            "As regras do tabuleiro, exatamente como descritas acima. Nada de " +
                "fortificações com efeito, cartas táticas ou momentum."
        )
        ModeCard(
            "MODO TÁTICO",
            "Tudo do clássico, mais quatro camadas:\n\n" +
                "• FORTIFICAÇÕES — o contingente define o nível do território: " +
                "5 a 7 vira Posto Avançado, 8 a 11 vira Bunker e 12 ou mais vira " +
                "Fortaleza. Cada nível soma +1 a um dado de defesa (nunca acima de 6) " +
                "e o nível cai sozinho se as tropas caírem.\n\n" +
                "• MOMENTUM — conquistar 2 territórios num turno rende uma carta " +
                "tática; 3, 4 ou 5+ conquistas rendem +1, +2 ou +3 exércitos no turno " +
                "seguinte, no máximo +3.\n\n" +
                "• CARTAS TÁTICAS — um segundo baralho, separado das cartas de " +
                "território. Abra CARTAS na barra superior e use a aba TÁTICAS. Cada " +
                "carta diz em que fase funciona e, quando não puder ser usada, o " +
                "motivo aparece na tela. A CPU também recebe e usa essas cartas.\n\n" +
                "• MISSÕES DE CAMPANHA — cada exército recebe uma das cartas 16 a 21 " +
                "como missão pública. Cumpri-la rende uma recompensa (exércitos, cartas " +
                "táticas ou bônus de troca), nunca a vitória: essa continua vindo do " +
                "objetivo secreto ou da eliminação dos adversários.\n\n" +
                "• RELATÓRIO DO ALTO COMANDO — ao fim de cada rodada completa, um " +
                "balanço com conquistas, perdas, baixas causadas e sofridas, " +
                "fortificações e momentum de cada exército."
        )

        Spacer(Modifier.height(14.dp))
        Text("BÔNUS DE CONTINENTE", color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PanelNavy)
                .padding(vertical = 6.dp)
        ) {
            MapData.continents.forEach { c ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        Modifier.size(12.dp).clip(CircleShape).background(Color(c.colorArgb))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(c.name, color = Color.White, modifier = Modifier.weight(1f), fontSize = 13.sp)
                    Text(
                        "+${c.bonus}",
                        color = Gold, fontWeight = FontWeight.Black, fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "Dica: no mapa, use dois dedos para dar zoom e arraste para navegar. " +
                "Fortificações aparecem nos territórios conforme o contingente cresce.",
            color = TextSecondary, style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ModeCard(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PanelNavy)
            .padding(14.dp)
    ) {
        Text(
            title,
            color = Gold, fontWeight = FontWeight.Black,
            fontSize = 12.sp, letterSpacing = 1.4.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Step(number: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Gold),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = NightNavy, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(3.dp))
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

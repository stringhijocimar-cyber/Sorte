package com.sorte.war.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.model.CardArt
import com.sorte.war.model.CardFamily
import com.sorte.war.model.ClassicCardArt
import com.sorte.war.model.MapData
import com.sorte.war.model.TacticalCard
import com.sorte.war.model.TacticalMedal
import com.sorte.war.ui.GameViewModel
import com.sorte.war.ui.Screen
import com.sorte.war.ui.theme.Divider
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.TacticalStroke
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary

/**
 * Arsenal: o baralho ilustrado completo, agrupado por família.
 *
 * É uma vitrine — não altera nenhuma regra. Tocar numa carta abre a arte em
 * tela cheia, para ver a ilustração e o texto impresso.
 */
@Composable
fun ArsenalScreen(vm: GameViewModel) {
    var zoomed by remember { mutableStateOf<CardArt?>(null) }

    BackHandler {
        if (zoomed != null) zoomed = null else vm.goTo(Screen.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A1D27), Color(0xFF07111A), NightNavy))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
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
                Column {
                    Text(
                        "ARSENAL",
                        color = Gold, fontWeight = FontWeight.Black,
                        fontSize = 20.sp, letterSpacing = 2.sp
                    )
                    Text(
                        "${CardArt.all.size} CARTAS ILUSTRADAS",
                        color = TextSecondary, fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "O baralho de coleção do WAR. Toque em uma carta para vê-la de perto.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            CardFamily.entries.forEach { family ->
                val cards = CardArt.byFamily(family)
                if (cards.isEmpty()) return@forEach

                Spacer(Modifier.height(18.dp))
                FamilyHeader(family, cards.size)
                Spacer(Modifier.height(9.dp))

                // grade de 3 colunas montada com Rows, para conviver com o scroll da tela
                cards.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)
                    ) {
                        row.forEach { card ->
                            CardThumb(
                                card = card,
                                modifier = Modifier.weight(1f),
                                onClick = { zoomed = card }
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // Cartas de território ilustradas
            val total = MapData.territories.size
            val ready = ClassicCardArt.availableTerritories(total)
            if (ready.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 3.dp, height = 16.dp)
                            .background(Gold, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "TERRITÓRIOS",
                        color = Gold, fontSize = 11.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 1.8.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${ready.size}/$total",
                        color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Divider))
                Spacer(Modifier.height(9.dp))

                (listOf(-1) + ready).chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)
                    ) {
                        row.forEach { id ->
                            TerritoryThumb(id = id, modifier = Modifier.weight(1f))
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                if (!ClassicCardArt.isComplete(total)) {
                    Text(
                        "Faltam ${total - ready.size} cartas de território. Enquanto o " +
                            "baralho não fecha, a mão da partida continua com as cartas " +
                            "desenhadas, para não misturar dois estilos.",
                        color = TextSecondary.copy(alpha = 0.85f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
        }

        zoomed?.let { card ->
            CardZoomOverlay(card = card, onClose = { zoomed = null })
        }
    }
}

@Composable
private fun FamilyHeader(family: CardFamily, count: Int) {
    val accent = Color(family.accentArgb)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 16.dp)
                .background(accent, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            family.label.uppercase(),
            color = accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.8.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            "$count",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(6.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Divider)
    )
}

@Composable
private fun CardThumb(
    card: CardArt,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.667f)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF06090E))
                .border(1.dp, TacticalStroke, RoundedCornerShape(9.dp))
                .clickableNoRipple(onClick)
        ) {
            Image(
                painter = painterResource(id = card.art),
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (card.power != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC06090E))
                        .border(1.dp, Gold.copy(alpha = 0.8f), CircleShape)
                ) {
                    Text(
                        "${card.power}",
                        color = Gold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            card.title,
            color = TextPrimary,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TerritoryThumb(id: Int, modifier: Modifier = Modifier) {
    val art = if (id < 0) ClassicCardArt.jokerArt else ClassicCardArt.of(id) ?: return
    val name = if (id < 0) "Coringa" else MapData.territory(id).name
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.667f)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF06090E))
                .border(1.dp, TacticalStroke, RoundedCornerShape(9.dp))
        ) {
            Image(
                painter = painterResource(id = art),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            name,
            color = TextPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold,
            maxLines = 1, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CardZoomOverlay(card: CardArt, onClose: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2040709))
            .clickableNoRipple(onClose)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .systemBarsPadding()
                .padding(horizontal = 26.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(id = card.art),
                contentDescription = card.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.height(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelNavy.copy(alpha = 0.94f))
                    .border(1.dp, Color(card.family.accentArgb).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "CARTA ${card.number} • ${card.family.label.uppercase()}",
                    color = Color(card.family.accentArgb),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    card.title.uppercase(),
                    color = Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                // as jogáveis mostram a regra que vale de fato na partida
                val rule = TacticalCard.all.firstOrNull { it.number == card.number }
                val medal = TacticalMedal.all.firstOrNull { it.number == card.number }
                Text(
                    rule?.rule ?: medal?.requirement ?: card.effect,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (rule != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "QUANDO: ${rule.timing.uppercase()}",
                        color = Color(card.family.accentArgb),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.9.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (medal != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (medal.assignable) "MISSÃO DE CAMPANHA" else "MEDALHA DE HONRA",
                        color = Color(card.family.accentArgb),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.9.sp
                    )
                    if (medal.assignable) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Recompensa: ${medal.rewardText}",
                            color = Gold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x22FFFFFF))
                    .clickableNoRipple(onClose)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Fechar",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("FECHAR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

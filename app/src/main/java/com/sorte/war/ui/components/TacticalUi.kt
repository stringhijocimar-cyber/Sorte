package com.sorte.war.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.Card as GameCard
import com.sorte.war.model.CardArt
import com.sorte.war.model.Fortification
import com.sorte.war.model.MapData
import com.sorte.war.model.Phase
import com.sorte.war.model.RoundReport
import com.sorte.war.model.TacticalCard
import com.sorte.war.model.TacticalTarget
import com.sorte.war.ui.screens.clickableNoRipple
import com.sorte.war.ui.theme.Crimson
import com.sorte.war.ui.theme.Divider
import com.sorte.war.ui.theme.Gold
import com.sorte.war.ui.theme.NightNavy
import com.sorte.war.ui.theme.PanelNavy
import com.sorte.war.ui.theme.SurfaceHigh
import com.sorte.war.ui.theme.TacticalGreen
import com.sorte.war.ui.theme.TacticalStroke
import com.sorte.war.ui.theme.TacticalTeal
import com.sorte.war.ui.theme.TextPrimary
import com.sorte.war.ui.theme.TextSecondary

/** Recorte superior de uma carta ilustrada: título e ilustração, sem o texto. */
@Composable
fun CardArtBanner(
    art: CardArt,
    modifier: Modifier = Modifier,
    ratio: Float = 1.45f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF06090E))
    ) {
        Image(
            painter = painterResource(id = art.art),
            contentDescription = art.title,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ---------------------------------------------------------------------------
// TELA DE CARTAS — abas TERRITÓRIO e TÁTICAS
// ---------------------------------------------------------------------------

/**
 * Mão do jogador. A aba TERRITÓRIO mantém as cartas do tabuleiro com a mesma
 * regra de troca de sempre; a aba TÁTICAS só existe no Modo Tático.
 */
@Composable
fun CardsDialog(
    engine: GameEngine,
    refresh: Int,
    onTrade: (List<GameCard>) -> Unit,
    onPlayTactical: (TacticalCard, Int?, Int?, Int, Int?) -> Unit,
    onClose: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION") refresh
    val player = engine.currentPlayer
    val cards = player.cards.toList()
    val tacticalHand = player.tacticalCards.toList()

    var tab by remember { mutableIntStateOf(0) }
    val selected = remember { mutableStateListOf<Int>() }
    var openCard by remember { mutableStateOf<TacticalCard?>(null) }

    val chosen = selected.mapNotNull { cards.getOrNull(it) }
    val valid = chosen.size == 3 && engine.isValidCardSet(chosen)
    val canTradeNow = engine.phase == Phase.REFORCO && player.isHuman

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PanelNavy,
        confirmButton = {
            if (tab == 0) {
                Button(
                    onClick = { if (valid) { onTrade(chosen); selected.clear() } },
                    enabled = valid && canTradeNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold, contentColor = NightNavy
                    )
                ) { Text("Trocar (+${engine.nextTradeBonus()})", fontWeight = FontWeight.Bold) }
            } else {
                TextButton(onClick = onClose) { Text("Fechar", color = Gold) }
            }
        },
        dismissButton = {
            if (tab == 0) TextButton(onClick = onClose) { Text("Fechar", color = TextSecondary) }
        },
        title = {
            Column {
                Text("Suas cartas", color = Gold, fontWeight = FontWeight.Bold)
                if (engine.tactical) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CardTab("TERRITÓRIO ${cards.size}", tab == 0) { tab = 0 }
                        CardTab("TÁTICAS ${tacticalHand.size}", tab == 1) { tab = 1 }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (tab == 0 || !engine.tactical) {
                    TerritoryCardsTab(
                        cards = cards,
                        selected = selected,
                        canTradeNow = canTradeNow
                    )
                } else {
                    TacticalCardsTab(
                        engine = engine,
                        hand = tacticalHand,
                        onOpen = { openCard = it }
                    )
                }
            }
        }
    )

    openCard?.let { card ->
        TacticalCardDialog(
            engine = engine,
            card = card,
            onClose = { openCard = null },
            onPlay = { primary, secondary, amount, targetPlayer ->
                openCard = null
                onPlayTactical(card, primary, secondary, amount, targetPlayer)
                onClose()
            }
        )
    }
}

@Composable
private fun CardTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) Gold.copy(alpha = 0.16f) else SurfaceHigh.copy(alpha = 0.7f))
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) Gold else TacticalStroke,
                RoundedCornerShape(9.dp)
            )
            .clickableNoRipple(onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Gold else TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun TerritoryCardsTab(
    cards: List<GameCard>,
    selected: MutableList<Int>,
    canTradeNow: Boolean
) {
    if (cards.isEmpty()) {
        Text(
            "Você ainda não possui cartas. Conquiste um território para receber uma.",
            color = TextSecondary
        )
        return
    }
    Text(
        "Selecione 3 cartas: três iguais, uma de cada símbolo, ou usando coringa.",
        color = TextSecondary, style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(10.dp))
    cards.chunked(3).forEachIndexed { rowIdx, rowCards ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            rowCards.forEachIndexed { colIdx, card ->
                val i = rowIdx * 3 + colIdx
                val isSel = i in selected
                WarCard(
                    card = card,
                    selected = isSel,
                    modifier = Modifier.clickableNoRipple {
                        if (isSel) selected.remove(i)
                        else if (selected.size < 3) selected.add(i)
                    }
                )
            }
        }
    }
    if (!canTradeNow) {
        Spacer(Modifier.height(8.dp))
        Text(
            "A troca só é possível na fase de reforço.",
            color = Color(0xFFFFB74D),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun TacticalCardsTab(
    engine: GameEngine,
    hand: List<TacticalCard>,
    onOpen: (TacticalCard) -> Unit
) {
    if (hand.isEmpty()) {
        Text(
            "Nenhuma carta tática na mão. Conquiste 2 ou mais territórios num " +
                "mesmo turno para receber uma.",
            color = TextSecondary
        )
        return
    }
    Text(
        "Toque numa carta para ver a regra e usá-la.",
        color = TextSecondary, style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(10.dp))
    hand.chunked(2).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)
        ) {
            row.forEach { card ->
                val blocked = engine.tacticalBlockReason(card)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickableNoRipple { onOpen(card) }
                ) {
                    Box {
                        CardArtBanner(CardArt.of(card), ratio = 0.85f)
                        if (blocked != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xB305080C))
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        card.title,
                        color = if (blocked == null) TextPrimary else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        if (blocked == null) "PRONTA" else "INDISPONÍVEL",
                        color = if (blocked == null) TacticalGreen else Crimson,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }
            repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/** Carta tática aberta: arte, regra, quando pode ser usada e escolha do alvo. */
@Composable
private fun TacticalCardDialog(
    engine: GameEngine,
    card: TacticalCard,
    onClose: () -> Unit,
    onPlay: (Int?, Int?, Int, Int?) -> Unit
) {
    val blocked = engine.tacticalBlockReason(card)
    var from by remember(card) { mutableStateOf<Int?>(null) }
    var to by remember(card) { mutableStateOf<Int?>(null) }
    var targetPlayer by remember(card) { mutableStateOf<Int?>(null) }

    val needsPair = card.target == TacticalTarget.PAR_PROPRIO_VIZINHO ||
        card.target == TacticalTarget.PAR_PROPRIO_QUALQUER

    val ready = blocked == null && when (card.target) {
        TacticalTarget.NENHUM -> true
        TacticalTarget.TERRITORIO_PROPRIO -> from != null
        TacticalTarget.INIMIGO_VIZINHO, TacticalTarget.INIMIGO_QUALQUER -> from != null
        TacticalTarget.PAR_PROPRIO_VIZINHO, TacticalTarget.PAR_PROPRIO_QUALQUER ->
            from != null && to != null
        TacticalTarget.JOGADOR -> targetPlayer != null
    }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = { if (ready) onPlay(from, to, 3, targetPlayer) },
                enabled = ready,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("USAR CARTA", fontWeight = FontWeight.Black) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Voltar", color = TextSecondary) } },
        title = { Text(card.title.uppercase(), color = Gold, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CardArtBanner(CardArt.of(card), ratio = 1.5f)
                Spacer(Modifier.height(10.dp))

                Text(card.rule, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "QUANDO: ${card.timing.uppercase()}",
                    color = TacticalTeal, fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 0.9.sp
                )

                if (blocked != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Crimson.copy(alpha = 0.12f))
                            .border(1.dp, Crimson.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(blocked, color = Color(0xFFFFC9C4), fontSize = 11.sp)
                    }
                    return@Column
                }

                when (card.target) {
                    TacticalTarget.NENHUM -> {}

                    TacticalTarget.TERRITORIO_PROPRIO -> TerritoryPicker(
                        engine = engine,
                        label = "ESCOLHA O TERRITÓRIO",
                        options = engine.territoriesOf(engine.currentPlayerIndex),
                        selected = from,
                        onPick = { from = it }
                    )

                    TacticalTarget.INIMIGO_VIZINHO -> TerritoryPicker(
                        engine = engine,
                        label = "ALVO INIMIGO VIZINHO",
                        options = engine.airStrikeTargets(),
                        selected = from,
                        onPick = { from = it }
                    )

                    TacticalTarget.INIMIGO_QUALQUER -> TerritoryPicker(
                        engine = engine,
                        label = "ALVO INIMIGO",
                        options = engine.bombardTargets(),
                        selected = from,
                        onPick = { from = it }
                    )

                    TacticalTarget.PAR_PROPRIO_VIZINHO, TacticalTarget.PAR_PROPRIO_QUALQUER -> {
                        TerritoryPicker(
                            engine = engine,
                            label = "DE ONDE SAEM AS TROPAS",
                            options = engine.troopSources().filter {
                                engine.troopDestinations(card, it).isNotEmpty()
                            },
                            selected = from,
                            onPick = { from = it; to = null }
                        )
                        if (from != null) {
                            Spacer(Modifier.height(8.dp))
                            TerritoryPicker(
                                engine = engine,
                                label = "PARA ONDE VÃO",
                                options = engine.troopDestinations(card, from!!),
                                selected = to,
                                onPick = { to = it }
                            )
                        }
                    }

                    TacticalTarget.JOGADOR -> PlayerPicker(
                        engine = engine,
                        selected = targetPlayer,
                        onPick = { targetPlayer = it }
                    )
                }

                if (needsPair && from != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Serão movidos até 3 exércitos, respeitando o mínimo de 1 na origem.",
                        color = TextSecondary, fontSize = 10.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun TerritoryPicker(
    engine: GameEngine,
    label: String,
    options: List<Int>,
    selected: Int?,
    onPick: (Int) -> Unit
) {
    Spacer(Modifier.height(10.dp))
    Text(
        label,
        color = Gold, fontSize = 8.5.sp,
        fontWeight = FontWeight.Black, letterSpacing = 1.sp
    )
    Spacer(Modifier.height(6.dp))
    if (options.isEmpty()) {
        Text("Nenhuma opção disponível.", color = TextSecondary, fontSize = 10.sp)
        return
    }
    options.sortedBy { MapData.territory(it).name }.chunked(2).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            row.forEach { id ->
                val isSel = selected == id
                val fort = engine.fortificationOf(id)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isSel) Gold.copy(alpha = 0.16f) else SurfaceHigh.copy(alpha = 0.7f)
                        )
                        .border(
                            if (isSel) 1.5.dp else 1.dp,
                            if (isSel) Gold else TacticalStroke,
                            RoundedCornerShape(9.dp)
                        )
                        .clickableNoRipple { onPick(id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        MapData.territory(id).name,
                        color = if (isSel) Gold else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        "⚔ ${engine.armiesOf[id]}" +
                            if (fort != Fortification.NENHUMA) "  •  ${fort.title}" else "",
                        color = TextSecondary, fontSize = 8.sp, maxLines = 1
                    )
                }
            }
            repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun PlayerPicker(engine: GameEngine, selected: Int?, onPick: (Int) -> Unit) {
    Spacer(Modifier.height(10.dp))
    Text(
        "ESCOLHA O ADVERSÁRIO",
        color = Gold, fontSize = 8.5.sp,
        fontWeight = FontWeight.Black, letterSpacing = 1.sp
    )
    Spacer(Modifier.height(6.dp))
    engine.rivals().forEach { id ->
        val p = engine.players[id]
        val isSel = selected == id
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSel) Gold.copy(alpha = 0.14f) else SurfaceHigh.copy(alpha = 0.7f))
                .border(
                    if (isSel) 1.5.dp else 1.dp,
                    if (isSel) Gold else TacticalStroke,
                    RoundedCornerShape(10.dp)
                )
                .clickableNoRipple { onPick(id) }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            AvatarPortrait(
                avatar = com.sorte.war.model.Avatar.byId(p.avatarId),
                sizeDp = 26,
                ringColor = Color(p.colorArgb)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    p.name,
                    color = if (isSel) Gold else TextPrimary,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1
                )
                Text(
                    "${engine.ownedCount(id)} territórios",
                    color = TextSecondary, fontSize = 8.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// RELATÓRIO DO ALTO COMANDO
// ---------------------------------------------------------------------------

/** Fechamento de rodada com os números reais colhidos pelo motor. */
@Composable
fun HighCommandReportDialog(
    engine: GameEngine,
    report: RoundReport,
    flavor: String?,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PanelNavy,
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = NightNavy)
            ) { Text("Às ordens!", fontWeight = FontWeight.Bold) }
        },
        title = {
            Column {
                Text(
                    "RELATÓRIO DO ALTO COMANDO",
                    color = Gold, fontWeight = FontWeight.Black,
                    fontSize = 14.sp, letterSpacing = 1.2.sp
                )
                Text(
                    "RODADA ${report.roundNumber}",
                    color = TacticalTeal, fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 1.4.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!flavor.isNullOrBlank()) {
                    Text(flavor, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                }

                SectionLabel("DESTAQUES DA RODADA")
                val name = { id: Int -> engine.players.getOrNull(id)?.name ?: "—" }
                Highlight("Maior conquistador", report.topConqueror?.let {
                    "${name(it.playerId)} conquistou ${it.conquered} territórios"
                })
                Highlight("Maior perda territorial", report.topLoser?.let {
                    "${name(it.playerId)} perdeu ${it.lost} territórios"
                })
                Highlight("Maior poder militar", report.topArmy?.let {
                    "${name(it.playerId)} soma ${it.armiesAfter} exércitos"
                })
                Highlight("Mais baixas causadas", report.topKiller?.let {
                    "${name(it.playerId)} eliminou ${it.casualtiesDealt} tropas inimigas"
                })
                Highlight("Mais baixas sofridas", report.topVictim?.let {
                    "${name(it.playerId)} perdeu ${it.casualtiesTaken} tropas"
                })
                Highlight("Maior avanço", report.topAdvance?.let {
                    "${name(it.playerId)} cresceu ${it.territoryDelta} territórios"
                })

                if (report.events.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    SectionLabel("ACONTECIMENTOS")
                    report.events.forEach {
                        Text("• $it", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                SectionLabel("POR EXÉRCITO")
                report.stats.forEach { st ->
                    val p = engine.players.getOrNull(st.playerId) ?: return@forEach
                    ArmyReportRow(engine, st.playerId, st, p.name, p.eliminated)
                }
            }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = TacticalTeal, fontSize = 8.5.sp,
        fontWeight = FontWeight.Black, letterSpacing = 1.4.sp
    )
    Spacer(Modifier.height(3.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(Divider))
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun Highlight(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label.uppercase(),
            color = TextSecondary, fontSize = 8.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.42f)
        )
        Text(
            value ?: "sem registro",
            color = if (value != null) Gold else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (value != null) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun ArmyReportRow(
    engine: GameEngine,
    playerId: Int,
    st: com.sorte.war.model.ArmyRoundStats,
    name: String,
    eliminated: Boolean
) {
    val p = engine.players[playerId]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .border(1.dp, TacticalStroke.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarPortrait(
                avatar = com.sorte.war.model.Avatar.byId(p.avatarId),
                sizeDp = 26,
                ringColor = Color(p.colorArgb)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (eliminated) "$name (eliminado)" else name,
                color = TextPrimary, fontSize = 11.sp,
                fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), maxLines = 1
            )
            if (st.momentum > 0) {
                Text(
                    "MOMENTUM +${st.momentum}",
                    color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        DeltaLine("Territórios", st.territoriesBefore, st.territoriesAfter, st.territoryDelta)
        DeltaLine("Exércitos", st.armiesBefore, st.armiesAfter, st.armyDelta)
        Spacer(Modifier.height(4.dp))
        StatLine("Conquistados", "${st.conquered}", TacticalGreen)
        StatLine("Perdidos", "${st.lost}", Crimson)
        StatLine("Baixas causadas", "${st.casualtiesDealt}", TacticalGreen)
        StatLine("Baixas sofridas", "${st.casualtiesTaken}", Crimson)
        if (engine.tactical) {
            StatLine("Fortalezas", "${st.fortresses}", Gold)
            StatLine("Bunkers", "${st.bunkers}", TacticalTeal)
            StatLine("Cartas táticas usadas", "${st.tacticalUsed}", TacticalTeal)
        }
    }
}

@Composable
private fun DeltaLine(label: String, before: Int, after: Int, delta: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextSecondary, fontSize = 9.sp, modifier = Modifier.weight(1f))
        Text(
            "$before → $after",
            color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (delta >= 0) "+$delta" else "$delta",
            color = when {
                delta > 0 -> TacticalGreen
                delta < 0 -> Crimson
                else -> TextSecondary
            },
            fontSize = 9.sp, fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun StatLine(label: String, value: String, accent: Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextSecondary, fontSize = 9.sp, modifier = Modifier.weight(1f))
        Text(value, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

/** Medalhas táticas conquistadas até agora, para a tela de cartas/relatório. */
@Composable
fun MedalStrip(engine: GameEngine, playerId: Int, modifier: Modifier = Modifier) {
    val medals = engine.players.getOrNull(playerId)?.medals.orEmpty()
    if (medals.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        medals.forEach { medal ->
            Box(
                modifier = Modifier
                    .size(width = 34.dp, height = 46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Gold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            ) {
                Image(
                    painter = painterResource(id = CardArt.of(medal).art),
                    contentDescription = medal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/** Cabeçalho ilustrado de uma missão secreta. */
@Composable
fun ObjectiveArt(objective: com.sorte.war.model.Objective?, modifier: Modifier = Modifier) {
    CardArtBanner(CardArt.forObjective(objective), modifier = modifier, ratio = 1.6f)
}

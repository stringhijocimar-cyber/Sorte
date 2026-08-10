package com.sorte.war.engine

import com.sorte.war.model.ArmyColor
import com.sorte.war.model.ArmyRoundStats
import com.sorte.war.model.ArmySnapshot
import com.sorte.war.model.BattleLogEntry
import com.sorte.war.model.Card
import com.sorte.war.model.CardSymbol
import com.sorte.war.model.Difficulty
import com.sorte.war.model.Fortification
import com.sorte.war.model.GameMode
import com.sorte.war.model.MapData
import com.sorte.war.model.Objective
import com.sorte.war.model.Objectives
import com.sorte.war.model.Phase
import com.sorte.war.model.Player
import com.sorte.war.model.PlayerPalette
import com.sorte.war.model.RoundReport
import com.sorte.war.model.RoundSnapshot
import com.sorte.war.model.SetupMode
import com.sorte.war.model.TacticalCard
import com.sorte.war.model.TacticalMedal
import com.sorte.war.model.TacticalTarget
import kotlin.random.Random

/**
 * Motor de regras do War. Mantém todo o estado da partida e expõe as ações
 * (reforçar, atacar, deslocar, trocar cartas, avançar de fase). A camada de UI
 * observa o estado por meio de um "tick" no ViewModel após cada mutação.
 */
class GameEngine(
    playerConfigs: List<PlayerConfig>,
    val difficulty: Difficulty = Difficulty.VETERANO,
    val setupMode: SetupMode = SetupMode.DADOS,
    /** Quantas cartas de objetivo o jogador humano recebe para escolher. */
    private val objectiveChoices: Int = 1,
    private val rng: Random = Random(System.nanoTime()),
    skipSetup: Boolean = false,
    /** Clássico mantém as regras do tabuleiro; tático liga a expansão. */
    val mode: GameMode = GameMode.CLASSICO
) {
    data class PlayerConfig(
        val name: String,
        val color: ArmyColor,
        val isHuman: Boolean,
        val avatarId: Int = 0
    )

    val players: List<Player> = playerConfigs.mapIndexed { i, c ->
        Player(
            id = i, name = c.name, colorArgb = c.color.argb,
            isHuman = c.isHuman, avatarId = c.avatarId
        )
    }

    private val n = MapData.territories.size
    val ownerOf = IntArray(n) { -1 }
    val armiesOf = IntArray(n) { 0 }

    var currentPlayerIndex = 0
        private set
    var phase = Phase.REFORCO
        private set
    var reinforcements = 0
        private set
    var winnerId: Int? = null
        private set

    /** Território conquistado neste turno? (Direito a uma carta ao fim do turno.) */
    var conqueredThisTurn = false
        private set
    var fortifyUsed = false
        private set

    var lastBattle: com.sorte.war.model.BattleResult? = null
        private set

    /** Opção de avançar tropas adicionais após conquistar (decisão do jogador humano). */
    data class AdvanceOption(val from: Int, val to: Int, val min: Int, val max: Int)
    var pendingAdvance: AdvanceOption? = null
        private set

    /** Quem eliminou cada jogador (para objetivos de destruição). -1 = ninguém. */
    private val eliminatedBy = IntArray(players.size) { -1 }

    private val drawPile = ArrayDeque<Card>()
    private val discardPile = mutableListOf<Card>()
    private var setsTraded = 0

    /** Dado tirado por cada exército no sorteio inicial (vazio no modo aleatório). */
    var initialRolls: IntArray = IntArray(0)
        private set

    /** Índice de quem abriu a partida (vencedor do sorteio). */
    var startingPlayer: Int = 0
        private set

    /**
     * Cartas de objetivo oferecidas ao jogador humano. Enquanto não estiver
     * vazia, a UI mostra as cartas para ele escolher uma.
     */
    var objectiveOptions: List<Objective> = emptyList()
        private set

    // ---------------------------------------------------------------------
    // ESTADO DO MODO TÁTICO
    // Tudo aqui fica neutro (zerado/vazio) quando a partida é clássica.
    // ---------------------------------------------------------------------

    val tactical: Boolean get() = mode == GameMode.TATICO

    /** Turnos já encerrados. Serve de relógio para tréguas e para o log. */
    var turnNumber = 0
        private set

    /** Rodada completa em andamento (todos os exércitos jogam uma vez). */
    var roundNumber = 1
        private set

    /** Reforço de Momentum guardado para o próximo turno de cada exército. */
    private val momentumOf = IntArray(players.size)

    /** Momentum que entrou no reforço do turno atual (para mostrar no HUD). */
    var momentumApplied = 0
        private set

    /** Territórios tomados pelo jogador da vez neste turno. */
    var conquestsThisTurn = 0
        private set

    private val tacticalDraw = ArrayDeque<TacticalCard>()
    private val tacticalDiscard = mutableListOf<TacticalCard>()

    /** Bônus de continente anulado por Sabotagem, por exército. */
    private val continentBonusBlocked = BooleanArray(players.size)

    /** Turno até o qual vale a trégua entre dois exércitos (Diplomacia). */
    private val truceUntil = Array(players.size) { IntArray(players.size) }

    /** Deslocamentos extras liberados por Marcha Forçada neste turno. */
    var extraFortifies = 0
        private set

    // Modificadores preparados para o próximo ataque (consumidos ao atacar).
    private var attackDiceBonus = 0
    private var seaRouteDiceBonus = 0
    private var ignoreFortification = false

    /** Combates da rodada em curso, base do relatório. */
    val battleLog = mutableListOf<BattleLogEntry>()
    private val roundEvents = mutableListOf<String>()
    private val tacticalUsedThisRound = IntArray(players.size)
    private var snapshot: RoundSnapshot? = null

    /** Relatório fechado da última rodada completa (null enquanto não houver). */
    var lastRoundReport: RoundReport? = null
        private set

    /** Já esteve bem atrás do líder? (usado pela medalha Reviravolta.) */
    private val wasBehind = BooleanArray(players.size)

    val currentPlayer: Player get() = players[currentPlayerIndex]

    init {
        if (!skipSetup) {
            setupBoard()
            assignObjectives()
            buildCardDeck()
            if (tactical) buildTacticalDeck()
            currentPlayerIndex = startingPlayer
            takeSnapshot()
            startTurn()
        }
    }

    // ---------------------------------------------------------------------
    // SETUP
    // ---------------------------------------------------------------------

    private fun setupBoard() {
        // Quem começa: no modo DADOS, o maior dado (com desempate por nova rolagem).
        startingPlayer = if (setupMode == SetupMode.DADOS) {
            var rolls: IntArray
            var winners: List<Int>
            var guard = 0
            do {
                rolls = IntArray(players.size) { rng.nextInt(6) + 1 }
                val best = rolls.max()
                winners = rolls.indices.filter { rolls[it] == best }
            } while (winners.size > 1 && guard++ < 20)
            initialRolls = rolls
            winners.first()
        } else {
            initialRolls = IntArray(0)
            rng.nextInt(players.size)
        }

        val ids = (0 until n).toMutableList().also { it.shuffle(rng) }
        // Distribui territórios em rodízio, começando por quem venceu o sorteio.
        ids.forEachIndexed { index, tId ->
            val p = (startingPlayer + index) % players.size
            ownerOf[tId] = p
            armiesOf[tId] = 1
        }
        // Exércitos iniciais por jogador (padrão clássico).
        val startArmies = when (players.size) {
            2 -> 40; 3 -> 35; 4 -> 30; 5 -> 25; else -> 20
        }
        for (p in players.indices) {
            val owned = (0 until n).filter { ownerOf[it] == p }
            var remaining = startArmies - owned.size
            while (remaining > 0) {
                val t = owned[rng.nextInt(owned.size)]
                armiesOf[t]++
                remaining--
            }
        }
    }

    private fun assignObjectives() {
        val deck = Objectives.buildDeck().shuffled(rng).toMutableList()
        val colorsInPlay = players.map { it.colorArgb }.toSet()
        for (p in players) {
            if (p.isHuman && objectiveChoices > 1) {
                // Oferece algumas cartas; o objetivo só é definido na escolha.
                val opts = ArrayList<Objective>(objectiveChoices)
                repeat(objectiveChoices.coerceAtMost(deck.size)) {
                    opts.add(sanitizeObjective(deck.removeAt(0), p, colorsInPlay))
                }
                objectiveOptions = opts
                p.objective = opts.first()   // provisório, até a escolha
            } else {
                p.objective = sanitizeObjective(deck.removeAt(0), p, colorsInPlay)
            }
        }
    }

    /** Confirma a carta de objetivo escolhida pelo jogador humano. */
    fun chooseObjective(index: Int) {
        val opts = objectiveOptions
        if (opts.isEmpty()) return
        val chosen = opts.getOrNull(index) ?: opts.first()
        players.firstOrNull { it.isHuman }?.objective = chosen
        objectiveOptions = emptyList()
    }

    private fun sanitizeObjective(
        obj: Objective, owner: Player, colorsInPlay: Set<Long>
    ): Objective {
        if (obj is Objective.DestroyPlayer) {
            if (obj.targetColorArgb == owner.colorArgb || obj.targetColorArgb !in colorsInPlay) {
                return Objectives.fallback()
            }
        }
        return obj
    }

    private fun buildCardDeck() {
        val symbols = listOf(CardSymbol.CIRCULO, CardSymbol.QUADRADO, CardSymbol.TRIANGULO)
        MapData.territories.forEach { t ->
            drawPile.add(Card(symbols[t.id % 3], t.id))
        }
        drawPile.add(Card(CardSymbol.CORINGA, -1))
        drawPile.add(Card(CardSymbol.CORINGA, -1))
        drawPile.shuffle()
    }

    private fun drawCard(): Card? {
        if (drawPile.isEmpty()) {
            if (discardPile.isEmpty()) return null
            drawPile.addAll(discardPile)
            discardPile.clear()
            drawPile.shuffle()
        }
        return if (drawPile.isEmpty()) null else drawPile.removeFirst()
    }

    private fun buildTacticalDeck() {
        tacticalDraw.clear()
        tacticalDraw.addAll(TacticalCard.buildDeck().shuffled(rng))
    }

    private fun drawTactical(): TacticalCard? {
        if (!tactical) return null
        if (tacticalDraw.isEmpty()) {
            if (tacticalDiscard.isEmpty()) return null
            tacticalDraw.addAll(tacticalDiscard.shuffled(rng))
            tacticalDiscard.clear()
        }
        return if (tacticalDraw.isEmpty()) null else tacticalDraw.removeFirst()
    }

    /** Entrega uma carta tática, respeitando o limite de mão. */
    private fun giveTactical(playerId: Int): TacticalCard? {
        val card = drawTactical() ?: return null
        val hand = players[playerId].tacticalCards
        hand.add(card)
        // mão cheia: a carta mais antiga volta para o descarte
        while (hand.size > TACTICAL_HAND_LIMIT) tacticalDiscard.add(hand.removeAt(0))
        return card
    }

    // ---------------------------------------------------------------------
    // CONSULTAS
    // ---------------------------------------------------------------------

    fun territoriesOf(playerId: Int): List<Int> = (0 until n).filter { ownerOf[it] == playerId }
    fun ownedCount(playerId: Int): Int = (0 until n).count { ownerOf[it] == playerId }

    /** Nível defensivo do território. Sempre NENHUMA fora do modo tático. */
    fun fortificationOf(tId: Int): Fortification =
        if (!tactical) Fortification.NENHUMA else Fortification.forArmies(armiesOf[tId])

    /** Há trégua vigente entre estes dois exércitos? (carta Diplomacia) */
    fun hasTruce(a: Int, b: Int): Boolean {
        if (!tactical) return false
        if (a !in players.indices || b !in players.indices) return false
        return truceUntil[a][b] > turnNumber
    }

    fun totalArmiesOf(playerId: Int): Int =
        (0 until n).sumOf { if (ownerOf[it] == playerId) armiesOf[it] else 0 }

    fun ownsContinent(playerId: Int, continentId: Int): Boolean =
        MapData.continent(continentId).territoryIds.all { ownerOf[it] == playerId }

    fun fullyOwnedContinents(playerId: Int): List<Int> =
        MapData.continents.filter { ownsContinent(playerId, it.id) }.map { it.id }

    /** Territórios de onde o jogador atual pode atacar (>1 exército e vizinho inimigo). */
    fun canAttackFrom(tId: Int): Boolean {
        if (ownerOf[tId] != currentPlayerIndex || armiesOf[tId] < 2) return false
        return attackTargets(tId).isNotEmpty()
    }

    /** Vizinhos inimigos atacáveis (uma trégua vigente retira o alvo da lista). */
    fun attackTargets(from: Int): List<Int> =
        MapData.territory(from).neighbors.filter {
            ownerOf[it] != currentPlayerIndex && !hasTruce(currentPlayerIndex, ownerOf[it])
        }

    /** Alvos de deslocamento: territórios próprios conectados por caminho amigo. */
    fun fortifyTargets(from: Int): List<Int> {
        if (ownerOf[from] != currentPlayerIndex || armiesOf[from] < 2) return emptyList()
        val visited = HashSet<Int>()
        val queue = ArrayDeque<Int>()
        queue.add(from); visited.add(from)
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            for (nb in MapData.territory(cur).neighbors) {
                if (nb !in visited && ownerOf[nb] == currentPlayerIndex) {
                    visited.add(nb); queue.add(nb)
                }
            }
        }
        visited.remove(from)
        return visited.toList()
    }

    // ---------------------------------------------------------------------
    // TURNO / FASES
    // ---------------------------------------------------------------------

    private fun startTurn() {
        conqueredThisTurn = false
        fortifyUsed = false
        pendingAdvance = null
        phase = Phase.REFORCO
        conquestsThisTurn = 0
        extraFortifies = 0
        attackDiceBonus = 0
        seaRouteDiceBonus = 0
        ignoreFortification = false
        momentumApplied = if (tactical) momentumOf[currentPlayerIndex] else 0
        momentumOf[currentPlayerIndex] = 0
        reinforcements = computeBaseReinforcements(currentPlayerIndex) + momentumApplied
        // a sabotagem vale apenas para este reforço
        continentBonusBlocked[currentPlayerIndex] = false
    }

    fun computeBaseReinforcements(playerId: Int): Int {
        val terr = ownedCount(playerId)
        val base = maxOf(3, terr / 2)
        val bonus = if (tactical && continentBonusBlocked[playerId]) 0
        else fullyOwnedContinents(playerId).sumOf { MapData.continent(it).bonus }
        // vantagem concedida às CPUs conforme o nível de dificuldade escolhido
        val handicap =
            if (players[playerId].isHuman) 0 else difficulty.bonusReinforcements
        return base + bonus + handicap
    }

    /** Coloca [count] exércitos num território próprio durante o reforço. */
    fun reinforce(tId: Int, count: Int = 1): Boolean {
        if (phase != Phase.REFORCO) return false
        if (ownerOf[tId] != currentPlayerIndex) return false
        val c = count.coerceAtMost(reinforcements)
        if (c <= 0) return false
        armiesOf[tId] += c
        reinforcements -= c
        return true
    }

    fun canAdvanceFromReinforce(): Boolean = reinforcements == 0

    /** Avança para a próxima fase respeitando as regras. */
    fun advancePhase() {
        when (phase) {
            Phase.REFORCO -> if (reinforcements == 0) phase = Phase.ATAQUE
            Phase.ATAQUE -> { pendingAdvance = null; phase = Phase.DESLOCAMENTO }
            Phase.DESLOCAMENTO -> endTurn()
            Phase.FIM_DE_JOGO -> {}
        }
    }

    private fun endTurn() {
        // Direito a uma carta de território se conquistou ao menos um território.
        if (conqueredThisTurn) {
            drawCard()?.let { currentPlayer.cards.add(it) }
        }
        if (tactical) awardMomentum(currentPlayerIndex)
        checkMedals()
        turnNumber++

        // Próximo jogador não eliminado.
        var next = currentPlayerIndex
        do {
            next = (next + 1) % players.size
        } while (players[next].eliminated && next != currentPlayerIndex)
        val wrapped = next <= currentPlayerIndex
        currentPlayerIndex = next
        if (wrapped) closeRound()
        startTurn()
        checkVictory()
    }

    /**
     * Recompensa de Momentum do modo tático, medida pelos territórios tomados
     * no turno: 2 conquistas rendem uma carta tática; a partir de 3, reforço
     * extra no turno seguinte, limitado a +3.
     */
    private fun awardMomentum(playerId: Int) {
        if (conquestsThisTurn >= 2) {
            giveTactical(playerId)?.let {
                roundEvents.add("${players[playerId].name} recebeu a carta tática ${it.title}")
            }
        }
        val extra = (conquestsThisTurn - 2).coerceIn(0, MAX_MOMENTUM)
        momentumOf[playerId] = extra
        if (extra > 0) {
            roundEvents.add("Momentum +$extra para ${players[playerId].name}")
        }
    }

    /** Momentum guardado para o próximo turno deste exército. */
    fun pendingMomentum(playerId: Int): Int =
        if (!tactical || playerId !in players.indices) 0 else momentumOf[playerId]

    // ---------------------------------------------------------------------
    // COMBATE
    // ---------------------------------------------------------------------

    /**
     * Executa uma rodada de combate. Em caso de conquista, move [moveArmies]
     * (ou o mínimo de dados usados) para o território conquistado.
     */
    fun attack(from: Int, to: Int, moveArmies: Int? = null): com.sorte.war.model.BattleResult? {
        if (phase != Phase.ATAQUE) return null
        if (ownerOf[from] != currentPlayerIndex) return null
        if (ownerOf[to] == currentPlayerIndex) return null
        if (to !in MapData.territory(from).neighbors) return null
        if (armiesOf[from] < 2) return null
        val defenderId = ownerOf[to]
        if (hasTruce(currentPlayerIndex, defenderId)) return null
        pendingAdvance = null

        val attackDice = minOf(3, armiesOf[from] - 1)
        val defendDice = minOf(3, armiesOf[to])

        // Defesa: a fortificação do território soma +1 aos maiores dados.
        val fortificationUsed = if (ignoreFortification) 0 else fortificationOf(to).level
        // Ataque: bônus preparado por cartas táticas (Tanque, Caça, Navio).
        val seaRoute = MapData.territory(from).continentId != MapData.territory(to).continentId
        val attackBoost = attackDiceBonus + (if (seaRoute) seaRouteDiceBonus else 0)
        attackDiceBonus = 0
        seaRouteDiceBonus = 0
        ignoreFortification = false

        val aRolls = boostTopDice(List(attackDice) { rng.nextInt(6) + 1 }, attackBoost)
        val dRolls = boostTopDice(List(defendDice) { rng.nextInt(6) + 1 }, fortificationUsed)

        var aLoss = 0
        var dLoss = 0
        val comparisons = minOf(aRolls.size, dRolls.size)
        for (i in 0 until comparisons) {
            if (aRolls[i] > dRolls[i]) dLoss++ else aLoss++ // empate favorece o defensor
        }

        armiesOf[from] -= aLoss
        armiesOf[to] -= dLoss

        var conquered = false
        if (armiesOf[to] <= 0) {
            conquered = true
            ownerOf[to] = currentPlayerIndex
            val minMove = attackDice
            val maxMove = armiesOf[from] - 1
            val move = (moveArmies ?: minMove).coerceIn(minMove, maxMove)
            armiesOf[from] -= move
            armiesOf[to] = move
            conqueredThisTurn = true
            conquestsThisTurn++
            pendingAdvance = if (moveArmies == null && maxMove > minMove)
                AdvanceOption(from, to, minMove, maxMove) else null
            handlePossibleElimination(defenderId, currentPlayerIndex)
        }

        battleLog.add(
            BattleLogEntry(
                attackerId = currentPlayerIndex,
                defenderId = defenderId,
                fromTerritoryId = from,
                toTerritoryId = to,
                attackerLosses = aLoss,
                defenderLosses = dLoss,
                conquered = conquered,
                fortificationLevel = fortificationUsed,
                turnNumber = turnNumber
            )
        )

        val result = com.sorte.war.model.BattleResult(
            attackerTerritoryId = from,
            defenderTerritoryId = to,
            attackerDice = aRolls,
            defenderDice = dRolls,
            attackerLosses = aLoss,
            defenderLosses = dLoss,
            conquered = conquered,
            fortificationLevel = fortificationUsed
        )
        lastBattle = result
        checkVictory()
        return result
    }

    /** Soma +1 aos [count] maiores dados, sem nunca passar de 6. */
    private fun boostTopDice(rolls: List<Int>, count: Int): List<Int> {
        val sorted = rolls.sortedDescending()
        if (count <= 0) return sorted
        val boosted = sorted.toMutableList()
        for (i in 0 until minOf(count, boosted.size)) {
            boosted[i] = minOf(6, boosted[i] + 1)
        }
        return boosted.sortedDescending()
    }

    private fun handlePossibleElimination(defenderId: Int, killerId: Int) {
        if (defenderId < 0) return
        if (ownedCount(defenderId) == 0 && !players[defenderId].eliminated) {
            players[defenderId].eliminated = true
            eliminatedBy[defenderId] = killerId
            // O vencedor herda as cartas do eliminado (regra do War).
            players[killerId].cards.addAll(players[defenderId].cards)
            players[defenderId].cards.clear()
            if (tactical) {
                tacticalDiscard.addAll(players[defenderId].tacticalCards)
                players[defenderId].tacticalCards.clear()
            }
            roundEvents.add("${players[defenderId].name} foi eliminado por ${players[killerId].name}")
        }
    }

    /** Move tropas adicionais para o território recém-conquistado (uma vez). */
    fun advanceMore(extra: Int) {
        val opt = pendingAdvance ?: return
        val c = extra.coerceIn(0, armiesOf[opt.from] - 1)
        armiesOf[opt.from] -= c
        armiesOf[opt.to] += c
        pendingAdvance = null
    }

    fun clearPendingAdvance() {
        pendingAdvance = null
    }

    // ---------------------------------------------------------------------
    // DESLOCAMENTO
    // ---------------------------------------------------------------------

    /** Ainda pode deslocar? (Marcha Forçada libera movimentos adicionais.) */
    fun canFortifyNow(): Boolean = phase == Phase.DESLOCAMENTO && (!fortifyUsed || extraFortifies > 0)

    fun fortify(from: Int, to: Int, count: Int): Boolean {
        if (!canFortifyNow()) return false
        if (ownerOf[from] != currentPlayerIndex || ownerOf[to] != currentPlayerIndex) return false
        if (to !in fortifyTargets(from)) return false
        val c = count.coerceIn(1, armiesOf[from] - 1)
        if (c <= 0) return false
        armiesOf[from] -= c
        armiesOf[to] += c
        if (fortifyUsed) extraFortifies-- else fortifyUsed = true
        return true
    }

    // ---------------------------------------------------------------------
    // CARTAS
    // ---------------------------------------------------------------------

    fun isValidCardSet(cards: List<Card>): Boolean {
        if (cards.size != 3) return false
        val wilds = cards.count { it.symbol == CardSymbol.CORINGA }
        val nonWild = cards.filter { it.symbol != CardSymbol.CORINGA }.map { it.symbol }
        if (wilds >= 1) return true // um coringa completa qualquer trinca
        val distinct = nonWild.toSet()
        return distinct.size == 1 || distinct.size == 3
    }

    /** Bônus escalonado por conjunto trocado (padrão War). */
    fun nextTradeBonus(): Int = tradeBonusFor(setsTraded)

    private fun tradeBonusFor(setsAlready: Int): Int = when (setsAlready) {
        0 -> 4; 1 -> 6; 2 -> 8; 3 -> 10; 4 -> 12; 5 -> 15
        else -> 15 + (setsAlready - 5) * 5
    }

    /**
     * Troca um conjunto de 3 cartas por exércitos (somados ao reforço).
     * Bônus adicional de +2 se possuir um território mostrado numa das cartas.
     */
    fun tradeCards(cards: List<Card>): Int {
        if (phase != Phase.REFORCO) return 0
        if (!isValidCardSet(cards)) return 0
        if (!currentPlayer.cards.containsAll(cards)) return 0

        val bonus = tradeBonusFor(setsTraded)
        setsTraded++
        currentPlayer.cards.removeAll(cards)
        discardPile.addAll(cards)

        var territoryBonus = 0
        for (card in cards) {
            if (card.territoryId >= 0 && ownerOf[card.territoryId] == currentPlayerIndex) {
                armiesOf[card.territoryId] += 2
                territoryBonus += 2
            }
        }
        reinforcements += bonus
        return bonus + territoryBonus
    }

    fun mustTradeCards(): Boolean = currentPlayer.cards.size >= 5

    // ---------------------------------------------------------------------
    // CARTAS TÁTICAS (expansão — inertes no modo clássico)
    // ---------------------------------------------------------------------

    /** Territórios inimigos vizinhos de algum território seu, com tropas de sobra. */
    fun airStrikeTargets(): List<Int> = (0 until n).filter { t ->
        ownerOf[t] != currentPlayerIndex && armiesOf[t] > 1 &&
            !hasTruce(currentPlayerIndex, ownerOf[t]) &&
            MapData.territory(t).neighbors.any { ownerOf[it] == currentPlayerIndex }
    }

    /** Qualquer território inimigo com tropas de sobra. */
    fun bombardTargets(): List<Int> = (0 until n).filter { t ->
        ownerOf[t] != currentPlayerIndex && armiesOf[t] > 1 &&
            !hasTruce(currentPlayerIndex, ownerOf[t])
    }

    /** Territórios seus que podem ceder tropas. */
    fun troopSources(): List<Int> =
        territoriesOf(currentPlayerIndex).filter { armiesOf[it] > 1 }

    /** Destinos possíveis para a carta [card] saindo de [from]. */
    fun troopDestinations(card: TacticalCard, from: Int): List<Int> {
        if (ownerOf[from] != currentPlayerIndex || armiesOf[from] < 2) return emptyList()
        val mine = territoriesOf(currentPlayerIndex).filter { it != from }
        return if (card.target == TacticalTarget.PAR_PROPRIO_VIZINHO) {
            mine.filter { it in MapData.territory(from).neighbors }
        } else mine
    }

    fun rivals(): List<Int> = players.filter { !it.eliminated && it.id != currentPlayerIndex }.map { it.id }

    /**
     * Por que esta carta não pode ser usada agora? Devolve null quando pode.
     * O texto é mostrado ao jogador na tela de cartas.
     */
    fun tacticalBlockReason(card: TacticalCard): String? {
        if (!tactical) return "Disponível apenas no Modo Tático."
        if (winnerId != null) return "A campanha terminou."
        if (card !in currentPlayer.tacticalCards) return "Você não possui esta carta."
        if (phase !in card.phases) return "Só pode ser usada: ${card.timing.lowercase()}"
        return when (card.target) {
            TacticalTarget.NENHUM -> null
            TacticalTarget.TERRITORIO_PROPRIO ->
                if (territoriesOf(currentPlayerIndex).isEmpty()) "Você não controla territórios." else null
            TacticalTarget.INIMIGO_VIZINHO ->
                if (airStrikeTargets().isEmpty())
                    "Nenhum território inimigo vizinho com tropas suficientes." else null
            TacticalTarget.INIMIGO_QUALQUER ->
                if (bombardTargets().isEmpty())
                    "Nenhum território inimigo com tropas suficientes." else null
            TacticalTarget.PAR_PROPRIO_VIZINHO, TacticalTarget.PAR_PROPRIO_QUALQUER ->
                if (troopSources().none { troopDestinations(card, it).isNotEmpty() })
                    "Nenhum território seu tem tropas de sobra para mover." else null
            TacticalTarget.JOGADOR ->
                if (rivals().isEmpty()) "Nenhum adversário em jogo." else null
        }
    }

    fun canPlayTactical(card: TacticalCard): Boolean = tacticalBlockReason(card) == null

    /**
     * Executa uma carta tática do jogador da vez e devolve o relato do efeito
     * (null se a jogada for inválida — a carta continua na mão).
     */
    fun playTactical(
        card: TacticalCard,
        primary: Int? = null,
        secondary: Int? = null,
        amount: Int = 3,
        targetPlayer: Int? = null
    ): String? {
        if (!canPlayTactical(card)) return null
        val me = currentPlayerIndex

        val report: String = when (card) {
            TacticalCard.INFANTARIA -> {
                reinforcements += 2
                "Reforço de infantaria: +2 exércitos para distribuir."
            }

            TacticalCard.FORTIFICACAO -> {
                val t = primary ?: return null
                if (ownerOf[t] != me) return null
                armiesOf[t] += 3
                "+3 exércitos entrincheirados em ${MapData.territory(t).name}."
            }

            TacticalCard.PARAQUEDISTAS, TacticalCard.HELICOPTERO -> {
                val from = primary ?: return null
                val to = secondary ?: return null
                if (to !in troopDestinations(card, from)) return null
                val moved = amount.coerceIn(1, minOf(3, armiesOf[from] - 1))
                if (moved < 1) return null
                armiesOf[from] -= moved
                armiesOf[to] += moved
                "$moved exércitos de ${MapData.territory(from).name} chegaram a " +
                    "${MapData.territory(to).name}."
            }

            TacticalCard.TANQUE -> {
                attackDiceBonus = minOf(3, attackDiceBonus + 1)
                "Blindados prontos: +1 no seu maior dado no próximo ataque."
            }

            TacticalCard.CACA -> {
                attackDiceBonus = minOf(3, attackDiceBonus + 2)
                "Superioridade aérea: +1 nos seus dois maiores dados no próximo ataque."
            }

            TacticalCard.NAVIO_DE_GUERRA -> {
                seaRouteDiceBonus = minOf(3, seaRouteDiceBonus + 2)
                "Frota posicionada: +1 nos dois maiores dados no próximo ataque " +
                    "a outro continente."
            }

            TacticalCard.ARTILHARIA -> {
                ignoreFortification = true
                "Artilharia em posição: a fortificação do defensor não contará no " +
                    "próximo ataque."
            }

            TacticalCard.ATAQUE_AEREO, TacticalCard.BOMBARDEIO -> {
                val t = primary ?: return null
                val valid = if (card == TacticalCard.ATAQUE_AEREO) airStrikeTargets()
                else bombardTargets()
                if (t !in valid) return null
                val removed = minOf(2, armiesOf[t] - 1)
                if (removed <= 0) return null
                armiesOf[t] -= removed
                "$removed exércitos inimigos destruídos em ${MapData.territory(t).name}."
            }

            TacticalCard.ESPIONAGEM -> {
                val pid = targetPlayer ?: return null
                if (pid !in rivals()) return null
                val spy = players[pid]
                val forts = territoriesOf(pid).count { fortificationOf(it) != Fortification.NENHUMA }
                "${spy.name}: ${ownedCount(pid)} territórios, ${totalArmiesOf(pid)} exércitos, " +
                    "${spy.cards.size} cartas de território, ${spy.tacticalCards.size} táticas " +
                    "e $forts fortificações."
            }

            TacticalCard.SABOTAGEM -> {
                val pid = targetPlayer ?: return null
                if (pid !in rivals()) return null
                continentBonusBlocked[pid] = true
                "Linhas de suprimento de ${players[pid].name} sabotadas: sem bônus de " +
                    "continentes no próximo reforço."
            }

            TacticalCard.DIPLOMACIA -> {
                val pid = targetPlayer ?: return null
                if (pid !in rivals()) return null
                val until = turnNumber + players.size
                truceUntil[me][pid] = until
                truceUntil[pid][me] = until
                "Trégua firmada com ${players[pid].name}."
            }

            TacticalCard.MARCHA_FORCADA -> {
                extraFortifies++
                "Marcha forçada: você ganhou um deslocamento adicional neste turno."
            }
        }

        currentPlayer.tacticalCards.remove(card)
        tacticalDiscard.add(card)
        tacticalUsedThisRound[me]++
        roundEvents.add("${currentPlayer.name} usou ${card.title}")
        checkVictory()
        return report
    }

    // ---------------------------------------------------------------------
    // MEDALHAS, SNAPSHOT E RELATÓRIO DA RODADA
    // ---------------------------------------------------------------------

    private fun award(player: Player, medal: TacticalMedal) {
        if (player.medals.add(medal)) {
            roundEvents.add("${player.name} conquistou a medalha ${medal.title}")
        }
    }

    /** Medalhas são registro histórico: não dão nenhuma vantagem em jogo. */
    private fun checkMedals() {
        if (!tactical) return
        val alive = players.filter { !it.eliminated }
        if (alive.isEmpty()) return
        val leaderTerritories = alive.maxOf { ownedCount(it.id) }
        val topArmies = alive.maxOf { totalArmiesOf(it.id) }
        val soleArmyLeader = alive.count { totalArmiesOf(it.id) == topArmies } == 1
        val soleTerritoryLeader = alive.count { ownedCount(it.id) == leaderTerritories } == 1

        for (p in alive) {
            val owned = ownedCount(p.id)
            if (owned >= n) award(p, TacticalMedal.DOMINACAO_GLOBAL)
            if (fullyOwnedContinents(p.id).isNotEmpty()) award(p, TacticalMedal.SUPREMACIA_CONTINENTAL)
            if (owned >= 24) award(p, TacticalMedal.CONTROLE_DE_FRONTEIRAS)
            if (eliminatedBy.any { it == p.id }) award(p, TacticalMedal.EXTERMINIO)
            if (alive.size > 1 && soleArmyLeader && totalArmiesOf(p.id) == topArmies) {
                award(p, TacticalMedal.SUPERIORIDADE_MILITAR)
            }
            if (p.cards.size >= 5) award(p, TacticalMedal.COLECIONADOR_DE_CARTAS)
            if (leaderTerritories - owned >= 5) wasBehind[p.id] = true
            if (wasBehind[p.id] && soleTerritoryLeader && owned == leaderTerritories) {
                award(p, TacticalMedal.REVIRAVOLTA)
            }
        }
    }

    private fun snapshotOf(id: Int) = ArmySnapshot(
        playerId = id,
        territories = ownedCount(id),
        armies = totalArmiesOf(id),
        continents = fullyOwnedContinents(id).size,
        territoryCards = players[id].cards.size,
        tacticalCards = players[id].tacticalCards.size,
        bunkers = territoriesOf(id).count { fortificationOf(it) == Fortification.BUNKER },
        fortresses = territoriesOf(id).count { fortificationOf(it) == Fortification.FORTALEZA }
    )

    private fun takeSnapshot() {
        snapshot = RoundSnapshot(roundNumber, players.map { snapshotOf(it.id) })
    }

    /**
     * Fecha a rodada: compara a foto do início com a de agora e monta o
     * relatório a partir do log de combate — nada é estimado.
     */
    private fun closeRound() {
        val before = snapshot
        val after = players.map { snapshotOf(it.id) }
        if (before != null) {
            val stats = players.map { p ->
                val b = before.armies.getOrNull(p.id)
                val a = after[p.id]
                ArmyRoundStats(
                    playerId = p.id,
                    territoriesBefore = b?.territories ?: a.territories,
                    territoriesAfter = a.territories,
                    armiesBefore = b?.armies ?: a.armies,
                    armiesAfter = a.armies,
                    conquered = battleLog.count { it.conquered && it.attackerId == p.id },
                    lost = battleLog.count { it.conquered && it.defenderId == p.id },
                    casualtiesDealt = battleLog.sumOf {
                        (if (it.attackerId == p.id) it.defenderLosses else 0) +
                            (if (it.defenderId == p.id) it.attackerLosses else 0)
                    },
                    casualtiesTaken = battleLog.sumOf {
                        (if (it.attackerId == p.id) it.attackerLosses else 0) +
                            (if (it.defenderId == p.id) it.defenderLosses else 0)
                    },
                    bunkers = a.bunkers,
                    fortresses = a.fortresses,
                    tacticalUsed = tacticalUsedThisRound[p.id],
                    momentum = momentumOf[p.id]
                )
            }
            val events = mutableListOf<String>()
            events.addAll(roundEvents)
            players.forEach { p ->
                val b = before.armies.getOrNull(p.id) ?: return@forEach
                val a = after[p.id]
                if (a.continents > b.continents) events.add("${p.name} dominou um novo continente")
                if (a.continents < b.continents) events.add("${p.name} perdeu um continente")
                if (a.fortresses > b.fortresses) events.add("Nova fortaleza erguida por ${p.name}")
            }
            lastRoundReport = RoundReport(roundNumber, stats, events.distinct().take(12))
        }
        roundNumber++
        battleLog.clear()
        roundEvents.clear()
        for (i in tacticalUsedThisRound.indices) tacticalUsedThisRound[i] = 0
        takeSnapshot()
    }

    // ---------------------------------------------------------------------
    // VITÓRIA
    // ---------------------------------------------------------------------

    private fun checkVictory() {
        val alive = players.filter { !it.eliminated }
        if (alive.size == 1) {
            winnerId = alive[0].id
            phase = Phase.FIM_DE_JOGO
            return
        }
        for (p in alive) {
            if (isObjectiveComplete(p)) {
                winnerId = p.id
                phase = Phase.FIM_DE_JOGO
                return
            }
        }
    }

    // ---------------------------------------------------------------------
    // SALVAR / CARREGAR PARTIDA
    // ---------------------------------------------------------------------

    /** Serializa a partida inteira num texto (formato próprio, sem dependências). */
    fun toSave(): String {
        val sb = StringBuilder()
        fun line(k: String, v: String) { sb.append(k).append('=').append(v).append('\n') }

        line("v", SAVE_VERSION.toString())
        line("diff", difficulty.name)
        line("cur", currentPlayerIndex.toString())
        line("phase", phase.name)
        line("reinf", reinforcements.toString())
        line("winner", (winnerId ?: -1).toString())
        line("conq", if (conqueredThisTurn) "1" else "0")
        line("fort", if (fortifyUsed) "1" else "0")
        line("sets", setsTraded.toString())
        line("owner", ownerOf.joinToString(","))
        line("armies", armiesOf.joinToString(","))
        line("elimBy", eliminatedBy.joinToString(","))
        line("draw", drawPile.joinToString(";") { cardToText(it) })
        line("discard", discardPile.joinToString(";") { cardToText(it) })

        // --- estado da expansão tática (v4) ---
        line("mode", mode.name)
        line("turn", turnNumber.toString())
        line("round", roundNumber.toString())
        line("mom", momentumOf.joinToString(","))
        line("momA", momentumApplied.toString())
        line("conqT", conquestsThisTurn.toString())
        line("xfort", extraFortifies.toString())
        line("atkB", attackDiceBonus.toString())
        line("seaB", seaRouteDiceBonus.toString())
        line("ignF", if (ignoreFortification) "1" else "0")
        line("sabot", continentBonusBlocked.joinToString(",") { if (it) "1" else "0" })
        line("truce", truceUntil.joinToString(";") { row -> row.joinToString(",") })
        line("behind", wasBehind.joinToString(",") { if (it) "1" else "0" })
        line("tused", tacticalUsedThisRound.joinToString(","))
        line("tdraw", tacticalDraw.joinToString(",") { it.ordinal.toString() })
        line("tdisc", tacticalDiscard.joinToString(",") { it.ordinal.toString() })
        snapshot?.let { snap ->
            line("snapR", snap.roundNumber.toString())
            line(
                "snap",
                snap.armies.joinToString(";") { a ->
                    listOf(
                        a.playerId, a.territories, a.armies, a.continents,
                        a.territoryCards, a.tacticalCards, a.bunkers, a.fortresses
                    ).joinToString(",")
                }
            )
        }
        line(
            "blog",
            battleLog.takeLast(MAX_SAVED_LOG).joinToString(";") { b ->
                listOf(
                    b.attackerId, b.defenderId, b.fromTerritoryId, b.toTerritoryId,
                    b.attackerLosses, b.defenderLosses, if (b.conquered) 1 else 0,
                    b.fortificationLevel, b.turnNumber
                ).joinToString(",")
            }
        )

        line("np", players.size.toString())
        players.forEach { p ->
            val fields = listOf(
                p.id.toString(),
                p.name,
                p.colorArgb.toString(),
                if (p.isHuman) "1" else "0",
                if (p.eliminated) "1" else "0",
                p.cards.joinToString(";") { cardToText(it) },
                objectiveToText(p.objective),
                p.avatarId.toString(),
                p.tacticalCards.joinToString(";") { it.ordinal.toString() },
                p.medals.joinToString(";") { it.ordinal.toString() }
            )
            line("p", fields.joinToString(FS))
        }
        return sb.toString()
    }

    companion object {
        const val SAVE_VERSION = 4

        /** Versões de save que o jogo ainda restaura (v3 volta como clássica). */
        private val SUPPORTED_SAVES = intArrayOf(3, 4)

        /** Teto do reforço extra de Momentum. */
        const val MAX_MOMENTUM = 3

        /** Máximo de cartas táticas na mão. */
        const val TACTICAL_HAND_LIMIT = 4

        /** Combates guardados no save (o log é da rodada, não da partida). */
        private const val MAX_SAVED_LOG = 300

        private const val FS = "\u0001" // separador de campos
        private const val GS = "\u0002" // separador de grupos

        private fun cardToText(c: Card): String = "${c.symbol.ordinal}:${c.territoryId}"

        private fun cardFromText(s: String): Card? {
            val p = s.split(":")
            if (p.size != 2) return null
            val sym = CardSymbol.entries.getOrNull(p[0].toIntOrNull() ?: return null) ?: return null
            return Card(sym, p[1].toIntOrNull() ?: return null)
        }

        private fun tacticalFromText(s: String?, sep: String = ";"): List<TacticalCard> {
            if (s.isNullOrBlank()) return emptyList()
            return s.split(sep).mapNotNull { TacticalCard.byId(it.trim().toIntOrNull() ?: -1) }
        }

        private fun cardsFromText(s: String): MutableList<Card> =
            if (s.isBlank()) mutableListOf()
            else s.split(";").mapNotNull { cardFromText(it) }.toMutableList()

        private fun objectiveToText(o: Objective?): String = when (o) {
            null -> ""
            is Objective.ConquerContinents ->
                listOf("C", o.continentIds.joinToString("-"), o.extraAny.toString(), o.description)
                    .joinToString(GS)
            is Objective.ConquerTerritories ->
                listOf("T", o.count.toString(), o.minArmiesEach.toString(), o.description)
                    .joinToString(GS)
            is Objective.DestroyPlayer ->
                listOf("D", o.targetColorArgb.toString(), o.targetColorName, o.description)
                    .joinToString(GS)
        }

        private fun objectiveFromText(s: String): Objective? {
            if (s.isBlank()) return null
            val p = s.split(GS)
            if (p.size < 4) return null
            return when (p[0]) {
                "C" -> Objective.ConquerContinents(
                    continentIds = p[1].split("-").mapNotNull { it.toIntOrNull() },
                    extraAny = p[2].toIntOrNull() ?: 0,
                    desc = p[3]
                )
                "T" -> Objective.ConquerTerritories(
                    count = p[1].toIntOrNull() ?: 24,
                    minArmiesEach = p[2].toIntOrNull() ?: 1,
                    desc = p[3]
                )
                "D" -> Objective.DestroyPlayer(
                    targetColorArgb = p[1].toLongOrNull() ?: 0L,
                    targetColorName = p[2],
                    desc = p[3]
                )
                else -> null
            }
        }

        /** Recria a partida a partir do texto gerado por [toSave]; null se inválido. */
        fun fromSave(text: String): GameEngine? {
            try {
                val map = HashMap<String, MutableList<String>>()
                text.lineSequence().forEach { ln ->
                    val i = ln.indexOf('=')
                    if (i > 0) map.getOrPut(ln.substring(0, i)) { mutableListOf() }
                        .add(ln.substring(i + 1))
                }
                fun one(k: String): String? = map[k]?.firstOrNull()
                val version = one("v")?.toIntOrNull() ?: 0
                if (version !in SUPPORTED_SAVES) return null

                val rows = map["p"] ?: return null
                if (rows.isEmpty()) return null

                val parsed = rows.map { it.split(FS) }
                if (parsed.any { it.size < 8 }) return null

                val configs = parsed.map {
                    PlayerConfig(
                        name = it[1],
                        color = ArmyColor(it[1], it[2].toLong()),
                        isHuman = it[3] == "1",
                        avatarId = it[7].toIntOrNull() ?: 0
                    )
                }
                val diff = runCatching {
                    Difficulty.valueOf(one("diff") ?: Difficulty.VETERANO.name)
                }.getOrDefault(Difficulty.VETERANO)
                // Saves v3 não conheciam modos: voltam como partida clássica.
                val savedMode = runCatching {
                    GameMode.valueOf(one("mode") ?: GameMode.CLASSICO.name)
                }.getOrDefault(GameMode.CLASSICO)
                val e = GameEngine(configs, diff, skipSetup = true, mode = savedMode)

                parsed.forEachIndexed { idx, f ->
                    val p = e.players[idx]
                    p.eliminated = f[4] == "1"
                    p.cards.clear()
                    p.cards.addAll(cardsFromText(f[5]))
                    p.objective = objectiveFromText(f[6])
                    p.tacticalCards.clear()
                    p.tacticalCards.addAll(tacticalFromText(f.getOrNull(8)))
                    p.medals.clear()
                    f.getOrNull(9)?.split(";")?.forEach { m ->
                        TacticalMedal.byId(m.toIntOrNull() ?: -1)?.let { p.medals.add(it) }
                    }
                }

                fun ints(k: String): List<Int> =
                    one(k)?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()

                val owner = ints("owner")
                val armies = ints("armies")
                if (owner.size != e.ownerOf.size || armies.size != e.armiesOf.size) return null
                for (i in owner.indices) { e.ownerOf[i] = owner[i]; e.armiesOf[i] = armies[i] }

                val elim = ints("elimBy")
                for (i in elim.indices) if (i < e.eliminatedBy.size) e.eliminatedBy[i] = elim[i]

                e.currentPlayerIndex = one("cur")?.toIntOrNull() ?: 0
                e.phase = Phase.valueOf(one("phase") ?: Phase.REFORCO.name)
                e.reinforcements = one("reinf")?.toIntOrNull() ?: 0
                e.winnerId = one("winner")?.toIntOrNull()?.takeIf { it >= 0 }
                e.conqueredThisTurn = one("conq") == "1"
                e.fortifyUsed = one("fort") == "1"
                e.setsTraded = one("sets")?.toIntOrNull() ?: 0

                e.drawPile.clear()
                e.drawPile.addAll(cardsFromText(one("draw") ?: ""))
                e.discardPile.clear()
                e.discardPile.addAll(cardsFromText(one("discard") ?: ""))

                // --- expansão tática: ausente nos saves v3, entra zerada ---
                e.turnNumber = one("turn")?.toIntOrNull() ?: 0
                e.roundNumber = one("round")?.toIntOrNull() ?: 1
                e.momentumApplied = one("momA")?.toIntOrNull() ?: 0
                e.conquestsThisTurn = one("conqT")?.toIntOrNull() ?: 0
                e.extraFortifies = one("xfort")?.toIntOrNull() ?: 0
                e.attackDiceBonus = one("atkB")?.toIntOrNull() ?: 0
                e.seaRouteDiceBonus = one("seaB")?.toIntOrNull() ?: 0
                e.ignoreFortification = one("ignF") == "1"

                ints("mom").forEachIndexed { i, v ->
                    if (i < e.momentumOf.size) e.momentumOf[i] = v.coerceIn(0, MAX_MOMENTUM)
                }
                ints("tused").forEachIndexed { i, v ->
                    if (i < e.tacticalUsedThisRound.size) e.tacticalUsedThisRound[i] = v
                }
                one("sabot")?.split(",")?.forEachIndexed { i, v ->
                    if (i < e.continentBonusBlocked.size) e.continentBonusBlocked[i] = v == "1"
                }
                one("behind")?.split(",")?.forEachIndexed { i, v ->
                    if (i < e.wasBehind.size) e.wasBehind[i] = v == "1"
                }
                one("truce")?.split(";")?.forEachIndexed { i, row ->
                    if (i < e.truceUntil.size) {
                        row.split(",").forEachIndexed { j, v ->
                            if (j < e.truceUntil[i].size) {
                                e.truceUntil[i][j] = v.trim().toIntOrNull() ?: 0
                            }
                        }
                    }
                }

                e.tacticalDraw.clear()
                e.tacticalDraw.addAll(tacticalFromText(one("tdraw"), ","))
                e.tacticalDiscard.clear()
                e.tacticalDiscard.addAll(tacticalFromText(one("tdisc"), ","))

                e.battleLog.clear()
                one("blog")?.split(";")?.forEach { row ->
                    val f = row.split(",").mapNotNull { it.trim().toIntOrNull() }
                    if (f.size == 9) {
                        e.battleLog.add(
                            BattleLogEntry(
                                attackerId = f[0], defenderId = f[1],
                                fromTerritoryId = f[2], toTerritoryId = f[3],
                                attackerLosses = f[4], defenderLosses = f[5],
                                conquered = f[6] == 1, fortificationLevel = f[7],
                                turnNumber = f[8]
                            )
                        )
                    }
                }

                val snapRows = one("snap")?.split(";")?.mapNotNull { row ->
                    val f = row.split(",").mapNotNull { it.trim().toIntOrNull() }
                    if (f.size == 8) {
                        ArmySnapshot(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7])
                    } else null
                }
                e.snapshot = if (!snapRows.isNullOrEmpty()) {
                    RoundSnapshot(one("snapR")?.toIntOrNull() ?: e.roundNumber, snapRows)
                } else {
                    RoundSnapshot(e.roundNumber, e.players.map { e.snapshotOf(it.id) })
                }

                return e
            } catch (t: Throwable) {
                return null
            }
        }
    }

    fun isObjectiveComplete(player: Player): Boolean {
        val obj = player.objective ?: return false
        return when (obj) {
            is Objective.ConquerContinents -> {
                val fixedOk = obj.continentIds.all { ownsContinent(player.id, it) }
                if (!fixedOk) return false
                if (obj.extraAny <= 0) return true
                val extras = fullyOwnedContinents(player.id).count { it !in obj.continentIds }
                extras >= obj.extraAny
            }
            is Objective.ConquerTerritories -> {
                territoriesOf(player.id).count { armiesOf[it] >= obj.minArmiesEach } >= obj.count
            }
            is Objective.DestroyPlayer -> {
                val target = players.firstOrNull { it.colorArgb == obj.targetColorArgb }
                if (target == null) {
                    // cor não está em jogo -> objetivo alternativo (24 territórios)
                    ownedCount(player.id) >= 24
                } else if (target.eliminated) {
                    // válido só se foi este jogador quem eliminou; senão, 24 territórios
                    if (eliminatedBy[target.id] == player.id) true
                    else ownedCount(player.id) >= 24
                } else false
            }
        }
    }
}

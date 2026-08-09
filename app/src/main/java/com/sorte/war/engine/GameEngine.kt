package com.sorte.war.engine

import com.sorte.war.model.ArmyColor
import com.sorte.war.model.Card
import com.sorte.war.model.CardSymbol
import com.sorte.war.model.MapData
import com.sorte.war.model.Objective
import com.sorte.war.model.Objectives
import com.sorte.war.model.Phase
import com.sorte.war.model.Player
import com.sorte.war.model.PlayerPalette
import kotlin.random.Random

/**
 * Motor de regras do War. Mantém todo o estado da partida e expõe as ações
 * (reforçar, atacar, deslocar, trocar cartas, avançar de fase). A camada de UI
 * observa o estado por meio de um "tick" no ViewModel após cada mutação.
 */
class GameEngine(
    playerConfigs: List<PlayerConfig>,
    private val rng: Random = Random(System.nanoTime())
) {
    data class PlayerConfig(val name: String, val color: ArmyColor, val isHuman: Boolean)

    val players: List<Player> = playerConfigs.mapIndexed { i, c ->
        Player(id = i, name = c.name, colorArgb = c.color.argb, isHuman = c.isHuman)
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

    val currentPlayer: Player get() = players[currentPlayerIndex]

    init {
        setupBoard()
        assignObjectives()
        buildCardDeck()
        startTurn()
    }

    // ---------------------------------------------------------------------
    // SETUP
    // ---------------------------------------------------------------------

    private fun setupBoard() {
        val ids = (0 until n).toMutableList().also { it.shuffle(rng) }
        // Distribui territórios em rodízio entre os jogadores.
        ids.forEachIndexed { index, tId ->
            val p = index % players.size
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
            var obj = deck.removeAt(0)
            obj = sanitizeObjective(obj, p, colorsInPlay)
            p.objective = obj
        }
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
        val symbols = listOf(CardSymbol.INFANTARIA, CardSymbol.CAVALARIA, CardSymbol.CANHAO)
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

    // ---------------------------------------------------------------------
    // CONSULTAS
    // ---------------------------------------------------------------------

    fun territoriesOf(playerId: Int): List<Int> = (0 until n).filter { ownerOf[it] == playerId }
    fun ownedCount(playerId: Int): Int = (0 until n).count { ownerOf[it] == playerId }

    fun ownsContinent(playerId: Int, continentId: Int): Boolean =
        MapData.continent(continentId).territoryIds.all { ownerOf[it] == playerId }

    fun fullyOwnedContinents(playerId: Int): List<Int> =
        MapData.continents.filter { ownsContinent(playerId, it.id) }.map { it.id }

    /** Territórios de onde o jogador atual pode atacar (>1 exército e vizinho inimigo). */
    fun canAttackFrom(tId: Int): Boolean {
        if (ownerOf[tId] != currentPlayerIndex || armiesOf[tId] < 2) return false
        return MapData.territory(tId).neighbors.any { ownerOf[it] != currentPlayerIndex }
    }

    fun attackTargets(from: Int): List<Int> =
        MapData.territory(from).neighbors.filter { ownerOf[it] != currentPlayerIndex }

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
        reinforcements = computeBaseReinforcements(currentPlayerIndex)
    }

    fun computeBaseReinforcements(playerId: Int): Int {
        val terr = ownedCount(playerId)
        val base = maxOf(3, terr / 2)
        val bonus = fullyOwnedContinents(playerId).sumOf { MapData.continent(it).bonus }
        return base + bonus
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
        // Direito a uma carta se conquistou ao menos um território.
        if (conqueredThisTurn) {
            drawCard()?.let { currentPlayer.cards.add(it) }
        }
        // Próximo jogador não eliminado.
        var next = currentPlayerIndex
        do {
            next = (next + 1) % players.size
        } while (players[next].eliminated && next != currentPlayerIndex)
        currentPlayerIndex = next
        startTurn()
        checkVictory()
    }

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
        pendingAdvance = null

        val attackDice = minOf(3, armiesOf[from] - 1)
        val defendDice = minOf(3, armiesOf[to])

        val aRolls = List(attackDice) { rng.nextInt(6) + 1 }.sortedDescending()
        val dRolls = List(defendDice) { rng.nextInt(6) + 1 }.sortedDescending()

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
            val defenderId = ownerOf[to]
            ownerOf[to] = currentPlayerIndex
            val minMove = attackDice
            val maxMove = armiesOf[from] - 1
            val move = (moveArmies ?: minMove).coerceIn(minMove, maxMove)
            armiesOf[from] -= move
            armiesOf[to] = move
            conqueredThisTurn = true
            pendingAdvance = if (moveArmies == null && maxMove > minMove)
                AdvanceOption(from, to, minMove, maxMove) else null
            handlePossibleElimination(defenderId, currentPlayerIndex)
        }

        val result = com.sorte.war.model.BattleResult(
            attackerTerritoryId = from,
            defenderTerritoryId = to,
            attackerDice = aRolls,
            defenderDice = dRolls,
            attackerLosses = aLoss,
            defenderLosses = dLoss,
            conquered = conquered
        )
        lastBattle = result
        checkVictory()
        return result
    }

    private fun handlePossibleElimination(defenderId: Int, killerId: Int) {
        if (defenderId < 0) return
        if (ownedCount(defenderId) == 0 && !players[defenderId].eliminated) {
            players[defenderId].eliminated = true
            eliminatedBy[defenderId] = killerId
            // O vencedor herda as cartas do eliminado (regra do War).
            players[killerId].cards.addAll(players[defenderId].cards)
            players[defenderId].cards.clear()
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

    fun fortify(from: Int, to: Int, count: Int): Boolean {
        if (phase != Phase.DESLOCAMENTO || fortifyUsed) return false
        if (ownerOf[from] != currentPlayerIndex || ownerOf[to] != currentPlayerIndex) return false
        if (to !in fortifyTargets(from)) return false
        val c = count.coerceIn(1, armiesOf[from] - 1)
        if (c <= 0) return false
        armiesOf[from] -= c
        armiesOf[to] += c
        fortifyUsed = true
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

package com.sorte.war.engine

import com.sorte.war.model.Card
import com.sorte.war.model.Difficulty
import com.sorte.war.model.Fortification
import com.sorte.war.model.MapData
import com.sorte.war.model.Phase
import com.sorte.war.model.TacticalCard
import kotlin.random.Random

/**
 * IA dos oponentes. Joga um turno completo (reforço, ataque e deslocamento)
 * com uma estratégia simples porém competente: reforça as fronteiras mais
 * pressionadas, ataca quando tem vantagem numérica e desloca tropas do
 * interior para a linha de frente.
 *
 * No modo Tático ela ainda mede a **força efetiva** do defensor (tropas mais
 * o efeito da fortificação), procura subir os próprios territórios de nível e
 * usa cartas táticas — com uma frequência que depende da dificuldade.
 */
object Ai {

    private val rng = Random(System.nanoTime())

    fun playTurn(engine: GameEngine) {
        if (engine.phase == Phase.FIM_DE_JOGO) return
        doReinforce(engine)
        engine.advancePhase() // -> ATAQUE
        playAttackCards(engine)
        doAttacks(engine)
        if (engine.phase == Phase.FIM_DE_JOGO) return
        engine.advancePhase() // -> DESLOCAMENTO
        doFortify(engine)
        engine.advancePhase() // -> fim do turno
    }

    private fun me(engine: GameEngine) = engine.currentPlayerIndex

    private fun borders(engine: GameEngine): List<Int> {
        val p = me(engine)
        return engine.territoriesOf(p).filter { t ->
            MapData.territory(t).neighbors.any { engine.ownerOf[it] != p }
        }
    }

    private fun maxEnemyNeighborArmies(engine: GameEngine, t: Int): Int {
        val p = me(engine)
        return MapData.territory(t).neighbors
            .filter { engine.ownerOf[it] != p }
            .maxOfOrNull { engine.armiesOf[it] } ?: 0
    }

    /**
     * Força de defesa considerada pela IA: tropas mais o peso da fortificação.
     *
     * O bônus vale meio exército por dado (posto e bunker +1, fortaleza +2).
     * Somar o valor cheio deixava a IA cautelosa paralisada em partidas de dois
     * exércitos, onde os dois lados crescem no mesmo ritmo e a diferença exigida
     * nunca aparecia.
     */
    private fun effectiveDefense(engine: GameEngine, t: Int): Int =
        engine.armiesOf[t] + (engine.fortificationOf(t).defenseDice + 1) / 2

    /** Com que frequência a IA gasta cartas táticas, conforme a dificuldade. */
    private fun cardAppetite(difficulty: Difficulty): Float = when (difficulty) {
        Difficulty.RECRUTA -> 0.25f
        Difficulty.VETERANO -> 0.55f
        Difficulty.GENERAL -> 0.80f
        Difficulty.MARECHAL -> 1.00f
    }

    private fun wants(engine: GameEngine): Boolean =
        rng.nextFloat() < cardAppetite(engine.difficulty)

    // ---------------- Reforço ----------------

    private fun doReinforce(engine: GameEngine) {
        // Troca cartas enquanto for obrigatório ou houver conjunto válido.
        var guard = 0
        while (guard++ < 10) {
            val set = findValidSet(engine) ?: break
            if (engine.mustTradeCards() || engine.currentPlayer.cards.size >= 3) {
                engine.tradeCards(set)
            } else break
        }

        playReinforceCards(engine)

        val front = borders(engine)
        var guard2 = 0
        while (engine.reinforcements > 0 && guard2++ < 500) {
            if (front.isEmpty()) {
                // Sem fronteiras (raro): reforça o território mais forte.
                val any = engine.territoriesOf(me(engine)).maxByOrNull { engine.armiesOf[it] }
                if (any != null) engine.reinforce(any, engine.reinforcements) else break
                break
            }
            val target = pickReinforceTarget(engine, front)
            engine.reinforce(target, 1)
        }
    }

    /**
     * Escolhe onde colocar o próximo exército. No modo tático, um território a
     * um passo de virar Bunker ou Fortaleza ganha preferência: concentrar vale
     * mais do que espalhar.
     */
    private fun pickReinforceTarget(engine: GameEngine, front: List<Int>): Int {
        return front.maxByOrNull { t ->
            val pressure = maxEnemyNeighborArmies(engine, t) - engine.armiesOf[t]
            val upgrade = if (engine.tactical && nextLevelAt(engine.armiesOf[t]) == 1) 3 else 0
            pressure + upgrade + rng.nextInt(2)
        } ?: front.first()
    }

    /** Quantos exércitos faltam para o território subir de nível defensivo. */
    private fun nextLevelAt(armies: Int): Int = when {
        armies >= Fortification.FORTALEZA.minArmies -> Int.MAX_VALUE
        armies >= Fortification.BUNKER.minArmies -> Fortification.FORTALEZA.minArmies - armies
        armies >= Fortification.POSTO.minArmies -> Fortification.BUNKER.minArmies - armies
        else -> Fortification.POSTO.minArmies - armies
    }

    private fun findValidSet(engine: GameEngine): List<Card>? {
        val cards = engine.currentPlayer.cards
        if (cards.size < 3) return null
        val n = cards.size
        for (i in 0 until n) for (j in i + 1 until n) for (k in j + 1 until n) {
            val set = listOf(cards[i], cards[j], cards[k])
            if (engine.isValidCardSet(set)) return set
        }
        return null
    }

    // ---------------- Cartas táticas ----------------

    private fun hand(engine: GameEngine): List<TacticalCard> =
        engine.currentPlayer.tacticalCards.toList()

    /** Cartas jogadas na fase de reforço. */
    private fun playReinforceCards(engine: GameEngine) {
        if (!engine.tactical) return
        val p = me(engine)

        for (card in hand(engine)) {
            if (!engine.canPlayTactical(card)) continue
            if (!wants(engine)) continue
            when (card) {
                TacticalCard.INFANTARIA -> engine.playTactical(card)

                TacticalCard.FORTIFICACAO -> {
                    // entrincheira onde a pressão inimiga é maior
                    val target = borders(engine).maxByOrNull {
                        maxEnemyNeighborArmies(engine, it) - engine.armiesOf[it]
                    } ?: continue
                    engine.playTactical(card, primary = target)
                }

                TacticalCard.SABOTAGEM -> {
                    // sabota quem tem mais continentes para perder
                    val target = engine.rivals().maxByOrNull {
                        engine.fullyOwnedContinents(it).size * 10 + engine.ownedCount(it)
                    } ?: continue
                    if (engine.fullyOwnedContinents(target).isEmpty()) continue
                    engine.playTactical(card, targetPlayer = target)
                }

                TacticalCard.DIPLOMACIA -> {
                    // trégua só faz sentido quando a IA está em desvantagem
                    val mine = engine.ownedCount(p)
                    val threat = engine.rivals().maxByOrNull { engine.ownedCount(it) } ?: continue
                    if (engine.ownedCount(threat) <= mine + 3) continue
                    engine.playTactical(card, targetPlayer = threat)
                }

                TacticalCard.PARAQUEDISTAS -> {
                    // reposiciona tropas paradas no interior para a linha de frente
                    val from = engine.troopSources().maxByOrNull { t ->
                        if (MapData.territory(t).neighbors.all { engine.ownerOf[it] == p })
                            engine.armiesOf[t] else 0
                    } ?: continue
                    if (MapData.territory(from).neighbors.any { engine.ownerOf[it] != p }) continue
                    val to = engine.troopDestinations(card, from).maxByOrNull {
                        maxEnemyNeighborArmies(engine, it) - engine.armiesOf[it]
                    } ?: continue
                    engine.playTactical(card, primary = from, secondary = to)
                }

                else -> {}
            }
        }
    }

    /** Cartas jogadas antes de atacar. */
    private fun playAttackCards(engine: GameEngine) {
        if (!engine.tactical || engine.phase != Phase.ATAQUE) return

        for (card in hand(engine)) {
            if (!engine.canPlayTactical(card)) continue
            if (!wants(engine)) continue
            when (card) {
                TacticalCard.ATAQUE_AEREO, TacticalCard.BOMBARDEIO -> {
                    // enfraquece a fortificação inimiga mais dura da fronteira
                    val valid = if (card == TacticalCard.ATAQUE_AEREO) engine.airStrikeTargets()
                    else engine.bombardTargets()
                    val target = valid.maxByOrNull { effectiveDefense(engine, it) } ?: continue
                    engine.playTactical(card, primary = target)
                }

                TacticalCard.ARTILHARIA -> {
                    // só vale contra defensor fortificado
                    val hasFort = bestAttack(engine)?.let { (_, to) ->
                        engine.fortificationOf(to) != Fortification.NENHUMA
                    } ?: false
                    if (hasFort) engine.playTactical(card)
                }

                TacticalCard.TANQUE, TacticalCard.CACA -> {
                    // reforça o dado quando o combate está apertado
                    val move = bestAttack(engine) ?: continue
                    val margin = engine.armiesOf[move.first] - effectiveDefense(engine, move.second)
                    if (margin in 0..3) engine.playTactical(card)
                }

                TacticalCard.NAVIO_DE_GUERRA -> {
                    val move = bestAttack(engine) ?: continue
                    val sea = MapData.territory(move.first).continentId !=
                        MapData.territory(move.second).continentId
                    if (sea) engine.playTactical(card)
                }

                TacticalCard.HELICOPTERO -> {
                    // junta tropas de um vizinho tranquilo com a fronteira quente
                    val to = borders(engine).maxByOrNull {
                        maxEnemyNeighborArmies(engine, it) - engine.armiesOf[it]
                    } ?: continue
                    val from = MapData.territory(to).neighbors.firstOrNull { nb ->
                        engine.ownerOf[nb] == me(engine) && engine.armiesOf[nb] > 3 &&
                            to in engine.troopDestinations(card, nb)
                    } ?: continue
                    engine.playTactical(card, primary = from, secondary = to)
                }

                else -> {}
            }
        }
    }

    // ---------------- Ataque ----------------

    /** Melhor par (origem, alvo) segundo a força efetiva do defensor. */
    private fun bestAttack(engine: GameEngine): Pair<Int, Int>? {
        val p = me(engine)
        val minForce = engine.difficulty.minArmiesToAttack
        val minAdvantage = engine.difficulty.attackThreshold
        var best: Pair<Int, Int>? = null
        var bestAdvantage = Int.MIN_VALUE
        for (from in engine.territoriesOf(p)) {
            if (engine.armiesOf[from] < 2) continue
            for (to in engine.attackTargets(from)) {
                // no tático a fortificação entra na conta; no clássico ela é 0
                val advantage = engine.armiesOf[from] - effectiveDefense(engine, to)
                val rawAdvantage = engine.armiesOf[from] - engine.armiesOf[to]
                // Válvula: uma superioridade numérica gritante justifica o
                // ataque mesmo contra uma fortaleza. Sem isso, dois exércitos
                // cautelosos crescem em paralelo e a partida nunca termina.
                val worthIt = advantage >= minAdvantage || rawAdvantage >= minAdvantage + 3
                if (engine.armiesOf[from] >= minForce &&
                    worthIt &&
                    advantage > bestAdvantage
                ) {
                    bestAdvantage = advantage
                    best = from to to
                }
            }
        }
        return best
    }

    private fun doAttacks(engine: GameEngine) {
        val p = me(engine)
        var guard = 0
        while (guard++ < 200 && engine.phase == Phase.ATAQUE) {
            val move = bestAttack(engine) ?: break
            val (from, to) = move
            val other = MapData.territory(from).neighbors.any {
                it != to && engine.ownerOf[it] != p
            }
            // Se o destino conquistado ficará numa fronteira, empurra a maioria.
            val moveArmies = if (other) (engine.armiesOf[from] / 2) else (engine.armiesOf[from] - 1)
            engine.attack(from, to, moveArmies)
            if (engine.winnerId != null) return
        }
    }

    // ---------------- Deslocamento ----------------

    private fun doFortify(engine: GameEngine) {
        if (engine.phase != Phase.DESLOCAMENTO) return
        // Marcha Forçada rende um movimento a mais: vale a pena antes de deslocar.
        if (engine.tactical) {
            val march = hand(engine).firstOrNull { it == TacticalCard.MARCHA_FORCADA }
            if (march != null && engine.canPlayTactical(march) && wants(engine)) {
                engine.playTactical(march)
            }
        }

        var guard = 0
        while (engine.canFortifyNow() && guard++ < 4) {
            if (!moveOneStack(engine)) break
        }
    }

    private fun moveOneStack(engine: GameEngine): Boolean {
        val p = me(engine)
        // Território interior (sem inimigos vizinhos) com mais tropas sobrando.
        val interiors = engine.territoriesOf(p)
            .filter { t ->
                engine.armiesOf[t] > 1 &&
                    MapData.territory(t).neighbors.all { engine.ownerOf[it] == p }
            }
            .sortedByDescending { engine.armiesOf[it] }

        for (from in interiors) {
            val reachableBorders = engine.fortifyTargets(from).filter { t ->
                MapData.territory(t).neighbors.any { engine.ownerOf[it] != p }
            }
            val to = reachableBorders.maxByOrNull { maxEnemyNeighborArmies(engine, it) }
            if (to != null) {
                return engine.fortify(from, to, engine.armiesOf[from] - 1)
            }
        }
        return false
    }
}

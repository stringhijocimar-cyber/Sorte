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

        // Etapa de posicionamento inicial: só distribui as tropas e passa a vez.
        if (engine.placingInitialArmies) {
            placeInitialArmies(engine)
            engine.advancePhase()
            return
        }

        doReinforce(engine)
        engine.advancePhase() // -> ATAQUE
        playAttackCards(engine)
        doAttacks(engine)
        if (engine.phase == Phase.FIM_DE_JOGO) return
        engine.advancePhase() // -> DESLOCAMENTO
        doFortify(engine)
        engine.advancePhase() // -> fim do turno
    }

    /**
     * Posicionamento inicial da CPU: concentra nas fronteiras, com a mesma
     * imperfeição da dificuldade — o Recruta espalha mais, o Marechal
     * concentra onde importa.
     */
    private fun placeInitialArmies(engine: GameEngine) {
        val front = borders(engine).ifEmpty { engine.territoriesOf(me(engine)) }
        if (front.isEmpty()) return
        while (engine.reinforcements > 0) {
            if (!engine.reinforce(pickReinforceTarget(engine, front), 1)) break
        }
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
            val hand = engine.currentPlayer.cards.size
            if (engine.mustTradeCards() || hand >= engine.difficulty.tradeAtHandSize) {
                engine.tradeCards(set)
            } else break
        }

        playReinforceCards(engine)

        val front = borders(engine)
        // Distribui TODOS os reforços. O laço termina porque cada colocação
        // bem-sucedida gasta um exército, e uma colocação que falha sai fora.
        //
        // Antes havia um teto fixo de 500 voltas: numa partida longa, várias
        // trocas de cartas seguidas rendem mais de 500 exércitos num turno só,
        // sobrava reforço, o motor se recusava a sair da fase de reforço e o
        // turno da CPU travava para sempre.
        while (engine.reinforcements > 0) {
            if (front.isEmpty()) {
                // Sem fronteiras (raro): reforça o território mais forte.
                val any = engine.territoriesOf(me(engine)).maxByOrNull { engine.armiesOf[it] }
                if (any != null) engine.reinforce(any, engine.reinforcements)
                break
            }
            val target = pickReinforceTarget(engine, front)
            if (!engine.reinforce(target, 1)) break
        }
    }

    /**
     * Escolhe onde colocar o próximo exército. No modo tático, um território a
     * um passo de virar Bunker ou Fortaleza ganha preferência: concentrar vale
     * mais do que espalhar.
     */
    private fun pickReinforceTarget(engine: GameEngine, front: List<Int>): Int {
        // Parte dos reforços vai para a melhor posição; o resto é espalhado.
        // É esse desperdício controlado que separa o Recruta do Marechal.
        if (rng.nextFloat() > engine.difficulty.reinforcementEfficiency) {
            return front[rng.nextInt(front.size)]
        }
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

    /**
     * Quanto a cautela já afrouxou. Zero nas primeiras 10 rodadas — o começo
     * precisa continuar manso para quem está aprendendo — e cresce depois,
     * até deixar qualquer nível decisivo o bastante para encerrar a partida.
     */
    private fun relaxation(engine: GameEngine): Int =
        ((engine.roundNumber - 10) / 8).coerceIn(0, 4)

    /** Ataques viáveis, do mais vantajoso para o menos. */
    private fun rankedAttacks(engine: GameEngine): List<Pair<Int, Int>> {
        val p = me(engine)
        // A cautela afrouxa devagar com o passar das rodadas. Sem isso, uma
        // mesa de Recrutas fica tão passiva que a partida não termina: a
        // medição mostrou mediana de 416 turnos e 1 em 40 partidas sem fim.
        // O começo continua manso, que é o que importa para quem está
        // aprendendo; o fim de jogo deixa de arrastar.
        val relax = relaxation(engine)
        val minForce = (engine.difficulty.minArmiesToAttack - relax).coerceAtLeast(2)
        val minAdvantage = (engine.difficulty.attackThreshold - relax).coerceAtLeast(0)
        val options = mutableListOf<Triple<Int, Int, Int>>()
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
                if (engine.armiesOf[from] >= minForce && worthIt) {
                    options.add(Triple(from, to, advantage))
                }
            }
        }
        return options.sortedByDescending { it.third }.map { it.first to it.second }
    }

    /**
     * Próximo ataque. Níveis mais baixos sorteiam entre os melhores em vez de
     * jogar sempre a melhor jogada matemática.
     */
    private fun bestAttack(engine: GameEngine): Pair<Int, Int>? {
        val ranked = rankedAttacks(engine)
        if (ranked.isEmpty()) return null
        val pool = minOf(engine.difficulty.candidateAttacks, ranked.size)
        return ranked[rng.nextInt(pool)]
    }

    private fun doAttacks(engine: GameEngine) {
        val p = me(engine)
        val maxRounds = engine.difficulty.maxAttackRounds + relaxation(engine) * 2
        var rounds = 0
        while (rounds++ < maxRounds && engine.phase == Phase.ATAQUE) {
            val move = bestAttack(engine) ?: break
            val (from, to) = move
            val other = MapData.territory(from).neighbors.any {
                it != to && engine.ownerOf[it] != p
            }
            // Se o destino conquistado ficará numa fronteira, empurra a maioria.
            val moveArmies = if (other) (engine.armiesOf[from] / 2) else (engine.armiesOf[from] - 1)
            val before = engine.conquestsThisTurn
            engine.attack(from, to, moveArmies)
            if (engine.winnerId != null) return

            // Conquistou: decide se continua a campanha ou encerra por aqui.
            if (engine.conquestsThisTurn > before) {
                val keepGoing = engine.difficulty.continueChance(engine.conquestsThisTurn)
                if (rng.nextFloat() > keepGoing) return
            }
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
                val available = engine.armiesOf[from] - 1
                val moved = maxOf(1, (available * engine.difficulty.fortifyEfficiency).toInt())
                return engine.fortify(from, to, moved)
            }
        }
        return false
    }
}

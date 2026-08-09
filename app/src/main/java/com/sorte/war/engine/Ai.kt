package com.sorte.war.engine

import com.sorte.war.model.Card
import com.sorte.war.model.MapData
import com.sorte.war.model.Phase
import kotlin.random.Random

/**
 * IA dos oponentes. Joga um turno completo (reforço, ataque e deslocamento)
 * com uma estratégia simples porém competente: reforça as fronteiras mais
 * pressionadas, ataca quando tem vantagem numérica e desloca tropas do
 * interior para a linha de frente.
 */
object Ai {

    private val rng = Random(System.nanoTime())

    fun playTurn(engine: GameEngine) {
        if (engine.phase == Phase.FIM_DE_JOGO) return
        doReinforce(engine)
        engine.advancePhase() // -> ATAQUE
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

        val front = borders(engine)
        var guard2 = 0
        while (engine.reinforcements > 0 && guard2++ < 500) {
            if (front.isEmpty()) {
                // Sem fronteiras (raro): reforça o território mais forte.
                val any = engine.territoriesOf(me(engine)).maxByOrNull { engine.armiesOf[it] }
                if (any != null) engine.reinforce(any, engine.reinforcements) else break
                break
            }
            // Reforça a fronteira mais pressionada (maior défice frente ao inimigo).
            val target = front.maxByOrNull {
                maxEnemyNeighborArmies(engine, it) - engine.armiesOf[it] + rng.nextInt(2)
            } ?: front.first()
            engine.reinforce(target, 1)
        }
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

    // ---------------- Ataque ----------------

    private fun doAttacks(engine: GameEngine) {
        val p = me(engine)
        var guard = 0
        while (guard++ < 200 && engine.phase == Phase.ATAQUE) {
            val minForce = engine.difficulty.minArmiesToAttack
            val minAdvantage = engine.difficulty.attackThreshold
            var best: Pair<Int, Int>? = null
            var bestAdvantage = Int.MIN_VALUE
            for (from in engine.territoriesOf(p)) {
                if (engine.armiesOf[from] < 2) continue
                for (to in engine.attackTargets(from)) {
                    val advantage = engine.armiesOf[from] - engine.armiesOf[to]
                    // A agressividade depende do nível de dificuldade escolhido.
                    if (engine.armiesOf[from] >= minForce &&
                        advantage >= minAdvantage &&
                        advantage > bestAdvantage
                    ) {
                        bestAdvantage = advantage
                        best = from to to
                    }
                }
            }
            val move = best ?: break
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
                engine.fortify(from, to, engine.armiesOf[from] - 1)
                return
            }
        }
    }
}

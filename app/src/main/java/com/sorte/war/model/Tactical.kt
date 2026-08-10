package com.sorte.war.model

/** Modos de jogo. O clássico mantém exatamente as regras do tabuleiro. */
enum class GameMode(val label: String, val tagline: String) {
    CLASSICO(
        "Clássico",
        "Experiência tradicional de conquista territorial."
    ),
    TATICO(
        "Tático",
        "Fortificações, cartas táticas, momentum e inteligência ampliada."
    )
}

/**
 * Nível defensivo de um território, calculado automaticamente pela quantidade
 * de tropas. Só existe no modo Tático.
 *
 * O bônus é abstrato: soma +1 aos [defenseDice] maiores dados de defesa, sem
 * nunca ultrapassar 6.
 */
enum class Fortification(
    val level: Int,
    val title: String,
    val minArmies: Int,
    val defenseDice: Int
) {
    NENHUMA(0, "Sem fortificação", 0, 0),
    POSTO(1, "Posto Avançado", 5, 1),
    BUNKER(2, "Bunker", 8, 2),
    FORTALEZA(3, "Fortaleza", 12, 3);

    val badge: String
        get() = when (this) {
            NENHUMA -> ""
            POSTO -> "$title — Nível 1"
            BUNKER -> "$title — Nível 2"
            FORTALEZA -> "$title — Nível 3"
        }

    companion object {
        fun forArmies(armies: Int): Fortification = when {
            armies >= FORTALEZA.minArmies -> FORTALEZA
            armies >= BUNKER.minArmies -> BUNKER
            armies >= POSTO.minArmies -> POSTO
            else -> NENHUMA
        }
    }
}

/** O que a carta tática precisa que o jogador aponte para produzir efeito. */
enum class TacticalTarget {
    /** Nenhum alvo: o efeito é imediato ou prepara o próximo ataque. */
    NENHUM,

    /** Um território do próprio jogador. */
    TERRITORIO_PROPRIO,

    /** Um território inimigo vizinho de algum território seu. */
    INIMIGO_VIZINHO,

    /** Qualquer território inimigo do mapa. */
    INIMIGO_QUALQUER,

    /** Dois territórios seus vizinhos (origem e destino). */
    PAR_PROPRIO_VIZINHO,

    /** Dois territórios seus quaisquer (origem e destino). */
    PAR_PROPRIO_QUALQUER,

    /** Um exército adversário. */
    JOGADOR
}

/**
 * O baralho tático — a expansão. É um baralho separado: não substitui nem
 * mistura com as cartas de território do tabuleiro.
 *
 * [rule] é a regra que vale no jogo e é o texto exibido na carta. [copies]
 * define quantas cópias entram no baralho, que é como o equilíbrio é feito:
 * efeitos mais fortes são mais raros.
 */
enum class TacticalCard(
    /** Número impresso na carta ilustrada correspondente (1 a 14). */
    val number: Int,
    val title: String,
    val phases: Set<Phase>,
    val target: TacticalTarget,
    val rule: String,
    val timing: String,
    val copies: Int
) {
    INFANTARIA(
        1, "Infantaria",
        setOf(Phase.REFORCO), TacticalTarget.NENHUM,
        "Recruta +2 exércitos para distribuir agora.",
        "Na fase de reforço.", 6
    ),
    PARAQUEDISTAS(
        2, "Paraquedistas",
        setOf(Phase.REFORCO, Phase.DESLOCAMENTO), TacticalTarget.PAR_PROPRIO_QUALQUER,
        "Salta até 3 exércitos para qualquer território seu, mesmo sem ligação por terra.",
        "No reforço ou no deslocamento.", 3
    ),
    TANQUE(
        3, "Tanque",
        setOf(Phase.ATAQUE), TacticalTarget.NENHUM,
        "No próximo ataque, +1 no seu maior dado (nunca passa de 6).",
        "Na fase de ataque, antes de atacar.", 4
    ),
    ARTILHARIA(
        4, "Artilharia",
        setOf(Phase.ATAQUE), TacticalTarget.NENHUM,
        "No próximo ataque, a fortificação do defensor não conta.",
        "Na fase de ataque, antes de atacar.", 4
    ),
    HELICOPTERO(
        5, "Helicóptero",
        setOf(Phase.ATAQUE, Phase.DESLOCAMENTO), TacticalTarget.PAR_PROPRIO_VIZINHO,
        "Transporta até 3 exércitos entre dois territórios seus vizinhos, na hora.",
        "No ataque ou no deslocamento.", 3
    ),
    CACA(
        6, "Caça",
        setOf(Phase.ATAQUE), TacticalTarget.NENHUM,
        "No próximo ataque, +1 nos seus dois maiores dados (nunca passa de 6).",
        "Na fase de ataque, antes de atacar.", 3
    ),
    NAVIO_DE_GUERRA(
        7, "Navio de Guerra",
        setOf(Phase.ATAQUE), TacticalTarget.NENHUM,
        "No próximo ataque por rota marítima (alvo em outro continente), " +
            "+1 nos seus dois maiores dados.",
        "Na fase de ataque, antes de atacar.", 2
    ),
    ATAQUE_AEREO(
        8, "Ataque Aéreo",
        setOf(Phase.ATAQUE), TacticalTarget.INIMIGO_VIZINHO,
        "Elimina até 2 exércitos de um território inimigo vizinho. Sempre resta 1 defensor.",
        "Na fase de ataque.", 3
    ),
    BOMBARDEIO(
        9, "Bombardeio",
        setOf(Phase.ATAQUE), TacticalTarget.INIMIGO_QUALQUER,
        "Elimina até 2 exércitos de qualquer território inimigo do mapa. " +
            "Sempre resta 1 defensor.",
        "Na fase de ataque.", 1
    ),
    ESPIONAGEM(
        10, "Espionagem",
        setOf(Phase.REFORCO, Phase.ATAQUE, Phase.DESLOCAMENTO), TacticalTarget.JOGADOR,
        "Revela as cartas, os exércitos e as fortificações de um adversário.",
        "A qualquer momento do seu turno.", 3
    ),
    SABOTAGEM(
        11, "Sabotagem",
        setOf(Phase.REFORCO), TacticalTarget.JOGADOR,
        "O adversário escolhido fica sem bônus de continentes no próximo reforço dele.",
        "Na fase de reforço.", 2
    ),
    MARCHA_FORCADA(
        12, "Marcha Forçada",
        setOf(Phase.REFORCO, Phase.ATAQUE, Phase.DESLOCAMENTO), TacticalTarget.NENHUM,
        "Libera um deslocamento adicional neste turno.",
        "A qualquer momento do seu turno.", 3
    ),
    FORTIFICACAO(
        13, "Fortificação",
        setOf(Phase.REFORCO), TacticalTarget.TERRITORIO_PROPRIO,
        "Entrincheira +3 exércitos num território seu.",
        "Na fase de reforço.", 4
    ),
    DIPLOMACIA(
        14, "Diplomacia",
        setOf(Phase.REFORCO), TacticalTarget.JOGADOR,
        "Trégua: você e o adversário escolhido não podem se atacar até o fim do " +
            "próximo turno dele.",
        "Na fase de reforço.", 2
    );

    companion object {
        val all: List<TacticalCard> get() = entries

        fun byId(id: Int): TacticalCard? = entries.getOrNull(id)

        /** Baralho completo, já com as repetições de cada carta. */
        fun buildDeck(): MutableList<TacticalCard> {
            val deck = mutableListOf<TacticalCard>()
            entries.forEach { card -> repeat(card.copies) { deck.add(card) } }
            return deck
        }
    }
}

/**
 * As cartas 15 a 21 do baralho ilustrado.
 *
 * Toda vez que um exército cumpre uma dessas condições, o feito fica
 * registrado como medalha e aparece no relatório. Além disso, no Modo Tático
 * cada exército recebe uma delas como **missão de campanha**: cumprir a sua
 * rende uma recompensa única — nunca a vitória, que continua sendo decidida
 * apenas pelo objetivo secreto ou pela eliminação dos adversários.
 *
 * As recompensas são deliberadamente modestas: aceleram quem está indo bem
 * sem encerrar a partida sozinhas.
 */
enum class TacticalMedal(
    /** Número impresso na carta ilustrada correspondente (15 a 21). */
    val number: Int,
    val title: String,
    val requirement: String,
    /** Exércitos somados ao próximo reforço de quem cumpre a missão. */
    val rewardArmies: Int = 0,
    /** Cartas táticas entregues na hora. */
    val rewardCards: Int = 0,
    /** Bônus somado à próxima troca de cartas de território. */
    val rewardTradeBonus: Int = 0,
    /** Pode ser sorteada como missão de campanha? */
    val assignable: Boolean = true
) {
    DOMINACAO_GLOBAL(
        15, "Dominação Global", "Controlar os 42 territórios.",
        assignable = false // conquistar o mapa inteiro já encerra a partida
    ),
    SUPREMACIA_CONTINENTAL(
        16, "Supremacia Continental", "Controlar um continente inteiro.",
        rewardCards = 2
    ),
    CONTROLE_DE_FRONTEIRAS(
        17, "Controle de Fronteiras",
        "Controlar 14 territórios que fazem fronteira com o inimigo.",
        rewardArmies = 3
    ),
    EXTERMINIO(
        18, "Extermínio", "Eliminar um adversário da partida.",
        rewardArmies = 3, rewardCards = 1
    ),
    SUPERIORIDADE_MILITAR(
        19, "Superioridade Militar", "Ter o maior exército do mapa, com folga de 25% sobre o segundo.",
        rewardArmies = 2
    ),
    COLECIONADOR_DE_CARTAS(
        20, "Colecionador de Cartas", "Chegar a 5 cartas de território na mão.",
        rewardTradeBonus = 4
    ),
    REVIRAVOLTA(
        21, "Reviravolta", "Assumir a liderança depois de estar 8 territórios atrás.",
        rewardArmies = 3, rewardCards = 1
    );

    /** Descrição curta da recompensa, para a interface. */
    val rewardText: String
        get() = buildList {
            if (rewardArmies > 0) add("+$rewardArmies exércitos no próximo reforço")
            if (rewardCards > 0) {
                add(if (rewardCards == 1) "1 carta tática" else "$rewardCards cartas táticas")
            }
            if (rewardTradeBonus > 0) add("+$rewardTradeBonus na próxima troca de cartas")
        }.joinToString(" e ").ifEmpty { "Registro de honra, sem recompensa." }

    companion object {
        val all: List<TacticalMedal> get() = entries

        /** Medalhas que podem virar missão de campanha. */
        val missions: List<TacticalMedal> get() = entries.filter { it.assignable }

        fun byId(id: Int): TacticalMedal? = entries.getOrNull(id)
    }
}

/** Uma rodada de combate registrada, base do relatório da rodada. */
data class BattleLogEntry(
    val attackerId: Int,
    val defenderId: Int,
    val fromTerritoryId: Int,
    val toTerritoryId: Int,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val conquered: Boolean,
    val fortificationLevel: Int,
    val turnNumber: Int
)

/** Estado de um exército no início da rodada, para comparar no fim. */
data class ArmySnapshot(
    val playerId: Int,
    val territories: Int,
    val armies: Int,
    val continents: Int,
    val territoryCards: Int,
    val tacticalCards: Int,
    val bunkers: Int,
    val fortresses: Int
)

/** Foto do tabuleiro no início de uma rodada completa. */
data class RoundSnapshot(
    val roundNumber: Int,
    val armies: List<ArmySnapshot>
)

/** Linha do relatório referente a um exército. */
data class ArmyRoundStats(
    val playerId: Int,
    val territoriesBefore: Int,
    val territoriesAfter: Int,
    val armiesBefore: Int,
    val armiesAfter: Int,
    val conquered: Int,
    val lost: Int,
    val casualtiesDealt: Int,
    val casualtiesTaken: Int,
    val bunkers: Int,
    val fortresses: Int,
    val tacticalUsed: Int,
    val momentum: Int
) {
    val territoryDelta: Int get() = territoriesAfter - territoriesBefore
    val armyDelta: Int get() = armiesAfter - armiesBefore
}

/** Relatório completo de uma rodada, montado pelo motor. */
data class RoundReport(
    val roundNumber: Int,
    val stats: List<ArmyRoundStats>,
    val events: List<String>
) {
    private fun best(selector: (ArmyRoundStats) -> Int): ArmyRoundStats? =
        stats.filter { selector(it) > 0 }.maxByOrNull(selector)

    val topConqueror: ArmyRoundStats? get() = best { it.conquered }
    val topLoser: ArmyRoundStats? get() = best { it.lost }
    val topArmy: ArmyRoundStats? get() = stats.maxByOrNull { it.armiesAfter }
    val topKiller: ArmyRoundStats? get() = best { it.casualtiesDealt }
    val topVictim: ArmyRoundStats? get() = best { it.casualtiesTaken }
    val topAdvance: ArmyRoundStats? get() = best { it.territoryDelta }
}

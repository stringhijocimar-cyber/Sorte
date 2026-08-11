package com.sorte.war.model

/** Como os territórios e a ordem de jogo são definidos no início da partida. */
enum class SetupMode(val label: String, val description: String) {
    DADOS(
        "Sorteio nos dados",
        "Cada exército rola um dado; quem tirar o maior começa a partida."
    ),
    ALEATORIA(
        "Totalmente aleatório",
        "Territórios e ordem de jogo sorteados sem rolagem de dados."
    )
}

/**
 * Nível de dificuldade dos oponentes controlados pela CPU.
 *
 * A diferença entre os níveis está na **qualidade das decisões** da IA, não
 * nos dados: o combate é exatamente o mesmo para humano e CPU em todos os
 * níveis. Toda a calibração fica aqui, para não haver número solto no Ai.kt.
 */
enum class Difficulty(
    val label: String,
    val description: String,
    /** Vantagem numérica mínima que a IA exige para atacar (menor = mais agressiva). */
    val attackThreshold: Int,
    /** Força mínima do território para a IA considerar atacar dali. */
    val minArmiesToAttack: Int,
    /** Exércitos extras que cada CPU recebe por turno. */
    val bonusReinforcements: Int,
    /** Teto de rodadas de combate que a IA trava por turno. */
    val maxAttackRounds: Int,
    /** Entre quantos dos melhores ataques ela sorteia o próximo alvo. */
    val candidateAttacks: Int,
    /** Chance de continuar a ofensiva depois de conquistar um território. */
    val continueAfterConquest: Float,
    /** Fração dos reforços que vai para a melhor posição (o resto é espalhado). */
    val reinforcementEfficiency: Float,
    /** Fração das tropas disponíveis que ela realmente desloca. */
    val fortifyEfficiency: Float,
    /** A partir de quantas cartas na mão ela troca por iniciativa própria. */
    val tradeAtHandSize: Int
) {
    RECRUTA(
        "Recruta", "IA mais cautelosa, ideal para aprender.",
        attackThreshold = 4, minArmiesToAttack = 6, bonusReinforcements = 0,
        maxAttackRounds = 7, candidateAttacks = 3, continueAfterConquest = 0.50f,
        reinforcementEfficiency = 0.60f, fortifyEfficiency = 0.55f, tradeAtHandSize = 4
    ),
    VETERANO(
        "Veterano", "IA equilibrada.",
        attackThreshold = 2, minArmiesToAttack = 4, bonusReinforcements = 0,
        maxAttackRounds = 18, candidateAttacks = 2, continueAfterConquest = 0.80f,
        reinforcementEfficiency = 0.85f, fortifyEfficiency = 0.75f, tradeAtHandSize = 3
    ),
    GENERAL(
        "General", "IA agressiva, com +1 reforço.",
        attackThreshold = 1, minArmiesToAttack = 3, bonusReinforcements = 1,
        maxAttackRounds = 50, candidateAttacks = 1, continueAfterConquest = 0.95f,
        reinforcementEfficiency = 1.0f, fortifyEfficiency = 1.0f, tradeAtHandSize = 3
    ),
    MARECHAL(
        "Marechal", "IA de elite, com +3 reforços.",
        attackThreshold = 0, minArmiesToAttack = 2, bonusReinforcements = 3,
        maxAttackRounds = 200, candidateAttacks = 1, continueAfterConquest = 1.0f,
        reinforcementEfficiency = 1.0f, fortifyEfficiency = 1.0f, tradeAtHandSize = 3
    );

    /**
     * Freio anti-bola-de-neve: uma sequência de dados boa não pode fazer uma
     * CPU iniciante atravessar um continente inteiro num turno só.
     */
    fun continueChance(conquestsThisTurn: Int): Float = when {
        this == RECRUTA && conquestsThisTurn >= 2 -> 0.15f
        this == VETERANO && conquestsThisTurn >= 4 -> continueAfterConquest * 0.5f
        else -> continueAfterConquest
    }

    companion object {
        fun byId(id: Int): Difficulty = entries.getOrElse(id) { VETERANO }
    }
}

/**
 * Hierarquia militar do jogador, conquistada com vitórias — como as patentes
 * do Exército. Cada patente exige um número acumulado de vitórias.
 */
enum class Rank(val title: String, val victoriesNeeded: Int, val insignia: String) {
    RECRUTA("Recruta", 0, "○"),
    SOLDADO("Soldado", 1, "▎"),
    CABO("Cabo", 2, "▎▎"),
    SARGENTO("Sargento", 4, "▎▎▎"),
    SUBTENENTE("Subtenente", 7, "◆"),
    TENENTE("Tenente", 11, "◆◆"),
    CAPITAO("Capitão", 16, "◆◆◆"),
    MAJOR("Major", 22, "★"),
    CORONEL("Coronel", 30, "★★"),
    GENERAL("General", 40, "★★★"),
    MARECHAL("Marechal", 55, "★★★★");

    companion object {
        /** Patente correspondente a um total de vitórias. */
        fun forVictories(v: Int): Rank = entries.last { v >= it.victoriesNeeded }

        /** Próxima patente, ou null se já estiver no topo. */
        fun next(current: Rank): Rank? =
            entries.firstOrNull { it.victoriesNeeded > current.victoriesNeeded }
    }
}

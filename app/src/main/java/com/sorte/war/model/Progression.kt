package com.sorte.war.model

/** Nível de dificuldade dos oponentes controlados pela CPU. */
enum class Difficulty(
    val label: String,
    val description: String,
    /** Vantagem numérica mínima que a IA exige para atacar (menor = mais agressiva). */
    val attackThreshold: Int,
    /** Força mínima do território para a IA considerar atacar dali. */
    val minArmiesToAttack: Int,
    /** Exércitos extras que cada CPU recebe por turno. */
    val bonusReinforcements: Int
) {
    RECRUTA("Recruta", "A CPU é cautelosa e só ataca com folga. Bom para aprender.", 3, 5, 0),
    VETERANO("Veterano", "A CPU joga de forma equilibrada. Desafio padrão.", 1, 3, 0),
    GENERAL("General", "A CPU é agressiva e recebe 1 exército extra por turno.", 1, 3, 1),
    MARECHAL("Marechal", "A CPU é implacável e recebe 3 exércitos extras por turno.", 0, 2, 3);

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

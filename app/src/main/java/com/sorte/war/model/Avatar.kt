package com.sorte.war.model

/** Estilo de elmo/toucado usado para desenhar o retrato do comandante. */
enum class Headgear {
    ELMO_GREGO, LOUROS, ELMO_PUNICO, NEMES, ELMO_CAVALEIRO, ELMO_ESTEPE,
    TURBANTE, BICORNE, QUEPE, FAIXA, ELMO_CELTA, ELMO_CHINES
}

/**
 * Comandantes históricos disponíveis como avatar do jogador.
 * Todos são figuras históricas consagradas, ao estilo dos jogos de estratégia.
 */
enum class Avatar(
    val commander: String,
    val epithet: String,
    val era: String,
    val headgear: Headgear,
    val skinArgb: Long,
    val accentArgb: Long
) {
    ALEXANDRE(
        "Alexandre", "o Grande", "Macedônia, séc. IV a.C.",
        Headgear.ELMO_GREGO, 0xFFE0AC85, 0xFFD64545
    ),
    CESAR(
        "Júlio César", "Imperador de Roma", "Roma, séc. I a.C.",
        Headgear.LOUROS, 0xFFE3B48F, 0xFFC9A227
    ),
    ANIBAL(
        "Aníbal Barca", "Terror de Roma", "Cartago, séc. III a.C.",
        Headgear.ELMO_PUNICO, 0xFFC98F63, 0xFF8E6BC4
    ),
    CLEOPATRA(
        "Cleópatra VII", "Rainha do Nilo", "Egito, séc. I a.C.",
        Headgear.NEMES, 0xFFD9A277, 0xFF2FA8C4
    ),
    JOANA(
        "Joana d'Arc", "A Donzela", "França, séc. XV",
        Headgear.ELMO_CAVALEIRO, 0xFFEFC6A6, 0xFFE8E2D4
    ),
    GENGIS(
        "Gengis Khan", "Senhor da Estepe", "Mongólia, séc. XIII",
        Headgear.ELMO_ESTEPE, 0xFFD8AC7C, 0xFF7A5230
    ),
    SALADINO(
        "Saladino", "Sultão do Egito", "Egito e Síria, séc. XII",
        Headgear.TURBANTE, 0xFFC98F63, 0xFF2E9E6B
    ),
    NAPOLEAO(
        "Napoleão", "Imperador dos Franceses", "França, séc. XIX",
        Headgear.BICORNE, 0xFFEFC6A6, 0xFF2A4C8F
    ),
    CAXIAS(
        "Duque de Caxias", "Patrono do Exército", "Brasil, séc. XIX",
        Headgear.QUEPE, 0xFFE0AC85, 0xFF1F6B3A
    ),
    ZUMBI(
        "Zumbi", "Líder dos Palmares", "Brasil, séc. XVII",
        Headgear.FAIXA, 0xFF6B4430, 0xFFD9A227
    ),
    BOUDICA(
        "Boudica", "Rainha dos Icenos", "Britânia, séc. I",
        Headgear.ELMO_CELTA, 0xFFF0CBAD, 0xFFB8422F
    ),
    SUNTZU(
        "Sun Tzu", "A Arte da Guerra", "China, séc. V a.C.",
        Headgear.ELMO_CHINES, 0xFFE3BE8F, 0xFFC0392B
    );

    companion object {
        val all: List<Avatar> get() = entries
        fun byId(id: Int): Avatar = entries.getOrElse(id) { ALEXANDRE }
    }
}

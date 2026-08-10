package com.sorte.war.model

import androidx.annotation.DrawableRes
import com.sorte.war.R

/** Famílias do baralho ilustrado. */
enum class CardFamily(val label: String, val accentArgb: Long) {
    UNIDADE("Unidades", 0xFF4FB6B2),
    TATICA("Táticas", 0xFFD9A63A),
    OBJETIVO("Objetivos", 0xFFC0533F),
    ESPECIAL("Especiais", 0xFFB07CD8)
}

/**
 * O baralho ilustrado do jogo: 21 cartas com a arte pintada.
 *
 * As cartas de território continuam sendo as do tabuleiro (silhueta +
 * símbolo) — estas aqui são o baralho de coleção, consultável no Arsenal, e
 * emprestam a arte às missões secretas.
 *
 * `power` só existe nas unidades (o número impresso no canto da carta).
 */
enum class CardArt(
    val number: Int,
    val title: String,
    val family: CardFamily,
    val power: Int?,
    val effect: String,
    @DrawableRes val art: Int
) {
    INFANTARIA(
        1, "Infantaria", CardFamily.UNIDADE, 1,
        "Tropa de linha. A base de qualquer ofensiva.",
        R.drawable.carta_01_infantaria
    ),
    PARAQUEDISTAS(
        2, "Paraquedistas", CardFamily.UNIDADE, 2,
        "Salto atrás das linhas inimigas.",
        R.drawable.carta_02_paraquedistas
    ),
    TANQUE(
        3, "Tanque", CardFamily.UNIDADE, 3,
        "Blindado de assalto. Rompe posições fortificadas.",
        R.drawable.carta_03_tanque
    ),
    ARTILHARIA(
        4, "Artilharia", CardFamily.UNIDADE, 3,
        "Fogo pesado a longa distância.",
        R.drawable.carta_04_artilharia
    ),
    HELICOPTERO(
        5, "Helicóptero", CardFamily.UNIDADE, 4,
        "Apoio aéreo aproximado e transporte rápido.",
        R.drawable.carta_05_helicoptero
    ),
    CACA(
        6, "Caça", CardFamily.UNIDADE, 5,
        "Superioridade aérea sobre o campo de batalha.",
        R.drawable.carta_06_caca
    ),
    NAVIO_DE_GUERRA(
        7, "Navio de Guerra", CardFamily.UNIDADE, 5,
        "Domínio naval e bombardeio costeiro.",
        R.drawable.carta_07_navio_de_guerra
    ),

    ATAQUE_AEREO(
        8, "Ataque Aéreo", CardFamily.TATICA, null,
        "Elimine até 2 exércitos de territórios adjacentes.",
        R.drawable.carta_08_ataque_aereo
    ),
    BOMBARDEIO(
        9, "Bombardeio", CardFamily.TATICA, null,
        "Remova até 2 exércitos de qualquer território inimigo.",
        R.drawable.carta_09_bombardeio
    ),
    ESPIONAGEM(
        10, "Espionagem", CardFamily.TATICA, null,
        "Veja as cartas e o número de exércitos de um jogador à sua escolha.",
        R.drawable.carta_10_espionagem
    ),
    SABOTAGEM(
        11, "Sabotagem", CardFamily.TATICA, null,
        "Anule o bônus de cartas de um jogador até o final do turno.",
        R.drawable.carta_11_sabotagem
    ),
    MARCHA_FORCADA(
        12, "Marcha Forçada", CardFamily.TATICA, null,
        "Movimente até 3 exércitos para qualquer território adjacente.",
        R.drawable.carta_12_marcha_forcada
    ),
    FORTIFICACAO(
        13, "Fortificação", CardFamily.TATICA, null,
        "Receba +3 exércitos neste território na fase de reforço.",
        R.drawable.carta_13_fortificacao
    ),
    DIPLOMACIA(
        14, "Diplomacia", CardFamily.TATICA, null,
        "Faça uma aliança temporária com um jogador. Vocês não podem se atacar por 1 turno.",
        R.drawable.carta_14_diplomacia
    ),

    DOMINACAO_GLOBAL(
        15, "Dominação Global", CardFamily.OBJETIVO, null,
        "Conquiste todos os territórios do mapa.",
        R.drawable.carta_15_dominacao_global
    ),
    SUPREMACIA_CONTINENTAL(
        16, "Supremacia Continental", CardFamily.OBJETIVO, null,
        "Conquiste todos os territórios de um continente.",
        R.drawable.carta_16_supremacia_continental
    ),
    CONTROLE_DE_FRONTEIRAS(
        17, "Controle de Fronteiras", CardFamily.OBJETIVO, null,
        "Conquiste 24 territórios que fazem fronteira com territórios inimigos.",
        R.drawable.carta_17_controle_de_fronteiras
    ),
    EXTERMINIO(
        18, "Extermínio", CardFamily.OBJETIVO, null,
        "Elimine completamente um jogador da partida.",
        R.drawable.carta_18_exterminio
    ),
    SUPERIORIDADE_MILITAR(
        19, "Superioridade Militar", CardFamily.OBJETIVO, null,
        "Tenha o maior número de exércitos no final de seu turno.",
        R.drawable.carta_19_superioridade_militar
    ),

    COLECIONADOR_DE_CARTAS(
        20, "Colecionador de Cartas", CardFamily.ESPECIAL, null,
        "Troque 5 cartas na mesma rodada.",
        R.drawable.carta_20_colecionador_de_cartas
    ),
    REVIRAVOLTA(
        21, "Reviravolta", CardFamily.ESPECIAL, null,
        "Vire o jogo a seu favor! Inverta a vantagem de territórios entre você e o jogador líder.",
        R.drawable.carta_21_reviravolta
    );

    companion object {
        val all: List<CardArt> get() = entries

        fun byFamily(family: CardFamily): List<CardArt> = entries.filter { it.family == family }

        /** Arte pelo número impresso na carta (1 a 21). */
        fun byNumber(number: Int): CardArt? = entries.firstOrNull { it.number == number }

        /** Arte da carta tática jogável. */
        fun of(card: TacticalCard): CardArt = byNumber(card.number) ?: INFANTARIA

        /** Arte da medalha tática. */
        fun of(medal: TacticalMedal): CardArt = byNumber(medal.number) ?: DOMINACAO_GLOBAL

        /**
         * Arte que ilustra a missão secreta sorteada pelo motor.
         *
         * O texto exibido continua vindo do objetivo real — a carta entra só
         * como ilustração, então as regras não mudam.
         */
        fun forObjective(objective: Objective?): CardArt = when (objective) {
            is Objective.ConquerContinents -> SUPREMACIA_CONTINENTAL
            is Objective.ConquerTerritories ->
                if (objective.minArmiesEach > 1) SUPERIORIDADE_MILITAR else CONTROLE_DE_FRONTEIRAS
            is Objective.DestroyPlayer -> EXTERMINIO
            null -> DOMINACAO_GLOBAL
        }
    }
}

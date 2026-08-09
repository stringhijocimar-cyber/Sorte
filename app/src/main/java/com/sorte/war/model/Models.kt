package com.sorte.war.model

/** Fases de um turno, na ordem clássica do jogo de tabuleiro War. */
enum class Phase {
    REFORCO,      // Distribuir novos exércitos
    ATAQUE,       // Atacar territórios inimigos adjacentes
    DESLOCAMENTO, // Mover exércitos entre territórios próprios conectados
    FIM_DE_JOGO
}

/** Símbolos das cartas de território, como no tabuleiro do War. */
enum class CardSymbol(val label: String) {
    CIRCULO("Círculo"),
    QUADRADO("Quadrado"),
    TRIANGULO("Triângulo"),
    CORINGA("Coringa")
}

/** Uma carta de território que o jogador recebe ao conquistar. */
data class Card(
    val symbol: CardSymbol,
    val territoryId: Int // -1 para coringa
)

/** Definição estática de um continente. */
data class Continent(
    val id: Int,
    val name: String,
    val bonus: Int,
    val colorArgb: Long,
    val territoryIds: List<Int>
)

/** Definição estática de um território (dados que não mudam durante a partida). */
data class Territory(
    val id: Int,
    val name: String,
    val continentId: Int,
    val x: Float,   // posição no mapa virtual 1000x660
    val y: Float,
    val neighbors: List<Int>
)

/** Um jogador (humano ou controlado pela IA). */
data class Player(
    val id: Int,
    val name: String,
    val colorArgb: Long,
    val isHuman: Boolean,
    var eliminated: Boolean = false,
    val cards: MutableList<Card> = mutableListOf(),
    var objective: Objective? = null
)

/** Tipos de objetivo secreto, fiéis às cartas do War. */
sealed class Objective(val description: String) {
    /** Conquistar totalmente uma lista de continentes. */
    class ConquerContinents(
        val continentIds: List<Int>,
        val extraAny: Int, // continentes adicionais quaisquer exigidos
        desc: String
    ) : Objective(desc)

    /** Conquistar N territórios (opcionalmente com mínimo de exércitos em cada). */
    class ConquerTerritories(
        val count: Int,
        val minArmiesEach: Int,
        desc: String
    ) : Objective(desc)

    /** Destruir totalmente os exércitos de uma cor. */
    class DestroyPlayer(
        val targetColorArgb: Long,
        val targetColorName: String,
        desc: String
    ) : Objective(desc)
}

/** Resultado de uma rodada de combate, usado pela UI para exibir os dados. */
data class BattleResult(
    val attackerTerritoryId: Int,
    val defenderTerritoryId: Int,
    val attackerDice: List<Int>,
    val defenderDice: List<Int>,
    val attackerLosses: Int,
    val defenderLosses: Int,
    val conquered: Boolean
)

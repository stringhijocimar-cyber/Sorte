package com.sorte.war.model

/**
 * Símbolo de cada carta de território.
 *
 * Os valores vêm do que está impresso nas cartas ilustradas do baralho
 * clássico, para que a carta na mão e a regra de troca digam a mesma coisa.
 * Os territórios cujas cartas ainda não chegaram (ou vieram sem o símbolo)
 * recebem um símbolo escolhido para o baralho continuar em 14 de cada,
 * exatamente como no tabuleiro.
 */
object ClassicSymbols {

    /** Símbolo lido na arte da carta. Null quando a carta ainda não existe. */
    private val printed: Map<Int, CardSymbol> = mapOf(
        // Triângulo
        2 to CardSymbol.TRIANGULO,   // Groenlândia
        7 to CardSymbol.TRIANGULO,   // Nova York
        9 to CardSymbol.TRIANGULO,   // Venezuela
        11 to CardSymbol.TRIANGULO,  // Brasil
        13 to CardSymbol.TRIANGULO,  // Islândia
        17 to CardSymbol.TRIANGULO,  // Alemanha
        22 to CardSymbol.TRIANGULO,  // Sudão
        25 to CardSymbol.TRIANGULO,  // Madagascar
        30 to CardSymbol.TRIANGULO,  // Omsk
        40 to CardSymbol.TRIANGULO,  // Nova Guiné

        // Quadrado
        1 to CardSymbol.QUADRADO,    // Mackenzie
        4 to CardSymbol.QUADRADO,    // Ottawa
        6 to CardSymbol.QUADRADO,    // Califórnia
        10 to CardSymbol.QUADRADO,   // Peru
        14 to CardSymbol.QUADRADO,   // Inglaterra
        15 to CardSymbol.QUADRADO,   // Suécia
        18 to CardSymbol.QUADRADO,   // Polônia
        21 to CardSymbol.QUADRADO,   // Egito
        24 to CardSymbol.QUADRADO,   // África do Sul
        26 to CardSymbol.QUADRADO,   // Aral

        // Círculo
        8 to CardSymbol.CIRCULO,     // México
        19 to CardSymbol.CIRCULO,    // França
        20 to CardSymbol.CIRCULO,    // Argélia
        23 to CardSymbol.CIRCULO,    // Congo
        27 to CardSymbol.CIRCULO,    // Oriente Médio
        41 to CardSymbol.CIRCULO     // Austrália
    )

    /**
     * Os 16 territórios do último lote. Os símbolos vieram impressos exatamente
     * como especificado, então entram junto com os demais.
     */
    private val printedLate: Map<Int, CardSymbol> = mapOf(
        0 to CardSymbol.CIRCULO,     // Alasca
        3 to CardSymbol.CIRCULO,     // Vancouver
        5 to CardSymbol.CIRCULO,     // Labrador
        12 to CardSymbol.CIRCULO,    // Argentina
        16 to CardSymbol.CIRCULO,    // Moscou
        28 to CardSymbol.CIRCULO,    // Omã
        29 to CardSymbol.CIRCULO,    // Sibéria
        31 to CardSymbol.CIRCULO,    // Tchita

        32 to CardSymbol.TRIANGULO,  // Vladivostok
        33 to CardSymbol.TRIANGULO,  // Mongólia
        34 to CardSymbol.TRIANGULO,  // Japão
        35 to CardSymbol.TRIANGULO,  // China

        36 to CardSymbol.QUADRADO,   // Índia
        37 to CardSymbol.QUADRADO,   // Vietnã
        38 to CardSymbol.QUADRADO,   // Sumatra
        39 to CardSymbol.QUADRADO    // Bornéu
    )

    private val all: Map<Int, CardSymbol> = printed + printedLate

    /** Existe símbolo impresso na arte desse território? */
    fun isPrinted(territoryId: Int): Boolean = territoryId in all

    /** Símbolo da carta desse território. */
    fun of(territoryId: Int): CardSymbol =
        all[territoryId] ?: CardSymbol.CIRCULO

    /** Quantas cartas de cada símbolo o baralho tem (deve ser 14, 14 e 14). */
    fun distribution(territoryCount: Int): Map<CardSymbol, Int> =
        (0 until territoryCount).groupingBy { of(it) }.eachCount()
}

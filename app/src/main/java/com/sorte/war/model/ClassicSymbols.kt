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
     * Preenchimento dos territórios sem símbolo impresso, calibrado para
     * fechar o baralho em 14 círculos, 14 quadrados e 14 triângulos.
     */
    private val filler: Map<Int, CardSymbol> = mapOf(
        0 to CardSymbol.CIRCULO,     // Alasca (arte veio sem símbolo)
        3 to CardSymbol.CIRCULO,     // Vancouver (sem carta)
        5 to CardSymbol.CIRCULO,     // Labrador (arte veio sem símbolo)
        12 to CardSymbol.CIRCULO,    // Argentina (arte veio sem símbolo)
        16 to CardSymbol.CIRCULO,    // Moscou (sem carta)
        28 to CardSymbol.CIRCULO,    // Omã (sem carta)
        29 to CardSymbol.CIRCULO,    // Sibéria (sem carta)
        31 to CardSymbol.CIRCULO,    // Tchita (sem carta)

        32 to CardSymbol.TRIANGULO,  // Vladivostok (sem carta)
        33 to CardSymbol.TRIANGULO,  // Mongólia (sem carta)
        34 to CardSymbol.TRIANGULO,  // Japão (sem carta)
        35 to CardSymbol.TRIANGULO,  // China (sem carta)

        36 to CardSymbol.QUADRADO,   // Índia (sem carta)
        37 to CardSymbol.QUADRADO,   // Vietnã (sem carta)
        38 to CardSymbol.QUADRADO,   // Sumatra (sem carta)
        39 to CardSymbol.QUADRADO    // Bornéu (sem carta)
    )

    /** Existe símbolo impresso na arte desse território? */
    fun isPrinted(territoryId: Int): Boolean = territoryId in printed

    /** Símbolo da carta desse território. */
    fun of(territoryId: Int): CardSymbol =
        printed[territoryId] ?: filler[territoryId] ?: CardSymbol.CIRCULO

    /** Quantas cartas de cada símbolo o baralho tem (deve ser 14, 14 e 14). */
    fun distribution(territoryCount: Int): Map<CardSymbol, Int> =
        (0 until territoryCount).groupingBy { of(it) }.eachCount()
}

package com.sorte.war.model

/**
 * Dados estáticos do mapa-múndi, fiéis ao tabuleiro clássico do War:
 * 42 territórios agrupados em 6 continentes, com adjacências e posições
 * num espaço virtual de 1000 x 660 (renderizado com escala na tela).
 */
object MapData {

    const val VIRTUAL_WIDTH = 1000f
    const val VIRTUAL_HEIGHT = 660f

    val continents: List<Continent> = listOf(
        Continent(0, "América do Norte", 5, 0xFFCDB86B, (0..8).toList()),
        Continent(1, "América do Sul", 2, 0xFFC77D5A, (9..12).toList()),
        Continent(2, "Europa", 5, 0xFF6B8FC7, (13..19).toList()),
        Continent(3, "África", 3, 0xFFC7A15A, (20..25).toList()),
        Continent(4, "Ásia", 7, 0xFF8FB96B, (26..37).toList()),
        Continent(5, "Oceania", 2, 0xFFC76B9E, (38..41).toList())
    )

    val territories: List<Territory> = listOf(
        // ---------- América do Norte (0) ----------
        Territory(0, "Alasca", 0, 70f, 95f, listOf(1, 3, 32)),
        Territory(1, "Mackenzie", 0, 175f, 90f, listOf(0, 2, 3, 4)),
        Territory(2, "Groenlândia", 0, 300f, 60f, listOf(1, 4, 5, 13)),
        Territory(3, "Vancouver", 0, 150f, 165f, listOf(0, 1, 4, 6)),
        Territory(4, "Ottawa", 0, 235f, 160f, listOf(1, 2, 3, 5, 6, 7)),
        Territory(5, "Labrador", 0, 315f, 150f, listOf(2, 4, 7)),
        Territory(6, "Califórnia", 0, 150f, 250f, listOf(3, 4, 7, 8)),
        Territory(7, "Nova York", 0, 255f, 240f, listOf(4, 5, 6, 8)),
        Territory(8, "México", 0, 175f, 325f, listOf(6, 7, 9)),

        // ---------- América do Sul (1) ----------
        Territory(9, "Venezuela", 1, 245f, 405f, listOf(8, 10, 11)),
        Territory(10, "Peru", 1, 230f, 495f, listOf(9, 11, 12)),
        Territory(11, "Brasil", 1, 330f, 475f, listOf(9, 10, 12, 20)),
        Territory(12, "Argentina", 1, 260f, 575f, listOf(10, 11)),

        // ---------- Europa (2) ----------
        Territory(13, "Islândia", 2, 430f, 125f, listOf(2, 14, 15)),
        Territory(14, "Inglaterra", 2, 430f, 200f, listOf(13, 15, 17, 19)),
        Territory(15, "Suécia", 2, 510f, 110f, listOf(13, 14, 16, 17)),
        Territory(16, "Moscou", 2, 620f, 150f, listOf(15, 17, 18, 26, 27, 28)),
        Territory(17, "Alemanha", 2, 505f, 205f, listOf(14, 15, 16, 18, 19)),
        Territory(18, "Polônia", 2, 530f, 265f, listOf(16, 17, 19, 20, 21, 28)),
        Territory(19, "França", 2, 445f, 270f, listOf(14, 17, 18, 20)),

        // ---------- África (3) ----------
        Territory(20, "Argélia", 3, 475f, 355f, listOf(11, 18, 19, 21, 22, 23)),
        Territory(21, "Egito", 3, 565f, 360f, listOf(18, 20, 22, 28)),
        Territory(22, "Sudão", 3, 585f, 435f, listOf(20, 21, 23, 24, 25, 28)),
        Territory(23, "Congo", 3, 545f, 500f, listOf(20, 22, 24)),
        Territory(24, "África do Sul", 3, 565f, 585f, listOf(22, 23, 25)),
        Territory(25, "Madagascar", 3, 650f, 545f, listOf(22, 24)),

        // ---------- Ásia (4) ----------
        Territory(26, "Aral", 4, 690f, 180f, listOf(16, 27, 29, 35)),
        Territory(27, "Oriente Médio", 4, 700f, 255f, listOf(16, 26, 28, 35, 36)),
        Territory(28, "Omã", 4, 665f, 330f, listOf(16, 18, 21, 22, 27, 36)),
        Territory(29, "Sibéria", 4, 760f, 105f, listOf(26, 30, 31, 33, 35)),
        Territory(30, "Omsk", 4, 805f, 160f, listOf(29, 31, 32, 33)),
        Territory(31, "Tchita", 4, 845f, 105f, listOf(29, 30, 32)),
        Territory(32, "Vladivostok", 4, 925f, 120f, listOf(0, 30, 31, 33, 34)),
        Territory(33, "Mongólia", 4, 830f, 205f, listOf(29, 30, 32, 34, 35)),
        Territory(34, "Japão", 4, 940f, 215f, listOf(32, 33)),
        Territory(35, "China", 4, 800f, 255f, listOf(26, 27, 29, 33, 36, 37)),
        Territory(36, "Índia", 4, 750f, 325f, listOf(27, 28, 35, 37)),
        Territory(37, "Vietnã", 4, 825f, 325f, listOf(35, 36, 38)),

        // ---------- Oceania (5) ----------
        Territory(38, "Sumatra", 5, 810f, 425f, listOf(37, 39, 41)),
        Territory(39, "Bornéu", 5, 890f, 430f, listOf(38, 40, 41)),
        Territory(40, "Nova Guiné", 5, 950f, 465f, listOf(39, 41)),
        Territory(41, "Austrália", 5, 900f, 550f, listOf(38, 39, 40))
    )

    /** Índice rápido por id. */
    val byId: Map<Int, Territory> = territories.associateBy { it.id }
    val continentById: Map<Int, Continent> = continents.associateBy { it.id }

    fun territory(id: Int): Territory = byId.getValue(id)
    fun continent(id: Int): Continent = continentById.getValue(id)

    /** Conjunto único de arestas (para desenhar as linhas de adjacência uma só vez). */
    val edges: List<Pair<Int, Int>> = buildList {
        val seen = HashSet<Long>()
        for (t in territories) {
            for (n in t.neighbors) {
                val a = minOf(t.id, n)
                val b = maxOf(t.id, n)
                val key = a.toLong() * 100 + b
                if (seen.add(key)) add(a to b)
            }
        }
    }
}

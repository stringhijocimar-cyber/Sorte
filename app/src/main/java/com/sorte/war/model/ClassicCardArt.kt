package com.sorte.war.model

import com.sorte.war.R

/**
 * Arte ilustrada das cartas de território.
 *
 * O baralho só passa a ser exibido com as ilustrações quando o conjunto está
 * inteiro — 42 territórios com arte e com o símbolo impresso legível. Enquanto
 * faltar carta, a mão continua sendo desenhada como estava (silhueta do
 * território e símbolo), para não misturar dois estilos na mesma mão.
 */
object ClassicCardArt {

    private val art: Map<Int, Int> = mapOf(
        0 to R.drawable.terr_00_alasca,
        1 to R.drawable.terr_01_mackenzie,
        2 to R.drawable.terr_02_groenlandia,
        4 to R.drawable.terr_04_ottawa,
        5 to R.drawable.terr_05_labrador,
        6 to R.drawable.terr_06_california,
        7 to R.drawable.terr_07_novayork,
        8 to R.drawable.terr_08_mexico,
        9 to R.drawable.terr_09_venezuela,
        10 to R.drawable.terr_10_peru,
        11 to R.drawable.terr_11_brasil,
        12 to R.drawable.terr_12_argentina,
        13 to R.drawable.terr_13_islandia,
        14 to R.drawable.terr_14_inglaterra,
        15 to R.drawable.terr_15_suecia,
        17 to R.drawable.terr_17_alemanha,
        18 to R.drawable.terr_18_polonia,
        19 to R.drawable.terr_19_franca,
        20 to R.drawable.terr_20_argelia,
        21 to R.drawable.terr_21_egito,
        22 to R.drawable.terr_22_sudao,
        23 to R.drawable.terr_23_congo,
        24 to R.drawable.terr_24_africadosul,
        25 to R.drawable.terr_25_madagascar,
        26 to R.drawable.terr_26_aral,
        27 to R.drawable.terr_27_orientemedio,
        30 to R.drawable.terr_30_omsk,
        3 to R.drawable.terr_03_vancouver,
        16 to R.drawable.terr_16_moscou,
        28 to R.drawable.terr_28_oma,
        29 to R.drawable.terr_29_siberia,
        31 to R.drawable.terr_31_tchita,
        32 to R.drawable.terr_32_vladivostok,
        33 to R.drawable.terr_33_mongolia,
        34 to R.drawable.terr_34_japao,
        35 to R.drawable.terr_35_china,
        36 to R.drawable.terr_36_india,
        37 to R.drawable.terr_37_vietna,
        38 to R.drawable.terr_38_sumatra,
        39 to R.drawable.terr_39_borneu,
        40 to R.drawable.terr_40_novaguine,
        41 to R.drawable.terr_41_australia
    )

    /** Os dois coringas do baralho. */
    val jokerArt: Int = R.drawable.terr_coringa
    val jokerArtAlt: Int = R.drawable.terr_coringa_02

    /** Arte do território, ou null se a carta ainda não foi entregue. */
    fun of(territoryId: Int): Int? = art[territoryId]

    /** Territórios que ainda não têm carta ilustrada. */
    fun missing(territoryCount: Int): List<Int> =
        (0 until territoryCount).filter { it !in art }

    /** Territórios com arte, mas sem o símbolo impresso na ilustração. */
    fun withoutPrintedSymbol(territoryCount: Int): List<Int> =
        (0 until territoryCount).filter { it in art && !ClassicSymbols.isPrinted(it) }

    /**
     * O baralho ilustrado está completo? Só então a mão passa a usar as artes.
     */
    fun isComplete(territoryCount: Int): Boolean =
        missing(territoryCount).isEmpty() && withoutPrintedSymbol(territoryCount).isEmpty()

    /** Cartas prontas, para a vitrine do Arsenal. */
    fun availableTerritories(territoryCount: Int): List<Int> =
        (0 until territoryCount).filter { it in art }
}

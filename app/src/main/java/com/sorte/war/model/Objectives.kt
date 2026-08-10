package com.sorte.war.model

/** Cor de um exército, como nas peças do War. */
data class ArmyColor(val name: String, val argb: Long)

object PlayerPalette {
    val VERMELHO = ArmyColor("Vermelho", 0xFFE53935)
    val AZUL = ArmyColor("Azul", 0xFF1E88E5)
    val VERDE = ArmyColor("Verde", 0xFF43A047)
    val AMARELO = ArmyColor("Amarelo", 0xFFFDD835)
    val PRETO = ArmyColor("Preto", 0xFF546E7A)
    val BRANCO = ArmyColor("Branco", 0xFFECEFF1)

    /** Ordem de distribuição das cores para até 6 jogadores. */
    val ordered = listOf(VERMELHO, AZUL, VERDE, AMARELO, PRETO, BRANCO)
}

object Objectives {

    /**
     * Baralho completo de objetivos do War. Os objetivos de "destruir cor"
     * são gerados para todas as cores padrão; o motor troca por um objetivo
     * alternativo caso a cor-alvo não esteja em jogo ou seja a do próprio jogador.
     */
    fun buildDeck(): List<Objective> {
        val deck = mutableListOf<Objective>()

        // Objetivos de conquista de continentes
        deck += Objective.ConquerContinents(
            continentIds = listOf(4, 3), extraAny = 0,
            desc = "Conquistar toda a Ásia e toda a África."
        )
        deck += Objective.ConquerContinents(
            continentIds = listOf(0, 3), extraAny = 0,
            desc = "Conquistar toda a América do Norte e toda a África."
        )
        deck += Objective.ConquerContinents(
            continentIds = listOf(4, 1), extraAny = 0,
            desc = "Conquistar toda a Ásia e toda a América do Sul."
        )
        deck += Objective.ConquerContinents(
            continentIds = listOf(2, 5), extraAny = 1,
            desc = "Conquistar toda a Europa, toda a Oceania e mais um continente à sua escolha."
        )
        deck += Objective.ConquerContinents(
            continentIds = listOf(0, 5), extraAny = 1,
            desc = "Conquistar toda a América do Norte, toda a Oceania e mais um continente à sua escolha."
        )
        deck += Objective.ConquerContinents(
            continentIds = listOf(2, 1), extraAny = 1,
            desc = "Conquistar toda a Europa, toda a América do Sul e mais um continente à sua escolha."
        )

        // Objetivos de número de territórios
        deck += Objective.ConquerTerritories(
            count = 24, minArmiesEach = 1,
            desc = "Conquistar 24 territórios quaisquer."
        )
        deck += Objective.ConquerTerritories(
            count = 18, minArmiesEach = 2,
            desc = "Conquistar 18 territórios, mantendo pelo menos 2 exércitos em cada um deles."
        )

        // Objetivos de destruição de cor
        for (c in PlayerPalette.ordered) {
            deck += Objective.DestroyPlayer(
                targetColorArgb = c.argb,
                targetColorName = c.name,
                desc = "Destruir totalmente os exércitos do jogador ${c.name}. " +
                        "Se ele não estiver em jogo ou for você, conquiste 24 territórios."
            )
        }

        return deck
    }

    /** Objetivo alternativo padrão quando um objetivo de destruição não é válido. */
    fun fallback(): Objective = Objective.ConquerTerritories(
        count = 24, minArmiesEach = 1,
        desc = "Conquistar 24 territórios quaisquer."
    )
}

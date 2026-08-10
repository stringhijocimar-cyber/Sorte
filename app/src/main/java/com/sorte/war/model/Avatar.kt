package com.sorte.war.model

import androidx.annotation.DrawableRes
import com.sorte.war.R

/**
 * Comandantes disponíveis como avatar do jogador.
 *
 * Cada um é um retrato militar ilustrado, exibido recortado em círculo no
 * menu, no HUD e no placar da partida.
 *
 * A ordem define o `avatarId` gravado nos saves — acrescente novos no fim
 * para não deslocar os índices de partidas já salvas.
 */
enum class Avatar(
    val commander: String,
    val rankTitle: String,
    val specialty: String,
    @DrawableRes val portrait: Int,
    val accentArgb: Long
) {
    VARGAS(
        "Rafael Vargas", "Comandante-Geral",
        "Veterano de campanhas em três continentes.",
        R.drawable.avatar_01, 0xFFD9A63A
    ),
    OKONKWO(
        "Marcus Okonkwo", "Chefe de Operações",
        "Especialista em incursões e assalto rápido.",
        R.drawable.avatar_02, 0xFF4FB6B2
    ),
    REINHARDT(
        "Otto Reinhardt", "General de Exército",
        "Condecorado por defesa de posições críticas.",
        R.drawable.avatar_03, 0xFFC0533F
    ),
    KOVAC(
        "Elena Kovac", "Oficial de Elite",
        "Comanda unidades de reconhecimento avançado.",
        R.drawable.avatar_04, 0xFF8E6BC4
    ),
    GREY(
        "Aldous Grey", "Almirante",
        "Domina rotas marítimas e desembarques.",
        R.drawable.avatar_05, 0xFF5D8FC7
    ),
    LIU(
        "Liu Wei", "General de Divisão",
        "Estrategista de longas campanhas terrestres.",
        R.drawable.avatar_06, 0xFFD4633F
    ),
    AKSOY(
        "Kaan Aksoy", "Comandante Tático",
        "Opera em terreno hostil e linhas avançadas.",
        R.drawable.avatar_07, 0xFFB98A3A
    ),
    MARCHETTI(
        "Sofia Marchetti", "Comandante de Frota",
        "Coordena forças combinadas em larga escala.",
        R.drawable.avatar_08, 0xFF4A9E6B
    ),
    FERRAZ(
        "Diego Ferraz", "Oficial de Campo",
        "Reage rápido e explora brechas inimigas.",
        R.drawable.avatar_09, 0xFF3E8FA8
    ),
    DIALLO(
        "Amara Diallo", "Comandante Suprema",
        "Lidera ofensivas decisivas até o domínio total.",
        R.drawable.avatar_10, 0xFFC9A227
    );

    companion object {
        val all: List<Avatar> get() = entries

        /**
         * Avatar de um índice salvo. Índices fora da faixa (de saves antigos,
         * quando o elenco era outro) caem no primeiro comandante.
         */
        fun byId(id: Int): Avatar = entries.getOrElse(id) { entries.first() }
    }
}

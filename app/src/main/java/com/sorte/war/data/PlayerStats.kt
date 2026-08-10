package com.sorte.war.data

import android.content.Context
import com.sorte.war.model.Rank

/** Estatísticas acumuladas do jogador entre partidas. */
data class PlayerStats(
    val gamesPlayed: Int = 0,
    val victories: Int = 0,
    val defeats: Int = 0,
    val territoriesConquered: Int = 0,
    val battlesWon: Int = 0,
    val battlesLost: Int = 0,
    val armiesLost: Int = 0,
    val cardSetsTraded: Int = 0,
    val bestTerritories: Int = 0,
    val favoriteAvatarId: Int = 0,
    val playerName: String = "Comandante"
) {
    val rank: Rank get() = Rank.forVictories(victories)
    val nextRank: Rank? get() = Rank.next(rank)

    /** Progresso (0..1) rumo à próxima patente. */
    val rankProgress: Float
        get() {
            val next = nextRank ?: return 1f
            val from = rank.victoriesNeeded
            val span = (next.victoriesNeeded - from).coerceAtLeast(1)
            return ((victories - from).toFloat() / span).coerceIn(0f, 1f)
        }

    val winRate: Int
        get() = if (gamesPlayed == 0) 0 else (victories * 100) / gamesPlayed
}

/** Guarda as estatísticas do jogador no aparelho. */
class StatsStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("war_stats", Context.MODE_PRIVATE)

    fun load(): PlayerStats = PlayerStats(
        gamesPlayed = prefs.getInt(GAMES, 0),
        victories = prefs.getInt(WINS, 0),
        defeats = prefs.getInt(LOSSES, 0),
        territoriesConquered = prefs.getInt(CONQUERED, 0),
        battlesWon = prefs.getInt(BWON, 0),
        battlesLost = prefs.getInt(BLOST, 0),
        armiesLost = prefs.getInt(ARMIES, 0),
        cardSetsTraded = prefs.getInt(TRADES, 0),
        bestTerritories = prefs.getInt(BEST, 0),
        favoriteAvatarId = prefs.getInt(AVATAR, 0),
        playerName = prefs.getString(NAME, "Comandante") ?: "Comandante"
    )

    private fun add(key: String, delta: Int) {
        if (delta <= 0) return
        prefs.edit().putInt(key, prefs.getInt(key, 0) + delta).apply()
    }

    /** Registra o resultado de uma rodada de combate do jogador humano. */
    fun recordBattle(won: Boolean, armiesLost: Int, conquered: Boolean) {
        add(if (won) BWON else BLOST, 1)
        add(ARMIES, armiesLost)
        if (conquered) add(CONQUERED, 1)
    }

    fun recordTrade() = add(TRADES, 1)

    /** Registra o fim de uma partida. */
    fun recordGameEnd(won: Boolean, territoriesHeld: Int) {
        val e = prefs.edit()
        e.putInt(GAMES, prefs.getInt(GAMES, 0) + 1)
        if (won) e.putInt(WINS, prefs.getInt(WINS, 0) + 1)
        else e.putInt(LOSSES, prefs.getInt(LOSSES, 0) + 1)
        if (territoriesHeld > prefs.getInt(BEST, 0)) e.putInt(BEST, territoriesHeld)
        e.apply()
    }

    /** Guarda as preferências do jogador para preencher o próximo jogo. */
    fun rememberProfile(name: String, avatarId: Int) {
        prefs.edit().putString(NAME, name).putInt(AVATAR, avatarId).apply()
    }

    fun reset() = prefs.edit().clear().apply()

    companion object {
        private const val GAMES = "games"
        private const val WINS = "wins"
        private const val LOSSES = "losses"
        private const val CONQUERED = "conquered"
        private const val BWON = "battles_won"
        private const val BLOST = "battles_lost"
        private const val ARMIES = "armies_lost"
        private const val TRADES = "trades"
        private const val BEST = "best_territories"
        private const val AVATAR = "avatar"
        private const val NAME = "name"
    }
}

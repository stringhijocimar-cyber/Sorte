package com.sorte.war.data

import android.content.Context
import com.sorte.war.engine.GameEngine
import com.sorte.war.model.MapData

/** Resumo da partida salva, mostrado no menu. */
data class SaveInfo(
    val playerName: String,
    val playersAlive: Int,
    val territories: Int,
    val savedAt: Long
)

/** Guarda a partida em andamento no armazenamento do aparelho. */
class GameStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("war_game", Context.MODE_PRIVATE)

    fun save(engine: GameEngine) {
        if (engine.winnerId != null) { clear(); return }
        val human = engine.players.firstOrNull { it.isHuman }
        prefs.edit()
            .putString(KEY_GAME, engine.toSave())
            .putString(KEY_NAME, human?.name ?: "Comandante")
            .putInt(KEY_ALIVE, engine.players.count { !it.eliminated })
            .putInt(KEY_TERR, human?.let { engine.ownedCount(it.id) } ?: 0)
            .putLong(KEY_AT, System.currentTimeMillis())
            .apply()
    }

    fun load(): GameEngine? {
        val text = prefs.getString(KEY_GAME, null) ?: return null
        val engine = GameEngine.fromSave(text)
        if (engine == null) { clear(); return null }   // save corrompido ou de versão antiga
        return engine
    }

    fun info(): SaveInfo? {
        if (!prefs.contains(KEY_GAME)) return null
        return SaveInfo(
            playerName = prefs.getString(KEY_NAME, "Comandante") ?: "Comandante",
            playersAlive = prefs.getInt(KEY_ALIVE, 0),
            territories = prefs.getInt(KEY_TERR, 0),
            savedAt = prefs.getLong(KEY_AT, 0L)
        )
    }

    fun hasSave(): Boolean = prefs.contains(KEY_GAME)

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_GAME = "game"
        private const val KEY_NAME = "name"
        private const val KEY_ALIVE = "alive"
        private const val KEY_TERR = "territories"
        private const val KEY_AT = "saved_at"

        const val TOTAL_TERRITORIES = 42
    }
}

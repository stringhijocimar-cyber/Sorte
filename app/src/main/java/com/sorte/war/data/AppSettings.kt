package com.sorte.war.data

import android.content.Context

/** Como a tela do jogo se comporta ao girar o aparelho. */
enum class ScreenOrientationMode(val label: String, val description: String) {
    AUTOMATICO("Automático", "Gira junto com o aparelho."),
    RETRATO("Retrato", "Trava a tela em pé."),
    PAISAGEM("Paisagem", "Trava a tela deitada — o mapa fica bem maior.")
}

/** Preferências gerais do aplicativo. */
class AppSettings(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("war_settings", Context.MODE_PRIVATE)

    var orientation: ScreenOrientationMode
        get() = runCatching {
            ScreenOrientationMode.valueOf(
                prefs.getString(KEY_ORIENTATION, ScreenOrientationMode.AUTOMATICO.name)!!
            )
        }.getOrDefault(ScreenOrientationMode.AUTOMATICO)
        set(value) {
            prefs.edit().putString(KEY_ORIENTATION, value.name).apply()
        }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) { prefs.edit().putBoolean(KEY_SOUND, value).apply() }

    private companion object {
        const val KEY_ORIENTATION = "orientation"
        const val KEY_SOUND = "sound"
    }
}

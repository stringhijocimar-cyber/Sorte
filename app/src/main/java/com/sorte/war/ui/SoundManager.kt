package com.sorte.war.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.sorte.war.R

/** Efeitos sonoros do jogo (dados, canhão, tiros, conquista, vitória, derrota). */
enum class Sfx { DICE, CANNON, GUNFIRE, CONQUER, VICTORY, DEFEAT, CLICK }

class SoundManager(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids: Map<Sfx, Int> = mapOf(
        Sfx.DICE to pool.load(context, R.raw.dice_roll, 1),
        Sfx.CANNON to pool.load(context, R.raw.cannon, 1),
        Sfx.GUNFIRE to pool.load(context, R.raw.gunfire, 1),
        Sfx.CONQUER to pool.load(context, R.raw.conquer, 1),
        Sfx.VICTORY to pool.load(context, R.raw.victory, 1),
        Sfx.DEFEAT to pool.load(context, R.raw.defeat, 1),
        Sfx.CLICK to pool.load(context, R.raw.click, 1)
    )

    var enabled: Boolean = true

    fun play(sfx: Sfx, volume: Float = 1f) {
        if (!enabled) return
        val id = ids[sfx] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}

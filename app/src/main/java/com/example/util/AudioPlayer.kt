package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator

object AudioPlayer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClickSound() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }

    fun playLevelUpSound() {
        // Play a short sequence
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }

    fun playTimerCompleteSound() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 500)
    }
}

// app/src/main/java/com/deskpet/app/util/SpeechHelper.kt
package com.deskpet.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Wrapper around Android TextToSpeech for pet voice interaction.
 * Uses Chinese voice with cute parameters (slower speed, higher pitch).
 */
object SpeechHelper : TextToSpeech.OnInitListener {

    private const val TAG = "SpeechHelper"

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isTtsAvailable = false
        private set

    private var enabled = false
    private var pendingText: String? = null

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                isTtsAvailable = true
                tts?.setSpeechRate(0.8f) // Slower = cuter
                tts?.setPitch(1.2f)      // Higher pitch = more adorable
                Log.i(TAG, "TTS initialized successfully")
                pendingText?.let {
                    speak(it)
                    pendingText = null
                }
            } else {
                isTtsAvailable = false
                Log.w(TAG, "Chinese TTS not available on this device")
            }
        } else {
            isTtsAvailable = false
            Log.w(TAG, "TTS init failed with status $status")
        }
        isInitialized = true
    }

    fun speak(text: String) {
        if (!enabled || !isTtsAvailable) return
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "pet_speak_${System.currentTimeMillis()}")
    }

    /**
     * Speaks immediately, flushing any queued speech.
     */
    fun speakNow(text: String) {
        if (!enabled || !isTtsAvailable) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pet_speak_now_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        isTtsAvailable = false
    }
}

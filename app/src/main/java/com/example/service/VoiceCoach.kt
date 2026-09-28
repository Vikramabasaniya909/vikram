package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.data.localization.AppLanguage
import java.util.Locale

class VoiceCoach(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var currentLanguage = AppLanguage.ENGLISH
    var isEnabled = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            applyLanguage(currentLanguage)
        }
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        if (isInitialized) {
            applyLanguage(language)
        }
    }

    private fun applyLanguage(language: AppLanguage) {
        val targetLocale = when (language) {
            AppLanguage.HINDI -> Locale("hi", "IN")
            else -> Locale.US
        }
        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English if Hindi TTS data is missing on the specific device
            tts?.setLanguage(Locale.US)
        }
        tts?.setSpeechRate(0.95f)
        tts?.setPitch(1.0f)
    }

    fun speak(text: String, flush: Boolean = false) {
        if (!isEnabled || !isInitialized) return
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "VOICE_COACH_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    fun speakGetReady(language: AppLanguage) {
        val text = when (language) {
            AppLanguage.HINDI -> "तैयार हो जाइए।"
            else -> "Get ready."
        }
        speak(text, flush = true)
    }

    fun speakCountdown(num: Int) {
        speak(num.toString(), flush = true)
    }

    fun speakStart(language: AppLanguage) {
        val text = when (language) {
            AppLanguage.HINDI -> "शुरू करें!"
            else -> "Start!"
        }
        speak(text, flush = true)
    }

    fun speakCadence(phase: Int, language: AppLanguage) {
        // Phase 1: lowering, Phase 2: pushing up, Phase 3: breathing
        val text = when (phase % 3) {
            0 -> if (language == AppLanguage.HINDI) "धीरे-धीरे नीचे जाएं।" else "Lower slowly."
            1 -> if (language == AppLanguage.HINDI) "अब ऊपर आएं।" else "Push back up."
            else -> if (language == AppLanguage.HINDI) "सांस लेते रहें।" else "Keep breathing."
        }
        speak(text, flush = false)
    }

    fun speakHalfway(language: AppLanguage) {
        val text = when (language) {
            AppLanguage.HINDI -> "आधा समय पूरा हुआ।"
            else -> "Halfway there!"
        }
        speak(text, flush = false)
    }

    fun speakRest(seconds: Int, language: AppLanguage) {
        val text = when (language) {
            AppLanguage.HINDI -> "शानदार! $seconds सेकंड आराम करें।"
            else -> "Great job! Rest for $seconds seconds."
        }
        speak(text, flush = true)
    }

    fun speakNextExercise(exerciseName: String, language: AppLanguage) {
        val text = when (language) {
            AppLanguage.HINDI -> "अगला व्यायाम: $exerciseName"
            else -> "Next up: $exerciseName"
        }
        speak(text, flush = false)
    }

    fun speakWorkoutComplete(language: AppLanguage) {
        val text = when (language) {
            AppLanguage.HINDI -> "वर्कआउट पूरा हो गया! बहुत बढ़िया प्रयास!"
            else -> "Workout complete! Fantastic effort!"
        }
        speak(text, flush = true)
    }
}

package dev.shahil.bharatbhraman

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Uttarakhand : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var textUttarakhand: TextView
    private lateinit var btnSpeak: Button
    private lateinit var btnPause: Button

    private var isSpeaking = false // ✅ toggle flag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uttarakhand)

        textUttarakhand = findViewById(R.id.textUttarakhand)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnPause = findViewById(R.id.btnPause)

        tts = TextToSpeech(this, this)

        // 🔊 Speak button (toggle)
        btnSpeak.setOnClickListener {
            if (!isSpeaking) {
                speakText()
                isSpeaking = true
            } else {
                tts.stop()
                isSpeaking = false
            }
        }

        // ⏸ Pause button (force stop)
        btnPause.setOnClickListener {
            if (tts.isSpeaking) {
                tts.stop()
                isSpeaking = false
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale.US

            for (voice in tts.voices) {
                if (voice.name.lowercase().contains("male")) {
                    tts.voice = voice
                    break
                }
            }

            // ❌ Removed auto speak → now muted by default
        }
    }

    private fun speakText() {
        tts.speak(textUttarakhand.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
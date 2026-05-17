package dev.shahil.bharatbhraman

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.*

class Up : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var textUp: TextView
    private lateinit var btnSpeak: Button
    private lateinit var btnPause: Button

    private lateinit var cardTaj: CardView
    private lateinit var cardKashi: CardView
    private lateinit var cardRam: CardView

    private var isSpeaking = false // ✅ toggle flag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_up)

        textUp = findViewById(R.id.textUp)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnPause = findViewById(R.id.btnPause)

        cardTaj = findViewById(R.id.cardTaj)
        cardKashi = findViewById(R.id.cardKashi)
        cardRam = findViewById(R.id.cardRam)

        tts = TextToSpeech(this, this)

        // 🔊 Toggle Speak
        btnSpeak.setOnClickListener {
            if (!isSpeaking) {
                speakText()
                isSpeaking = true
            } else {
                tts.stop()
                isSpeaking = false
            }
        }

        // ⏸ Pause button
        btnPause.setOnClickListener {
            if (tts.isSpeaking) {
                tts.stop()
                isSpeaking = false
            }
        }

        cardTaj.setOnClickListener {
            startActivity(Intent(this, TajActivity::class.java))
        }

        cardKashi.setOnClickListener {
            startActivity(Intent(this, KashiActivity::class.java))
        }

        cardRam.setOnClickListener {
            startActivity(Intent(this, RamActivity::class.java))
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

            // ❌ Removed auto speak → muted by default
        }
    }

    private fun speakText() {
        val text = textUp.text.toString()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
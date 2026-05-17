package dev.shahil.bharatbhraman

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.*
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import kotlin.jvm.java

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val text = findViewById<TextView>(R.id.taglineText)

        val uri = Uri.parse("android.resource://$packageName/${R.raw.splash_video}")
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener {
            it.isLooping = false
            videoView.alpha = 0f
            it.setVolume(0f, 0f) // mute
            videoView.animate().alpha(1f).setDuration(800).start()
            videoView.start()
        }

        // 🔥 Cinematic "Coming Out" Animation
        val scale = ScaleAnimation(
            0.3f, 1.1f,
            0.3f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        val translate = TranslateAnimation(
            0f, 0f,
            400f, 0f
        )

        val fade = AlphaAnimation(0f, 1f)

        val set = AnimationSet(true)
        set.duration = 1200
        set.interpolator = OvershootInterpolator()
        set.addAnimation(scale)
        set.addAnimation(translate)
        set.addAnimation(fade)

        text.postDelayed({
            text.startAnimation(set)
        }, 600)

        // Auto move to next screen
        videoView.setOnCompletionListener {
            startActivity(Intent(this, Login::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
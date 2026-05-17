package dev.shahil.bharatbhraman

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity

class RateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val subject = "App Rating"
            val body = "User has rated: $rating stars"

            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("adityarajv2105@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            emailIntent.putExtra(Intent.EXTRA_TEXT, body)

            startActivity(Intent.createChooser(emailIntent, "Send Feedback via Email"))
        }
    }
}
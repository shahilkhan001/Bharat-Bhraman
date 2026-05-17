package dev.shahil.bharatbhraman

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etFeedback = findViewById<EditText>(R.id.etFeedback)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitFeedback)

        btnSubmit.setOnClickListener {
            val feedbackText = etFeedback.text.toString().trim()
            if (feedbackText.isEmpty()) {
                etFeedback.error = "Enter feedback"
                return@setOnClickListener
            }

            val userEmail = auth.currentUser?.email ?: "anonymous"
            val feedbackData = hashMapOf(
                "email" to userEmail,
                "feedback" to feedbackText,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("feedback").add(feedbackData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback sent successfully!", Toast.LENGTH_SHORT).show()
                    etFeedback.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send feedback!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
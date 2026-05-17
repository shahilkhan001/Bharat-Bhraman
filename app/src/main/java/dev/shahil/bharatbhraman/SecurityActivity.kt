package dev.shahil.bharatbhraman

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.Executor

class SecurityActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val uid = user.uid
        val pinInput = findViewById<EditText>(R.id.pinInput)
        val savePin = findViewById<Button>(R.id.savePin)
        val fingerprintBtn = findViewById<Button>(R.id.setupFingerprint)

        // 🔹 Save PIN
        savePin.setOnClickListener {
            val pinText = pinInput.text.toString().trim()
            if (pinText.length != 4) {
                pinInput.error = "PIN must be 4 digits"
                return@setOnClickListener
            }

            db.collection("users").document(uid)
                .set(hashMapOf("pin" to pinText), SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "PIN Saved Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving PIN", Toast.LENGTH_SHORT).show()
                }
        }

        // 🔹 Setup Fingerprint
        fingerprintBtn.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
                Toast.makeText(this, "Fingerprint not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val executor: Executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    db.collection("users").document(uid)
                        .set(hashMapOf("fingerprint_enabled" to true), SetOptions.merge())
                        .addOnSuccessListener {
                            runOnUiThread {
                                Toast.makeText(this@SecurityActivity, "Fingerprint Enabled", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@SecurityActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            runOnUiThread {
                                Toast.makeText(this@SecurityActivity, "Error enabling fingerprint", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {
                        Toast.makeText(this@SecurityActivity, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Setup Fingerprint")
                .setSubtitle("Use your fingerprint to secure app")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }
}
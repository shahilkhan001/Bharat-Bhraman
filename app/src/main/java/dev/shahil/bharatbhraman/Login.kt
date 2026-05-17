package dev.shahil.bharatbhraman

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executor

class Login : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    // 🔥 CLICK ANIMATION
    private fun animateClick(view: View) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signup = findViewById<TextView>(R.id.signup)
        val fgp = findViewById<ImageButton>(R.id.fgp)
        val pinBtn = findViewById<ImageButton>(R.id.pin)

        // 🔹 EMAIL LOGIN
        loginBtn.setOnClickListener {

            animateClick(loginBtn)

            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty()) { email.error = "Enter Email"; return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) { email.error = "Invalid Email"; return@setOnClickListener }
            if (passwordText.isEmpty()) { password.error = "Enter Password"; return@setOnClickListener }

            Toast.makeText(this, "Processing Login...", Toast.LENGTH_SHORT).show()

            auth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        val intent = Intent(this, _root_ide_package_.dev.shahil.bharatbhraman.MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Verify Email First!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 🔹 SIGNUP
        signup.setOnClickListener {

            animateClick(signup)

            startActivity(Intent(this, _root_ide_package_.dev.shahil.bharatbhraman.Signup::class.java))
        }

        // 🔹 PIN LOGIN
        pinBtn.setOnClickListener {

            animateClick(pinBtn)

            Toast.makeText(this, "Opening PIN Login...", Toast.LENGTH_SHORT).show()

            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Login first with Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val pin = doc.getString("pin") ?: ""
                if (pin.isEmpty()) {
                    Toast.makeText(this, "Set PIN first via SecurityActivity", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val input = EditText(this)
                input.hint = "Enter PIN"
                input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

                android.app.AlertDialog.Builder(this)
                    .setTitle("Enter PIN")
                    .setView(input)
                    .setPositiveButton("Login") { _, _ ->
                        if (input.text.toString() == pin) {
                            val intent = Intent(this, _root_ide_package_.dev.shahil.bharatbhraman.MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        // 🔹 FINGERPRINT LOGIN
        fgp.setOnClickListener {

            animateClick(fgp)

            Toast.makeText(this, "Starting Fingerprint Authentication...", Toast.LENGTH_SHORT).show()

            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Login first with Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val fingerEnabled = doc.getBoolean("fingerprint_enabled") ?: false
                if (!fingerEnabled) {
                    Toast.makeText(this, "Enable Fingerprint first via SecurityActivity", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val executor: Executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            Toast.makeText(this@Login, "Fingerprint Verified", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@Login, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        override fun onAuthenticationFailed() {
                            Toast.makeText(this@Login, "Try Again", Toast.LENGTH_SHORT).show()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Login with Fingerprint")
                    .setSubtitle("Use your fingerprint")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}
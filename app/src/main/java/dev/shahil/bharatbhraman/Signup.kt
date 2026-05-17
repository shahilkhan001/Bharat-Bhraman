package dev.shahil.bharatbhraman

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class Signup : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    private fun animateClick(view: View) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val name = findViewById<EditText>(R.id.fullName)
        val dob = findViewById<EditText>(R.id.dob)
        val email = findViewById<EditText>(R.id.emailSignup)
        val pass = findViewById<EditText>(R.id.passwordSignup)
        val confirm = findViewById<EditText>(R.id.confirmPassword)
        val btn = findViewById<Button>(R.id.signupBtn)
        val showPass1 = findViewById<CheckBox>(R.id.showPassword1)
        val showPass2 = findViewById<CheckBox>(R.id.showPassword2)
        val loginRedirect = findViewById<TextView>(R.id.loginRedirect)

        // ✅ FIXED LOGIN REDIRECT
        loginRedirect.setOnClickListener {
            animateClick(loginRedirect)
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // 🔥 DOB PICKER (YEAR FIRST FIX)
        dob.setOnClickListener {

            animateClick(dob)

            val cal = Calendar.getInstance()

            val dialog = DatePickerDialog(
                this,
                { _, y, m, d ->
                    dob.setText("$d/${m + 1}/$y")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )

            dialog.show()

            // 🔥 FORCE YEAR VIEW FIRST
            try {
                val datePicker = dialog.datePicker
                val id = resources.getIdentifier("android:id/year", null, null)
                val yearView = datePicker.findViewById<View>(id)
                yearView?.performClick()
            } catch (e: Exception) {
                // fallback safe
            }
        }

        // SHOW PASSWORD 1
        showPass1.setOnCheckedChangeListener { _, isChecked ->

            animateClick(showPass1)

            pass.inputType = if (isChecked)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            pass.setSelection(pass.text.length)
        }

        // SHOW PASSWORD 2
        showPass2.setOnCheckedChangeListener { _, isChecked ->

            animateClick(showPass2)

            confirm.inputType = if (isChecked)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            confirm.setSelection(confirm.text.length)
        }

        // SIGNUP BUTTON
        btn.setOnClickListener {

            animateClick(btn)

            val nameText = name.text.toString().trim()
            val dobText = dob.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passText = pass.text.toString().trim()
            val confirmText = confirm.text.toString().trim()

            if (nameText.isEmpty()) { name.error = "Enter Name"; return@setOnClickListener }
            if (dobText.isEmpty()) { dob.error = "Select DOB"; return@setOnClickListener }
            if (emailText.isEmpty()) { email.error = "Enter Email"; return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) { email.error = "Invalid Email"; return@setOnClickListener }
            if (passText.length < 6) { pass.error = "Min 6 characters"; return@setOnClickListener }
            if (passText != confirmText) { confirm.error = "Passwords not matching"; return@setOnClickListener }

            auth.createUserWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val user = auth.currentUser

                        user?.sendEmailVerification()

                        val uid = user?.uid ?: return@addOnCompleteListener

                        val map = hashMapOf(
                            "name" to nameText,
                            "dob" to dobText,
                            "email" to emailText,
                            "pin" to "",
                            "fingerprint_enabled" to false,
                            "profileImage" to ""
                        )

                        db.collection("users").document(uid).set(map)
                            .addOnSuccessListener {

                                Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()

                                startActivity(Intent(this, Login::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
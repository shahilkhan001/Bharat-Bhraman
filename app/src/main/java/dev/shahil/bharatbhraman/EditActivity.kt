package dev.shahil.bharatbhraman

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val nameEdit = findViewById<EditText>(R.id.editName)
        val dobEdit = findViewById<EditText>(R.id.editDob)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get().addOnSuccessListener {
            nameEdit.setText(it.getString("name"))
            dobEdit.setText(it.getString("dob"))
        }

        dobEdit.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                dobEdit.setText("$d/${m + 1}/$y")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        saveBtn.setOnClickListener {
            val newName = nameEdit.text.toString().trim()
            val newDob = dobEdit.text.toString().trim()

            if (newName.isEmpty()) {
                nameEdit.error = "Enter Name"
                return@setOnClickListener
            }

            db.collection("users").document(uid)
                .update("name", newName, "dob", newDob)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
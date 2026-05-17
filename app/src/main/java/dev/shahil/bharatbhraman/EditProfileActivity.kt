package dev.shahil.bharatbhraman

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var saveBtn: Button
    private lateinit var profileImg: ImageView

    private var imageUri: Uri? = null

    // Pick Image
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { startCrop(it) }
        }

    // Crop Result
    private val cropImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let {
                    imageUri = it
                    profileImg.setImageURI(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editName = findViewById(R.id.editName)
        saveBtn = findViewById(R.id.saveBtn)
        profileImg = findViewById(R.id.profileImgEdit)

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Load user data
        db.collection("users").document(user.uid).get().addOnSuccessListener {
            editName.setText(it.getString("name"))

            val img = it.getString("profileImage")
            if (!img.isNullOrEmpty()) {
                Glide.with(this).load(img).into(profileImg)
            }
        }

        // Click image → pick
        profileImg.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Save
        saveBtn.setOnClickListener {

            val newName = editName.text.toString()

            db.collection("users")
                .document(user.uid)
                .update("name", newName)

            imageUri?.let {
                uploadImage(it)
            }

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Start Crop
    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped.jpg"))

        val uCrop = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)

        cropImage.launch(uCrop.getIntent(this))
    }

    // Upload Image
    private fun uploadImage(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val ref = FirebaseStorage.getInstance().reference
            .child("profileImages/${user.uid}.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .update("profileImage", url.toString())

                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
            }
    }
}
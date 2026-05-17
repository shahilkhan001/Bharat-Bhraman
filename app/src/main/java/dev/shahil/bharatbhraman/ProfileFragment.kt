package dev.shahil.bharatbhraman

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var name: TextView
    private lateinit var profileImg: ImageView
    private var imageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                uploadImage()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name = view.findViewById(R.id.userName)
        profileImg = view.findViewById(R.id.profileImg)

        name.text = "Loading..."

        profileImg.setOnClickListener {
            imagePicker.launch("image/*")
        }

        view.findViewById<View>(R.id.editProfile).setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        view.findViewById<View>(R.id.security).setOnClickListener {
            startActivity(Intent(requireContext(), SecurityActivity::class.java))
        }

        view.findViewById<View>(R.id.rate).setOnClickListener {
            startActivity(Intent(requireContext(), RateActivity::class.java))
        }

        view.findViewById<View>(R.id.feedback).setOnClickListener {
            startActivity(Intent(requireContext(), FeedbackActivity::class.java))
        }

        view.findViewById<View>(R.id.logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
        }

        view.findViewById<View>(R.id.deleteAccount).setOnClickListener {
            showDeleteDialog()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener {
                name.text = it.getString("name") ?: "User"

                val imageUrl = it.getString("profileImage")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.logo)
                        .into(profileImg)
                }
            }
            .addOnFailureListener {
                name.text = "User"
            }
    }

    private fun uploadImage() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profileImages/${user.uid}.jpg")

        imageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .update("profileImage", downloadUri.toString())

                        Glide.with(this)
                            .load(downloadUri)
                            .into(profileImg)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .delete()

        user.delete().addOnCompleteListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
        }
    }
}
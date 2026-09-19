package com.example.mindmile.users

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var imgProfile: ImageView
    
    // Image Picker Result Launcher
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imgProfile.setImageURI(it)
            imgProfile.imageTintList = null // Remove tint to show original image colors
            imgProfile.setPadding(0, 0, 0, 0) // Remove padding to fill the circle
            // Process and Upload
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        imgProfile = findViewById(R.id.imgProfile)

        btnBack.setOnClickListener {
            finish()
        }
        
        // Open Gallery on Image Click
        imgProfile.setOnClickListener {
            pickImage.launch("image/*")
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val email = currentUser.email
            tvEmail.text = if (!email.isNullOrEmpty()) email else "No Email"
            
            // Fetch User Details from Firestore
            loadUserProfile(uid, tvName)
        } else {
            tvName.text = "Not Logged In"
            tvEmail.text = "-"
        }
    }

    private fun loadUserProfile(uid: String, tvName: TextView) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    val profileImageBase64 = document.getString("profileImage")

                    tvName.text = if (!username.isNullOrEmpty()) username else "No Name Set"
                    
                    if (!profileImageBase64.isNullOrEmpty()) {
                        try {
                            // Use NO_WRAP to avoid newline issues
                            val decodedString = Base64.decode(profileImageBase64, Base64.NO_WRAP)
                            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            imgProfile.setImageBitmap(decodedByte)
                            imgProfile.imageTintList = null // Remove tint to show original image colors
                            imgProfile.setPadding(0, 0, 0, 0) // Remove padding to fill the circle
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    tvName.text = "User Data Not Found"
                }
            }
            .addOnFailureListener {
                tvName.text = "Error Loading Data"
            }
    }
    
    private fun uploadProfileImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            if (bitmap == null) {
                Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show()
                return
            }

            val outputStream = ByteArrayOutputStream()
            
            // 1. Calculate center crop to maintain aspect ratio (Square)
            val dimension = bitmap.width.coerceAtMost(bitmap.height)
            val x = (bitmap.width - dimension) / 2
            val y = (bitmap.height - dimension) / 2
            val squaredBitmap = Bitmap.createBitmap(bitmap, x, y, dimension, dimension)
            
            // 2. Compress to standard size (300x300) without stretching (since it's already square)
            val scaledBitmap = Bitmap.createScaledBitmap(squaredBitmap, 300, 300, true)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // Use NO_WRAP to ensure single line string
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            // Update UI immediately with the scaled bitmap to confirm processing
            imgProfile.setImageBitmap(scaledBitmap)
            imgProfile.imageTintList = null // Remove tint to show original image colors
            imgProfile.setPadding(0, 0, 0, 0) // Remove padding to fill the circle
            
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            
            db.collection("users").document(uid)
                .set(mapOf("profileImage" to base64String), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
                
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

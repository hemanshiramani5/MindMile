package com.example.mindmile.admin

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mindmile.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
 
class AdminAddVideoActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private lateinit var imagePreview: ImageView
    private var videoId: String? = null // For editing existing video
    private var existingThumbnail: String? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(imagePreview)
            imagePreview.setPadding(0, 0, 0, 0)
        }
    }

    private lateinit var btnSave: CardView
    private lateinit var btnSaveText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_video)

        db = FirebaseFirestore.getInstance()

        // UI References
        val backBtn = findViewById<ImageView>(R.id.backButton)
        val titleInput = findViewById<TextInputEditText>(R.id.inputTitle)
        val descInput = findViewById<TextInputEditText>(R.id.inputDescription)
        val urlInput = findViewById<TextInputEditText>(R.id.inputUrl)
        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSaveVideo)
        btnSaveText = findViewById(R.id.btnSaveText)
        imagePreview = findViewById(R.id.imagePreview)
        progressBar = findViewById(R.id.progressBar)
        
        // Setup Spinner with expanded categories
        val categories = listOf(
            "Motivation", "Meditation", "Exercise", "Sleep", "Focus",
            "Happy", "Sad", "Angry", "Counseling", "Motivational Speakers"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = adapter

        // Check if editing existing video
        videoId = intent.getStringExtra("videoId")
        if (videoId != null) {
            // Pre-fill form for editing
            titleInput.setText(intent.getStringExtra("title"))
            descInput.setText(intent.getStringExtra("description"))
            urlInput.setText(intent.getStringExtra("url"))
            existingThumbnail = intent.getStringExtra("thumbnailUrl")
            
            val category = intent.getStringExtra("category")
            // Enhanced category matching (case insensitive)
            if (category != null) {
                val position = categories.indexOfFirst { it.equals(category, ignoreCase = true) }
                if (position >= 0) {
                    spinner.setSelection(position)
                }
            }
            
            // Load existing thumbnail if available
            existingThumbnail?.let { thumb ->
                if (thumb.startsWith("data:image") || thumb.startsWith("/9j")) {
                    // Base64 image
                    try {
                        val imageBytes = Base64.decode(thumb, Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imagePreview.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // URL
                    Glide.with(this).load(thumb).centerCrop().into(imagePreview)
                }
                imagePreview.setPadding(0, 0, 0, 0)
            }
            
            btnSaveText.text = "UPDATE VIDEO"
        }

        // Back Button
        backBtn.setOnClickListener { finish() }

        // Image Selection
        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val desc = descInput.text.toString().trim()
            val url = urlInput.text.toString().trim()
            val category = spinner.selectedItem.toString()

            if (title.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "Please fill in title and URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent double click
            btnSave.isEnabled = false
            btnSaveText.text = "SAVING..."
            progressBar.visibility = View.VISIBLE

            if (selectedImageUri != null) {
                saveVideoWithBase64(title, desc, url, category)
            } else {
                saveVideoToDatabase(title, desc, url, category, null)
            }
        }
    }

    private fun saveVideoWithBase64(title: String, desc: String, url: String, category: String) {
        try {
            selectedImageUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val base64Image = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
                
                saveVideoToDatabase(title, desc, url, category, base64Image)
            } ?: run {
                saveVideoToDatabase(title, desc, url, category, null)
            }
        } catch (e: Exception) {
            btnSave.isEnabled = true
            btnSaveText.text = "PUBLISH CONTENT"
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Image Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveVideoToDatabase(title: String, desc: String, url: String, category: String, thumbnailUrl: String?) {
        val finalThumbnail = thumbnailUrl ?: existingThumbnail
        
        val videoData = hashMapOf(
            "title" to title,
            "description" to desc,
            "url" to url,
            "category" to category,
            "thumbnailUrl" to finalThumbnail,
            "timestamp" to Timestamp.now()
        )

        if (videoId != null) {
            // Update existing video
            db.collection("videos").document(videoId!!)
                .set(videoData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Video updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSave.isEnabled = true
                    btnSaveText.text = "UPDATE VIDEO"
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Update Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new video
            db.collection("videos")
                .add(videoData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Video '$title' added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSave.isEnabled = true
                    btnSaveText.text = "PUBLISH CONTENT"
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

package com.example.mindmile.admin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class admin_edit_card : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var editImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSave: Button

    private var docId: String? = null
    private val PICK_IMAGE = 100
    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_edit_card)

        db = FirebaseFirestore.getInstance()

        editTitle = findViewById(R.id.editTitle)
        editDescription = findViewById(R.id.editDescription)
        editImage = findViewById(R.id.editImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSave)

        docId = intent.getStringExtra("docId")
        if (docId == null) {
            Toast.makeText(this, "Invalid habit card", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadHabitDetails()

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadHabitDetails() {
        db.collection("habit_cards").document(docId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    editTitle.setText(document.getString("title") ?: "")
                    editDescription.setText(document.getString("description") ?: "")

                    val imageBase64 = document.getString("imageBase64")
                    if (!imageBase64.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            editImage.setImageBitmap(bitmap)
                            selectedBitmap = bitmap  // <- set selectedBitmap immediately
                        } catch (e: Exception) {
                            editImage.setImageResource(R.drawable.ic_launcher_background)
                        }
                    } else {
                        editImage.setImageResource(R.drawable.ic_launcher_background)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load habit details", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                editImage.setImageURI(imageUri)
                val drawable = editImage.drawable
                if (drawable is BitmapDrawable) {
                    selectedBitmap = drawable.bitmap
                }
            }
        }
    }

    private fun saveChanges() {
        val title = editTitle.text.toString().trim()
        val description = editDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert selectedBitmap to Base64
        var imageBase64: String? = null
        selectedBitmap?.let {
            val baos = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bytes = baos.toByteArray()
            imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        }

        val updateMap = hashMapOf<String, Any>(
            "title" to title,
            "description" to description
        )
        imageBase64?.let { updateMap["imageBase64"] = it }

        db.collection("habit_cards").document(docId!!)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Habit updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update habit", Toast.LENGTH_SHORT).show()
            }
    }
}

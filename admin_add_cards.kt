package com.example.mindmile.admin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class admin_add_cards : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var titleInput: EditText
    private lateinit var descInput: EditText
    private lateinit var selectImageBtn: Button
    private lateinit var addCardBtn: Button
    private lateinit var showCardBtn: Button
    private lateinit var imageView: ImageView

    private var imageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                imageView.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_add_cards)

        db = FirebaseFirestore.getInstance()

        titleInput = findViewById(R.id.habit_title_input)
        descInput = findViewById(R.id.habit_desc_input)
        selectImageBtn = findViewById(R.id.select_image_btn)
        addCardBtn = findViewById(R.id.add_card_btn)
        showCardBtn = findViewById(R.id.show_card_btn)
        imageView = findViewById(R.id.habit_image_preview)

        selectImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        addCardBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val desc = descInput.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveCardWithBase64(title, desc)
        }
        showCardBtn.setOnClickListener {
            val intent = Intent(this, admin_card_show::class.java)
            startActivity(intent)
        }
    }

    private fun saveCardWithBase64(title: String, desc: String) {
        // Convert imageView to Base64
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)

        val imageBytes = stream.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val habitCard = hashMapOf(
            "title" to title,
            "description" to desc,
            "imageBase64" to base64Image,
            "createdAt" to Timestamp.now()
        )

        db.collection("habit_cards")
            .add(habitCard)
            .addOnSuccessListener {
                Toast.makeText(this, "Habit Card Added Successfully!", Toast.LENGTH_SHORT).show()
                titleInput.text.clear()
                descInput.text.clear()
                imageView.setImageResource(0)
                imageUri = null
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

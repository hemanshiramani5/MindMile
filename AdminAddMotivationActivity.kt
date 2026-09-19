package com.example.mindmile.admin

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AdminAddMotivationActivity : AppCompatActivity()
{
    private lateinit var db: FirebaseFirestore
    private lateinit var etQuoteText: EditText
    private lateinit var etQuoteAuthor: EditText
    private lateinit var ivQuoteBackground: ImageView
    private var quoteId: String? = null
    private var existingImageBase64: String? = null
    private var imageSelected = false

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                ivQuoteBackground.setImageURI(it)
                imageSelected = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_motivation)

        db = FirebaseFirestore.getInstance()

        etQuoteText = findViewById(R.id.etQuoteText)
        etQuoteAuthor = findViewById(R.id.etQuoteAuthor)
        ivQuoteBackground = findViewById(R.id.ivQuoteBackground)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSelectImage).setOnClickListener { pickImage.launch("image/*") }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveQuote() }

        // Check for Edit Mode
        quoteId = intent.getStringExtra("quoteId")
        if (quoteId != null) {
            etQuoteText.setText(intent.getStringExtra("text"))
            etQuoteAuthor.setText(intent.getStringExtra("author"))
            existingImageBase64 = intent.getStringExtra("imageBase64")
            
            if (existingImageBase64 != null) {
                try {
                    val bytes = Base64.decode(existingImageBase64, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ivQuoteBackground.setImageBitmap(bitmap)
                    imageSelected = true
                } catch (e: Exception) {
                    ivQuoteBackground.setImageResource(R.drawable.ic_image_placeholder)
                }
            }
            findViewById<Button>(R.id.btnSave).text = "Update Quote"
        }
    }

    private fun saveQuote() {
        val text = etQuoteText.text.toString().trim()
        val author = etQuoteAuthor.text.toString().trim()

        if (text.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!imageSelected && existingImageBase64 == null) {
             Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
             return
        }

        // Convert image to Base64 with resizing only if new image selected or needed
        // If image wasn't changed, we can use existingBase64 if available, 
        // BUT determining if it was changed vs just setting initial bitmap is tricky without flag.
        // We set imageSelected=true when prefilling. 
        
        // Simpler approach: verify if drawable is a bitmap, if so compress it.
        // If it's a placeholder resource, we fail validation above.

        val base64Image: String
        try {
            val drawable = ivQuoteBackground.drawable as? BitmapDrawable
            if (drawable != null) {
                val originalBitmap = drawable.bitmap
                val resizedBitmap = getResizedBitmap(originalBitmap, 800)
                val stream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                base64Image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
            } else if (existingImageBase64 != null) {
                base64Image = existingImageBase64!!
            } else {
                 Toast.makeText(this, "Image Error", Toast.LENGTH_SHORT).show()
                 return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
            return
        }

        val quoteData = hashMapOf(
            "text" to text,
            "author" to author,
            "imageBase64" to base64Image,
            "timestamp" to Timestamp.now()
        )

        if (quoteId != null) {
             db.collection("MotivationTable").document(quoteId!!)
                .update(quoteData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quote Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                     Toast.makeText(this, "Update Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("MotivationTable")
                .add(quoteData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quote Added", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}

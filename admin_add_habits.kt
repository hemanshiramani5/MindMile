package com.example.mindmile.admin

import android.content.Intent
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

class admin_add_habits : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etHabitName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etGoalValue: EditText
    private lateinit var spinnerGoalUnit: Spinner
    private lateinit var ivHabitImage: ImageView
    private var imageSelected = false

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                ivHabitImage.setImageURI(it)
                imageSelected = true
            }
        }

    private var habitId: String? = null  // For editing existing habit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_add_habits)

        db = FirebaseFirestore.getInstance()

        // 🔹 Views
        etHabitName = findViewById(R.id.etHabitName)
        etDescription = findViewById(R.id.etDescription)
        etGoalValue = findViewById(R.id.etGoalValue)
        spinnerGoalUnit = findViewById(R.id.spinnerGoalUnit)
        ivHabitImage = findViewById(R.id.ivHabitImage)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnSelectImage = findViewById<Button>(R.id.selectImageBtn)

        // 🔹 Spinner Data
        val units = arrayOf("steps", "minutes", "hours", "liters", "km", "times")
        spinnerGoalUnit.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, units)

        // 🔹 Intent Data for Editing
        habitId = intent.getStringExtra("habitId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val imageBase64 = intent.getStringExtra("image")

        etHabitName.setText(title)
        etDescription.setText(description)

        if (!imageBase64.isNullOrEmpty()) {
            ivHabitImage.setImageBitmap(base64ToBitmap(imageBase64))
            imageSelected = true
        } else {
            ivHabitImage.setImageResource(R.drawable.ic_image_placeholder)
        }

        if (!habitId.isNullOrEmpty()) {
            loadHabitFromDatabase(habitId!!)
        }

        // 🔹 Click Listeners
        btnBack.setOnClickListener { finish() }
        btnSelectImage.setOnClickListener { pickImage.launch("image/*") }
        btnSave.setOnClickListener { saveHabit() }
    }

    // 🔹 Save or Update Habit
    private fun saveHabit() {
        val title = etHabitName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val goalValueStr = etGoalValue.text.toString().trim()
        val goalUnit = spinnerGoalUnit.selectedItem.toString()

        if (title.isEmpty() || goalValueStr.isEmpty() || !imageSelected) {
            Toast.makeText(this, "Fill all fields and select image", Toast.LENGTH_SHORT).show()
            return
        }

        val goalValue = goalValueStr.toIntOrNull() ?: 1

        // 🔹 Convert image to Base64
        val bitmap = (ivHabitImage.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)

        val habitData = hashMapOf(
            "title" to title,
            "description" to description,
            "goalValue" to goalValue,
            "goalUnit" to goalUnit,
            "frequency" to "per day",
            "imageUrl" to base64Image,
            "timestamp" to Timestamp.now()
        )

        if (!habitId.isNullOrEmpty()) {
            // 🔹 Update existing habit
            db.collection("habits").document(habitId!!)
                .set(habitData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Habit Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            // 🔹 Create new habit
            db.collection("habits")
                .add(habitData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Habit Added", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 🔹 Load habit from Firestore
    private fun loadHabitFromDatabase(habitId: String) {
        db.collection("habits").document(habitId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etHabitName.setText(doc.getString("title"))
                    etDescription.setText(doc.getString("description"))
                    etGoalValue.setText(doc.getLong("goalValue")?.toString() ?: "1")
                    val unit = doc.getString("goalUnit")
                    val spinnerPosition = (spinnerGoalUnit.adapter as ArrayAdapter<String>).getPosition(unit)
                    spinnerGoalUnit.setSelection(spinnerPosition)
                    doc.getString("imageUrl")?.let {
                        ivHabitImage.setImageBitmap(base64ToBitmap(it))
                        imageSelected = true
                    }
                }
            }
    }

    // 🔹 Base64 → Bitmap
    private fun base64ToBitmap(base64: String): Bitmap {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

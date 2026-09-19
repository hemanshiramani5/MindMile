package com.example.mindmile.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore

class admin_show_habits : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var habitsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_show_habits)

        db = FirebaseFirestore.getInstance()
        habitsLayout = findViewById(R.id.habitsLayout)

        loadHabits()
    }

    @SuppressLint("MissingInflatedId")
    private fun loadHabits() {
        habitsLayout.removeAllViews()

        db.collection("habits")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val habitId = doc.id
                    val title = doc.getString("title") ?: "No Title"
                    val description = doc.getString("description") ?: ""
                    val imageBase64 = doc.getString("imageUrl")

                    val habitView = layoutInflater.inflate(R.layout.item_habit_card, null)

                    val tvTitle = habitView.findViewById<TextView>(R.id.tvHabitTitle)
                    val tvDescription = habitView.findViewById<TextView>(R.id.tvHabitDescription)
                    val ivImage = habitView.findViewById<ImageView>(R.id.ivHabitImage)
                    val btnEdit = habitView.findViewById<Button>(R.id.btnEdit)

                    tvTitle.text = title
                    tvDescription.text = description

                    if (!imageBase64.isNullOrEmpty()) {
                        ivImage.setImageBitmap(base64ToBitmap(imageBase64))
                    } else {
                        ivImage.setImageResource(R.drawable.ic_image_placeholder)
                    }

                    // 🔹 Edit Habit Click
                    btnEdit.setOnClickListener {
                        val intent = Intent(this, admin_add_habits::class.java)
                        intent.putExtra("habitId", habitId)
                        intent.putExtra("title", title)
                        intent.putExtra("description", description)
                        intent.putExtra("image", imageBase64)
                        startActivity(intent)
                    }

                    habitsLayout.addView(habitView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 Helper: Base64 → Bitmap
    private fun base64ToBitmap(base64: String) =
        android.graphics.BitmapFactory.decodeByteArray(
            Base64.decode(base64, Base64.DEFAULT),
            0,
            Base64.decode(base64, Base64.DEFAULT).size
        )
}

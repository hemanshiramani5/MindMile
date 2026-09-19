package com.example.mindmile.users

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.widget.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.example.mindmile.users.add_habits
import com.google.firebase.firestore.FirebaseFirestore

class show_habits : AppCompatActivity() {

    private lateinit var llHabitsContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_habits)

        llHabitsContainer = findViewById(R.id.llHabitsContainer)
        loadHabits()
    }

    private fun loadHabits() {
        db.collection("habits").get().addOnSuccessListener { result ->
            llHabitsContainer.removeAllViews()

            for (doc in result) {

                val habitId = doc.id
                val habitName = doc.getString("title") ?: "No Title"
                val habitDescription = doc.getString("description") ?: "No Description"
                val imageBase64 = doc.getString("imageUrl")

                // 🔹 Main Card
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(24, 24, 24, 24)
                    setBackgroundResource(R.drawable.card_bg)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 16 }
                }

                // 🔹 Image
                val image = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(140, 140).apply {
                        rightMargin = 16
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    if (!imageBase64.isNullOrBlank())
                        setImageBitmap(base64ToBitmap(imageBase64))
                    else
                        setImageResource(R.drawable.ic_image_placeholder)
                }

                // 🔹 Text Layout
                val habitTextLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                // 🔹 Habit Title
                val tvName = TextView(this).apply {
                    text = habitName
                    textSize = 18f
                    setTextColor(Color.BLACK)
                }

                // 🔹 Habit Description
                val tvDescription = TextView(this).apply {
                    text = habitDescription
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                }

                habitTextLayout.addView(tvName)
                habitTextLayout.addView(tvDescription)

                // 🔹 Heart+ Icon
                val ivHeart = ImageView(this).apply {
                    setImageResource(R.drawable.ic_heart_plus)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    card.setOnClickListener {
                        val intent = Intent(this@show_habits, add_habits::class.java)
                        intent.putExtra("habitId", habitId)
                        intent.putExtra("title", habitName)
                        intent.putExtra("description", habitDescription)
                        intent.putExtra("image", imageBase64)
                        startActivity(intent)
                    }
                }

                // 🔹 Add Views
                card.addView(image)
                card.addView(habitTextLayout)
                card.addView(ivHeart)

                llHabitsContainer.addView(card)
            }
        }
    }

    // 🔹 Base64 → Bitmap
    private fun base64ToBitmap(base64: String): Bitmap {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
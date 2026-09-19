package com.example.mindmile.users

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore

class HabitSuggestionListActivity : AppCompatActivity() {

    private lateinit var suggestionContainer: LinearLayout
    private lateinit var tvCategoryTitle: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_suggestion_list)

        suggestionContainer = findViewById(R.id.suggestionContainer)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val category = intent.getStringExtra("category") ?: "Suggestions"
        tvCategoryTitle.text = category

        loadHabitsByCategory(category)
    }

    private fun loadHabitsByCategory(category: String) {
        db.collection("habits")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { result ->
                suggestionContainer.removeAllViews()

                if (result.isEmpty) {
                    val noHabitsTv = TextView(this).apply {
                        text = "No habit suggestions found for this category."
                        gravity = Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    }
                    suggestionContainer.addView(noHabitsTv)
                    return@addOnSuccessListener
                }

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
                        
                        setOnClickListener {
                            val intent = Intent(this@HabitSuggestionListActivity, add_habits::class.java)
                            intent.putExtra("habitId", habitId)
                            intent.putExtra("title", habitName)
                            intent.putExtra("description", habitDescription)
                            intent.putExtra("image", imageBase64)
                            startActivity(intent)
                        }
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
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val tvName = TextView(this).apply {
                        text = habitName
                        textSize = 18f
                        setTextColor(Color.BLACK)
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }

                    val tvDescription = TextView(this).apply {
                        text = habitDescription
                        textSize = 14f
                        setTextColor(Color.DKGRAY)
                    }

                    habitTextLayout.addView(tvName)
                    habitTextLayout.addView(tvDescription)

                    val ivHeart = ImageView(this).apply {
                        setImageResource(R.drawable.ic_heart_plus)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    card.addView(image)
                    card.addView(habitTextLayout)
                    card.addView(ivHeart)

                    suggestionContainer.addView(card)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun base64ToBitmap(base64: String): android.graphics.Bitmap {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

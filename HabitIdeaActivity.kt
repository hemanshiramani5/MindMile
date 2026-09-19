package com.example.mindmile.users

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64
import com.example.mindmile.R


class HabitIdeaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var gridLayout: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_idea)

        db = FirebaseFirestore.getInstance()
        gridLayout = findViewById(R.id.gridLayout)

        loadHabitCards()
    }

    private fun loadHabitCards() {
        db.collection("habit_cards")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val title = document.getString("title") ?: "Habit"
                    val description = document.getString("description") ?: ""
                    val imageBase64 = document.getString("imageBase64") ?: ""

                    val card = layoutInflater.inflate(R.layout.card_item, gridLayout, false) as LinearLayout


                    val img = card.findViewById<ImageView>(R.id.cardImage)
                    val titleTv = card.findViewById<TextView>(R.id.cardTitle)
                    val descTv = card.findViewById<TextView>(R.id.cardDesc)

                    titleTv.text = title
                    descTv.text = description

                    if (imageBase64.isNotEmpty()) {
                        val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                        Glide.with(this).asBitmap().load(bytes).into(img)
                    }

                    gridLayout.addView(card)
                }
            }

            .addOnFailureListener {
                Toast.makeText(this, "Failed to load habit cards", Toast.LENGTH_SHORT).show()
            }
    }
}

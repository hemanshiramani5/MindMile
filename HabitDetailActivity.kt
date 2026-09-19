package com.example.mindmile.users

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R

class HabitDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_details)

        val tvTitle = findViewById<TextView>(R.id.tvHabitTitle)
        val tvDescription = findViewById<TextView>(R.id.tvHabitDescription)
        val ivImage = findViewById<ImageView>(R.id.ivHabitImage)

        // Get data from Intent
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val imageBase64 = intent.getStringExtra("image")

        tvTitle.text = title
        tvDescription.text = description

        if (!imageBase64.isNullOrEmpty()) {
            val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
            ivImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        }
    }
}

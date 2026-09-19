package com.example.mindmile.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mindmile.R

class admin_habits : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_habits)

        val addHabitCard = findViewById<CardView>(R.id.manageHabitCard)
        val addVideoCard = findViewById<CardView>(R.id.viewUsersCard)

        addHabitCard.setOnClickListener {
            startActivity(Intent(this, admin_add_habits::class.java))
        }

        addVideoCard.setOnClickListener {
            startActivity(Intent(this, admin_show_habits::class.java))
        }
    }
}

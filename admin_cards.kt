package com.example.mindmile.admin

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R

class admin_cards : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_cards) // <-- your XML filename

        // Cards
        val addHabitCard = findViewById<LinearLayout>(R.id.manageHabitCard)
        val addVideoCard = findViewById<LinearLayout>(R.id.viewUsersCard)

        // Click: Add Habit Cards
        addHabitCard.setOnClickListener {
            startActivity(
                Intent(this, admin_add_cards::class.java)
            )
        }

    }
}

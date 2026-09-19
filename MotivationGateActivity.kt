package com.example.mindmile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.Login
import com.example.mindmile.R
import com.google.android.material.textfield.TextInputEditText

class MotivationGateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation_gate)

        val btnGetStarted: Button = findViewById(R.id.btnGetStarted)
        val editMotivation: TextInputEditText = findViewById(R.id.editMotivation)

        btnGetStarted.setOnClickListener {
            val motivation = editMotivation.text.toString().trim()
            if (motivation.isEmpty()) {
                editMotivation.error = "Please tell us what motivates you"
            } else {
                // Save that we've seen this screen
                val sharedPref = getSharedPreferences("MindMilePrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putBoolean("motivation_seen", true).apply()

                // Move to login
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }
    }
}

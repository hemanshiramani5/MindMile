package com.example.mindmile.users

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mindmile.R

class CounselingHubActivity : AppCompatActivity() {

    private var currentMood: String = "Neutral"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counseling_hub)

        currentMood = intent.getStringExtra("MOOD") ?: "Neutral"

        val backButton = findViewById<View>(R.id.backButton)
        val cardSpeakers = findViewById<CardView>(R.id.cardSpeakers)
        val cardChat = findViewById<CardView>(R.id.cardChat)

        backButton.setOnClickListener { 
            finish()
            overridePendingTransition(R.anim.fade_out_scale, R.anim.fade_in_scale)
        }

        cardSpeakers.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("CATEGORY", "Motivational Speakers")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }

        cardChat.setOnClickListener {
            val intent = Intent(this, CounselorsActivity::class.java)
            intent.putExtra("MOOD", currentMood)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_scale, R.anim.fade_out_scale)
        }
    }
}

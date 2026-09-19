package com.example.mindmile.users

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R

class CounselorProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counselor_profile)

        val name = intent.getStringExtra("NAME") ?: "Dr. Counselor"
        val specialty = intent.getStringExtra("SPECIALTY") ?: "General Help"
        val quote = intent.getStringExtra("QUOTE") ?: "Everything will be okay."
        val imageRes = intent.getIntExtra("IMAGE_RES", R.drawable.ic_profile)

        findViewById<TextView>(R.id.profileName).text = name
        findViewById<TextView>(R.id.profileSpecialty).text = specialty
        findViewById<TextView>(R.id.profileQuote).text = "\"$quote\""
        findViewById<ImageView>(R.id.profileImage).setImageResource(imageRes)

        val mood = intent.getStringExtra("MOOD") ?: "Neutral"

        findViewById<Button>(R.id.btnStartChat).setOnClickListener {
            val chatIntent = Intent(this, ChatActivity::class.java)
            chatIntent.putExtra("COUNSELOR_NAME", name)
            chatIntent.putExtra("COUNSELOR_SPECIALTY", specialty)
            chatIntent.putExtra("MOOD", mood)
            startActivity(chatIntent)
        }
    }
}

package com.example.mindmile.users

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.Login
import com.example.mindmile.R
import com.google.firebase.auth.FirebaseAuth

class Settings : AppCompatActivity() {

    private lateinit var logoutSection: LinearLayout
    private lateinit var accountSection: LinearLayout
    private lateinit var notiSection: LinearLayout
    private lateinit var privacySection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        logoutSection = findViewById(R.id.logoutSection)
        accountSection = findViewById(R.id.accountSection)
        notiSection = findViewById(R.id.notiSection)
        privacySection = findViewById(R.id.privacySection)

        accountSection.setOnClickListener {
            Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show()
        }

        notiSection.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        privacySection.setOnClickListener {
            Toast.makeText(this, "Privacy clicked", Toast.LENGTH_SHORT).show()
        }

        logoutSection.setOnClickListener {
            // 1️⃣ Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // 2️⃣ Clear saved login state
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // 3️⃣ Notify user
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // 4️⃣ Redirect to Login screen
            startActivity(Intent(this, Login::class.java))
            finish()
        }

    }
}

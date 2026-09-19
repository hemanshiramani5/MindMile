package com.example.mindmile.admin

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.Login
import com.example.mindmile.R
import com.google.firebase.auth.FirebaseAuth

class AdminSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_settings)

        val btnBack = findViewById<android.view.View>(R.id.btnBack)
        val adminProfileSection = findViewById<LinearLayout>(R.id.adminProfileSection)
        val logoutSection = findViewById<LinearLayout>(R.id.logoutSection)

        btnBack.setOnClickListener {
            finish()
        }

        adminProfileSection.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }

        logoutSection.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

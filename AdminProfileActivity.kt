package com.example.mindmile.admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvAdminName = findViewById<TextView>(R.id.tvAdminName)
        val tvAdminEmail = findViewById<TextView>(R.id.tvAdminEmail)

        btnBack.setOnClickListener {
            finish()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val email = currentUser.email
            tvAdminEmail.text = email ?: "No Email"

            // Fetch Admin Details from Firestore
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("username") ?: document.getString("name") ?: "Admin"
                        tvAdminName.text = name
                    } else {
                        tvAdminName.text = "Admin User"
                    }
                }
                .addOnFailureListener {
                    tvAdminName.text = "Error Loading Name"
                }
        }
    }
}

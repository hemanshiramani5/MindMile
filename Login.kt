package com.example.mindmile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.admin.admin_dashboard
import com.example.mindmile.users.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ------------------ AUTO-LOGIN CHECK ------------------
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val isAdmin = sharedPref.getBoolean("isAdmin", false)

        if (isLoggedIn) {
            val targetActivity = if (isAdmin) admin_dashboard::class.java else MainActivity::class.java
            startActivity(Intent(this, targetActivity))
            finish() // close login screen
            return
        }

        // ------------------ LOAD LOGIN LAYOUT ------------------
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val registerLink = findViewById<TextView>(R.id.register_link)

        // ------------------ LOGIN BUTTON ------------------
        loginBtn.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }

                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        Toast.makeText(this, "Login failed: user is null", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val uid = currentUser.uid

                    db.collection("Admin")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            val editor = sharedPref.edit()
                            editor.putBoolean("isLoggedIn", true) // Save login state

                            if (doc.exists()) {
                                editor.putBoolean("isAdmin", true)
                                editor.apply()
                                Toast.makeText(this, "Admin Login!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, admin_dashboard::class.java))
                            } else {
                                editor.putBoolean("isAdmin", false)
                                editor.apply()
                                Toast.makeText(this, "User Login!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
        }

        // ------------------ REGISTER LINK ------------------
        registerLink.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
        }
    }
}
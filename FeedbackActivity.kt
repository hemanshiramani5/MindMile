package com.example.mindmile.users

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val etFeedback = findViewById<EditText>(R.id.etFeedback)
        val btnSend = findViewById<MaterialButton>(R.id.btnSend)
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener { finish() }

        btnSend.setOnClickListener {
            val feedbackText = etFeedback.text.toString().trim()
            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendFeedback(feedbackText)
        }
    }

    private fun sendFeedback(message: String) {
        val currentUser = auth.currentUser ?: return
        val feedbackData = hashMapOf(
            "userId" to currentUser.uid,
            "userEmail" to (currentUser.email ?: "Anonymous"),
            "message" to message,
            "timestamp" to Timestamp.now()
        )

        db.collection("feedbacks").add(feedbackData)
            .addOnSuccessListener {
                Toast.makeText(this, "Feedback sent! Thank you!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

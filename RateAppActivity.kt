package com.example.mindmile.users

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RateAppActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ratingBar: RatingBar
    private lateinit var etComment: EditText
    private lateinit var btnSubmit: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_app)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ratingBar = findViewById(R.id.ratingBar)
        etComment = findViewById(R.id.etRatingComment)
        btnSubmit = findViewById(R.id.btnSubmitRating)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            submitRating()
        }
    }

    private fun submitRating() {
        val rating = ratingBar.rating
        val comment = etComment.text.toString().trim()
        val currentUser = auth.currentUser

        if (rating == 0f) {
            Toast.makeText(this, "Please select at least one star", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."

        val ratingData = hashMapOf(
            "userId" to currentUser.uid,
            "userEmail" to (currentUser.email ?: "Anonymous"),
            "rating" to rating,
            "comment" to comment,
            "timestamp" to Timestamp.now()
        )

        db.collection("app_ratings")
            .add(ratingData)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your rating! 🎉", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Rating"
                Toast.makeText(this, "Failed to submit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

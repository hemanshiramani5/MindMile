package com.example.mindmile.users

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.databinding.ActivityMoodSelectionBinding
import com.example.mindmile.model.UserMood
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MoodSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodSelectionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Close button logic
        binding.btnClose.setOnClickListener {
            finish()
        }

        // Mood click listeners
        binding.moodAwful.setOnClickListener { saveMoodAndFinish("ic_mood_awful") }
        binding.moodExcellent.setOnClickListener { saveMoodAndFinish("ic_mood_excellent") }
        binding.moodBad.setOnClickListener { saveMoodAndFinish("ic_mood_bad") }
        binding.moodGreat.setOnClickListener { saveMoodAndFinish("ic_mood_great") }
        binding.moodPoor.setOnClickListener { saveMoodAndFinish("ic_mood_poor") }
        binding.moodGood.setOnClickListener { saveMoodAndFinish("ic_mood_good") }
        binding.moodNeutral.setOnClickListener { saveMoodAndFinish("ic_mood_neutral") }
    }

    private fun saveMoodAndFinish(moodDrawableName: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to save mood", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateKey = dateFormat.format(Calendar.getInstance().time)
        val timestamp = Timestamp.now()

        // 1. Save locally for immediate UI update in MainActivity
        val sharedPref = getSharedPreferences("MindMilePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("mood_$dateKey", moodDrawableName)
            apply()
        }

        // 2. Save to Firestore for Admin and Sync
        // Use HashMap for maximum reliability (avoids POJO mapping issues)
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("username") ?: document.getString("name") 
                    ?: document.getString("fullName") ?: "Unknown User"
                val email = document.getString("email") ?: currentUser.email ?: "No Email"

                val moodData = hashMapOf(
                    "moodId" to UUID.randomUUID().toString(),
                    "userId" to currentUser.uid,
                    "userName" to name,
                    "userEmail" to email,
                    "moodDrawableName" to moodDrawableName,
                    "timestamp" to timestamp,
                    "date" to dateKey
                )

                db.collection("mood_records").add(moodData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Mood synced to cloud!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Cloud Error: ${e.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
            }
            .addOnFailureListener {
                // If we can't get name, use email/id
                val moodData = hashMapOf(
                    "moodId" to UUID.randomUUID().toString(),
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.email ?: "User"),
                    "userEmail" to (currentUser.email ?: "No Email"),
                    "moodDrawableName" to moodDrawableName,
                    "timestamp" to timestamp,
                    "date" to dateKey
                )
                db.collection("mood_records").add(moodData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Mood synced (profile fetch failed)", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Cloud Error (Final): ${e.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
            }
    }
}

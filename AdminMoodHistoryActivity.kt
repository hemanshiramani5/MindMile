package com.example.mindmile.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindmile.databinding.ActivityAdminMoodHistoryBinding
import com.example.mindmile.model.UserMood
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminMoodHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMoodHistoryBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMoodHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSeed.setOnClickListener {
            seedInitialRecord()
        }

        setupRecyclerView()
        loadMoodRecords()
    }

    private fun seedInitialRecord() {
        val seedData = hashMapOf(
            "moodId" to "seed_test",
            "userId" to "admin_seed",
            "userName" to "Admin (System)",
            "userEmail" to "admin@mindmile.com",
            "moodDrawableName" to "ic_mood_excellent",
            "timestamp" to com.google.firebase.Timestamp.now(),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        )

        binding.btnSeed.isEnabled = false
        binding.btnSeed.text = "Initializing..."

        db.collection("mood_records").add(seedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Table successfully created/initialized!", Toast.LENGTH_LONG).show()
                loadMoodRecords() // Refresh view
            }
            .addOnFailureListener { e ->
                binding.btnSeed.isEnabled = true
                binding.btnSeed.text = "Initialize Cloud Table"
                Toast.makeText(this, "Initialize Error: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("AdminSeed", "Error: ${e.message}")
            }
    }

    private fun setupRecyclerView() {
        binding.rvAdminMoodHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMoodRecords() {
        // Fetch ALL records without orderBy to avoid needing an index immediately
        // We will sort them locally in the processResult function
        db.collection("mood_records")
            .get()
            .addOnSuccessListener { result ->
                processResult(result)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fetch Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.tvEmpty.text = "Connection Error: ${e.message}"
                binding.tvEmpty.visibility = View.VISIBLE
            }
    }

    private fun processResult(result: com.google.firebase.firestore.QuerySnapshot) {
        val allMoods = mutableListOf<UserMood>()
        
        if (result.isEmpty) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text = "Cloud Table 'mood_records' is not created yet.\nClick below to initialize it!"
            binding.btnSeed.visibility = View.VISIBLE
            binding.rvAdminMoodHistory.visibility = View.GONE
            return
        }

        for (document in result) {
            try {
                val mood = UserMood(
                    moodId = document.getString("moodId") ?: "",
                    userId = document.getString("userId") ?: "",
                    userName = document.getString("userName") ?: "User",
                    userEmail = document.getString("userEmail") ?: "No Email",
                    moodDrawableName = document.getString("moodDrawableName") ?: "ic_emoji",
                    timestamp = document.getTimestamp("timestamp"),
                    date = document.getString("date") ?: ""
                )
                allMoods.add(mood)
            } catch (e: Exception) {
                android.util.Log.e("AdminMood", "Mapping Error: ${e.message}")
            }
        }

        // --- GROUPING LOGIC: Keep only the LAST mood for each USER ---
        val userLastMoods = allMoods
            .groupBy { it.userId } // Group by the unique user ID
            .mapValues { entry -> 
                // For each user, find the one with the highest timestamp
                entry.value.maxByOrNull { it.timestamp?.seconds ?: 0L }
            }
            .values
            .filterNotNull()
            .sortedByDescending { it.timestamp?.seconds ?: 0L } // Show most recent users at top

        binding.tvEmpty.visibility = View.GONE
        binding.btnSeed.visibility = View.GONE
        binding.rvAdminMoodHistory.visibility = View.VISIBLE
        binding.rvAdminMoodHistory.adapter = AdminMoodAdapter(userLastMoods)

        // Inform admin how many unique users were found
        Toast.makeText(this, "Showing last moods for ${userLastMoods.size} users", Toast.LENGTH_SHORT).show()
    }
}

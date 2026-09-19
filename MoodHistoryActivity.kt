package com.example.mindmile.users

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindmile.databinding.ActivityMoodHistoryBinding

class MoodHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            loadLocalHistory()
            return
        }

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("mood_records")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val moodList = mutableListOf<MoodRecord>()
                for (document in result) {
                    val date = document.getString("date") ?: ""
                    val icon = document.getString("moodDrawableName") ?: "ic_emoji"
                    val timestamp = document.getTimestamp("timestamp")
                    
                    // Convert Firestore data to local MoodRecord model
                    moodList.add(MoodRecord(date, icon, timestamp))
                }
                
                // Sort by timestamp if available, else date
                moodList.sortWith(compareByDescending<MoodRecord> { it.timestamp?.seconds ?: 0L }.thenByDescending { it.date })

                if (moodList.isEmpty()) {
                    loadLocalHistory() // Fallback to local if Cloud is empty
                } else {
                    displayList(moodList)
                }
            }
            .addOnFailureListener {
                loadLocalHistory()
            }
    }

    private fun loadLocalHistory() {
        val sharedPref = getSharedPreferences("MindMilePrefs", MODE_PRIVATE)
        val allEntries = sharedPref.all
        val moodRecords = mutableListOf<MoodRecord>()

        for ((key, value) in allEntries) {
            if (key.startsWith("mood_") && value is String) {
                val date = key.replace("mood_", "")
                moodRecords.add(MoodRecord(date, value))
            }
        }

        moodRecords.sortByDescending { it.date }

        if (moodRecords.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvMoodHistory.visibility = View.GONE
        } else {
            displayList(moodRecords)
        }
    }

    private fun displayList(list: List<MoodRecord>) {
        binding.tvEmpty.visibility = View.GONE
        binding.rvMoodHistory.visibility = View.VISIBLE
        binding.rvMoodHistory.layoutManager = LinearLayoutManager(this)
        binding.rvMoodHistory.adapter = MoodRecordAdapter(list)
    }
}

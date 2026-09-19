package com.example.mindmile.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class AdminFeedbackActivity : AppCompatActivity() {

    data class FeedbackItem(
        val userEmail: String,
        val message: String,
        val timestamp: com.google.firebase.Timestamp
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var progressBar: ProgressBar
    private val feedbackList = mutableListOf<FeedbackItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_feedback)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rvFeedbacks)
        emptyState = findViewById(R.id.emptyState)
        progressBar = findViewById(R.id.progressBar)
        
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadFeedbacks()
    }

    private fun loadFeedbacks() {
        progressBar.visibility = View.VISIBLE
        db.collection("feedbacks")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                feedbackList.clear()
                for (doc in result) {
                    val email = doc.getString("userEmail") ?: "Anonymous"
                    val msg = doc.getString("message") ?: ""
                    val time = doc.getTimestamp("timestamp") ?: com.google.firebase.Timestamp.now()
                    feedbackList.add(FeedbackItem(email, msg, time))
                }

                if (feedbackList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = FeedbackAdapter(feedbackList)
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            }
    }

    inner class FeedbackAdapter(private val list: List<FeedbackItem>) :
        RecyclerView.Adapter<FeedbackAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val email: TextView = view.findViewById(R.id.tvUserEmail)
            val message: TextView = view.findViewById(R.id.tvMessage)
            val date: TextView = view.findViewById(R.id.tvTimestamp)

            fun bind(item: FeedbackItem) {
                email.text = item.userEmail
                message.text = item.message
                val dateDate = item.timestamp.toDate()
                val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                date.text = dateFormat.format(dateDate)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feedback, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}

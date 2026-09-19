package com.example.mindmile.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class AdminRatingsActivity : AppCompatActivity() {

    data class RatingItem(
        val userEmail: String,
        val comment: String,
        val rating: Float,
        val timestamp: com.google.firebase.Timestamp
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var progressBar: ProgressBar
    private val ratingsList = mutableListOf<RatingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_ratings)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rvRatings)
        emptyState = findViewById(R.id.emptyState)
        progressBar = findViewById(R.id.progressBar)
        
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadRatings()
    }

    private fun loadRatings() {
        progressBar.visibility = View.VISIBLE
        db.collection("app_ratings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                ratingsList.clear()
                for (doc in result) {
                    val email = doc.getString("userEmail") ?: "Anonymous"
                    val comment = doc.getString("comment") ?: ""
                    val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                    val time = doc.getTimestamp("timestamp") ?: com.google.firebase.Timestamp.now()
                    ratingsList.add(RatingItem(email, comment, rating, time))
                }

                if (ratingsList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = RatingsAdapter(ratingsList)
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            }
    }

    inner class RatingsAdapter(private val list: List<RatingItem>) :
        RecyclerView.Adapter<RatingsAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val email: TextView = view.findViewById(R.id.tvUserEmail)
            val comment: TextView = view.findViewById(R.id.tvComment)
            val ratingBar: RatingBar = view.findViewById(R.id.ratingBarSmall)
            val date: TextView = view.findViewById(R.id.tvTimestamp)

            fun bind(item: RatingItem) {
                email.text = item.userEmail
                comment.text = item.comment
                ratingBar.rating = item.rating
                
                val dateDate = item.timestamp.toDate()
                val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                date.text = dateFormat.format(dateDate)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rating, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}

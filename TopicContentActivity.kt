package com.example.mindmile.users

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.example.mindmile.utils.VideoUtils
import com.google.firebase.firestore.FirebaseFirestore

class TopicContentActivity : AppCompatActivity() {

    data class ContentItem(
        val title: String, 
        val quote: String, 
        val youtubeUrl: String,
        val thumbnailUrl: String? = null
    )

    private var topicName = "Motivation"
    private lateinit var db: FirebaseFirestore
    private val contentList = mutableListOf<ContentItem>()
    private lateinit var adapter: ContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_content)

        db = FirebaseFirestore.getInstance()
        topicName = intent.getStringExtra("TOPIC") ?: "Motivation"
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = topicName
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.contentRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ContentAdapter(contentList)
        recycler.adapter = adapter

        loadContent()
    }

    private fun loadContent() {
        contentList.clear()
        
        db.collection("videos")
            .whereEqualTo("category", topicName)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val title = doc.getString("title") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val url = doc.getString("url") ?: ""
                    val thumb = doc.getString("thumbnailUrl")
                    contentList.add(ContentItem(title, desc, url, thumb))
                }
                
                // If it's empty, and it was "Motivational Speakers", maybe also try "Speaker"
                if (contentList.isEmpty() && topicName == "Motivational Speakers") {
                    fetchMotivationalSpeakersFallback()
                } else {
                    adapter.notifyDataSetChanged()
                    if (contentList.isEmpty()) {
                        // Keep some defaults if needed? Or show a toast.
                        Toast.makeText(this, "No content found for $topicName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMotivationalSpeakersFallback() {
        db.collection("videos")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val category = doc.getString("category") ?: ""
                    if (category.contains("Speaker", ignoreCase = true)) {
                        val title = doc.getString("title") ?: ""
                        val desc = doc.getString("description") ?: ""
                        val url = doc.getString("url") ?: ""
                        val thumb = doc.getString("thumbnailUrl")
                        contentList.add(ContentItem(title, desc, url, thumb))
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    inner class ContentAdapter(private val list: List<ContentItem>) : RecyclerView.Adapter<ContentAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.textVideoTitle)
            val quote: TextView = view.findViewById(R.id.textQuote)
            val btnWatch: Button = view.findViewById(R.id.btnWatch)
            val thumbnailImg: ImageView = view.findViewById(R.id.imgThumbnail)
            val thumbnailContainer: View = thumbnailImg.parent as View

            fun bind(item: ContentItem) {
                title.text = item.title
                quote.text = "\"${item.quote}\""
                
                // Use Centralized VideoUtils for loading thumbnails
                VideoUtils.loadThumbnail(itemView.context, item.thumbnailUrl, item.youtubeUrl, thumbnailImg)
                
                val openVideo = View.OnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.youtubeUrl))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(itemView.context, "Cannot open video", Toast.LENGTH_SHORT).show()
                    }
                }

                btnWatch.setOnClickListener(openVideo)
                thumbnailContainer.setOnClickListener(openVideo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_motivation_content, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}

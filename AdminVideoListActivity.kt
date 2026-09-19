package com.example.mindmile.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class AdminVideoListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: AdminVideoAdapter
    private val videoList = mutableListOf<VideoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_video_list)

        db = FirebaseFirestore.getInstance()

        // Views
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val fabAddVideo = findViewById<FloatingActionButton>(R.id.fabAddVideo)
        recyclerView = findViewById(R.id.recyclerView)
        emptyState = findViewById(R.id.emptyState)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdminVideoAdapter(
            videoList,
            onEdit = { video -> editVideo(video) },
            onDelete = { video -> deleteVideo(video) }
        )
        recyclerView.adapter = adapter

        // Listeners
        btnBack.setOnClickListener { finish() }
        fabAddVideo.setOnClickListener {
            startActivity(Intent(this, AdminAddVideoActivity::class.java))
        }

        // Load videos
        loadVideos()
    }

    override fun onResume() {
        super.onResume()
        loadVideos() // Refresh list when returning from add/edit
    }

    private fun loadVideos() {
        db.collection("videos")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                videoList.clear()
                for (doc in documents) {
                    val video = VideoItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        url = doc.getString("url") ?: "",
                        category = doc.getString("category") ?: "",
                        thumbnailUrl = doc.getString("thumbnailUrl")
                    )
                    videoList.add(video)
                }
                adapter.notifyDataSetChanged()
                
                // Show/hide empty state
                if (videoList.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load videos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editVideo(video: VideoItem) {
        val intent = Intent(this, AdminAddVideoActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("title", video.title)
            putExtra("description", video.description)
            putExtra("url", video.url)
            putExtra("category", video.category)
            putExtra("thumbnailUrl", video.thumbnailUrl)
        }
        startActivity(intent)
    }

    private fun deleteVideo(video: VideoItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Video")
            .setMessage("Are you sure you want to delete \"${video.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("videos").document(video.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Video deleted", Toast.LENGTH_SHORT).show()
                        loadVideos()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val category: String,
    val thumbnailUrl: String?
)

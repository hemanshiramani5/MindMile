package com.example.mindmile.users

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindmile.R
import com.example.mindmile.utils.VideoUtils
import com.google.firebase.firestore.FirebaseFirestore

class VideoActivity : AppCompatActivity() {

    data class VideoContent(
        val title: String,
        val description: String,
        val resId: Int = 0,
        val thumbRes: Int = 0,
        val thumbnailUrl: String? = null,
        val category: String,
        val url: String? = null
    )

    private lateinit var videoView: VideoView
    private lateinit var playerContainer: FrameLayout
    private lateinit var galleryContainer: LinearLayout
    private lateinit var db: FirebaseFirestore
    private val videoList = mutableListOf<VideoContent>()
    private lateinit var adapter: VideoGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        db = FirebaseFirestore.getInstance()

        // Setup UI references
        videoView = findViewById(R.id.videoView)
        playerContainer = findViewById(R.id.playerContainer)
        galleryContainer = findViewById(R.id.galleryContainer)
        val closeButton = findViewById<ImageView>(R.id.btnClosePlayer)
        val recycler = findViewById<RecyclerView>(R.id.videoRecyclerView)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val toolbarTitleText = findViewById<TextView>(R.id.toolbarTitle)

        val categoryFilter = intent.getStringExtra("CATEGORY")
        if (!categoryFilter.isNullOrEmpty() && categoryFilter != "ALL") {
            toolbarTitleText.text = categoryFilter
        }

        backBtn.setOnClickListener { finish() }

        // Setup Recycler
        adapter = VideoGalleryAdapter(videoList) { video ->
            playVideo(video)
        }
        recycler.layoutManager = GridLayoutManager(this, 1)
        recycler.adapter = adapter

        closeButton.setOnClickListener {
            stopVideo()
        }

        // Fetch data
        loadVideos()
    }

    private fun loadVideos() {
        val categoryFilter = intent.getStringExtra("CATEGORY")
        
        videoList.clear()
        adapter.notifyDataSetChanged()

        // Fetch from Firestore
        db.collection("videos").get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val title = doc.getString("title") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val url = doc.getString("url") ?: ""
                    val category = doc.getString("category") ?: ""
                    val thumbnailUrl = doc.getString("thumbnailUrl")

                    // Flexible Filter logic: Show if category match or if filter is null/ALL
                    val matchesFilter = categoryFilter == null || 
                                      categoryFilter == "ALL" || 
                                      category.equals(categoryFilter, ignoreCase = true) ||
                                      (categoryFilter == "Motivational Speakers" && 
                                          (category.contains("Speaker", ignoreCase = true) || 
                                           category.equals("Counseling", ignoreCase = true) ||
                                           category.equals("Motivation", ignoreCase = true)))

                    if (matchesFilter) {
                        videoList.add(VideoContent(
                            title = title,
                            description = desc,
                            category = category,
                            url = url,
                            thumbnailUrl = thumbnailUrl
                        ))
                    }
                }
                adapter.notifyDataSetChanged()
                
                if (videoList.isEmpty()) {
                    Toast.makeText(this, "No videos found in $categoryFilter", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load remote videos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun playVideo(video: VideoContent) {
        if (!video.url.isNullOrEmpty()) {
            // Open external URL (YouTube/Web)
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Cannot open video link", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Internal Playback (for hardcoded or file resource if any)
        if (video.resId != 0) {
            galleryContainer.visibility = View.GONE
            playerContainer.visibility = View.VISIBLE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val uri = Uri.parse("android.resource://$packageName/${video.resId}")
            videoView.setVideoURI(uri)
            videoView.start()
            videoView.setOnCompletionListener { stopVideo() }
        }
    }

    private fun stopVideo() {
        videoView.stopPlayback()
        playerContainer.visibility = View.GONE
        galleryContainer.visibility = View.VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onBackPressed() {
        if (playerContainer.visibility == View.VISIBLE) {
            stopVideo()
        } else {
            super.onBackPressed()
        }
    }


    // Adapter Class
    inner class VideoGalleryAdapter(
        private val videos: List<VideoContent>,
        private val onClick: (VideoContent) -> Unit
    ) : RecyclerView.Adapter<VideoGalleryAdapter.VideoHolder>() {

        inner class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.textTitle)
            val desc: TextView = itemView.findViewById(R.id.textDescription)
            val thumb: ImageView = itemView.findViewById(R.id.videoThumbnail)
            val card: View = itemView.findViewById(R.id.cardVideo)
            val badge: TextView = itemView.findViewById(R.id.badgeCategory)
            val btnWatch: View = itemView.findViewById(R.id.btnWatchVideo)

            fun bind(video: VideoContent) {
                title.text = video.title
                desc.text = video.description
                badge.text = video.category.uppercase()
                
                // Use Centralized VideoUtils for loading thumbnails
                VideoUtils.loadThumbnail(itemView.context, video.thumbnailUrl, video.url, thumb)
                
                card.setOnClickListener { onClick(video) }
                btnWatch.setOnClickListener { onClick(video) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_gallery, parent, false)
            return VideoHolder(view)
        }

        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
            holder.bind(videos[position])
        }

        override fun getItemCount() = videos.size
    }
}

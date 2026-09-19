package com.example.mindmile.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindmile.R
import com.example.mindmile.utils.VideoUtils

class AdminVideoAdapter(
    private val videos: List<VideoItem>,
    private val onEdit: (VideoItem) -> Unit,
    private val onDelete: (VideoItem) -> Unit
) : RecyclerView.Adapter<AdminVideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.videoThumbnail)
        val title: TextView = view.findViewById(R.id.videoTitle)
        val category: TextView = view.findViewById(R.id.videoCategory)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        
        holder.title.text = video.title
        holder.category.text = video.category

        // Load thumbnail
        VideoUtils.loadThumbnail(
            holder.itemView.context,
            video.url,
            video.thumbnailUrl,
            holder.thumbnail
        )

        holder.btnEdit.setOnClickListener { onEdit(video) }
        holder.btnDelete.setOnClickListener { onDelete(video) }
    }

    override fun getItemCount() = videos.size
}

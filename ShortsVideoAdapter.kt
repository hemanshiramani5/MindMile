package com.example.mindmile.users

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView

class ShortsVideoAdapter(
    private val context: Context,
    private val videoItems: List<ShortsVideoActivity.VideoItem>
) : RecyclerView.Adapter<ShortsVideoAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video_short, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoItems[position])
    }

    override fun getItemCount(): Int = videoItems.size

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.play()
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.pause()
    }
    
    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.release()
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerView: StyledPlayerView = itemView.findViewById(R.id.playerView)
        private val titleText: TextView = itemView.findViewById(R.id.videoTitle)
        private val descText: TextView = itemView.findViewById(R.id.videoDescription)
        private val likeBtn: View = itemView.findViewById(R.id.likeButton)
        private val shareBtn: View = itemView.findViewById(R.id.btnShare)
        private var exoplayer: ExoPlayer? = null

        fun bind(item: ShortsVideoActivity.VideoItem) {
            titleText.text = item.title
            descText.text = item.description

            if (exoplayer == null) {
                exoplayer = ExoPlayer.Builder(context).build()
                playerView.player = exoplayer
            }

            val uri = Uri.parse("android.resource://${context.packageName}/${item.videoResId}")
            val mediaItem = MediaItem.fromUri(uri)
            
            exoplayer?.setMediaItem(mediaItem)
            exoplayer?.prepare()
            exoplayer?.repeatMode = Player.REPEAT_MODE_ONE

            likeBtn.setOnClickListener {
                // Future: Implement like logic
                android.widget.Toast.makeText(context, "Liked!", android.widget.Toast.LENGTH_SHORT).show()
            }

            shareBtn.setOnClickListener {
                // Future: Implement share logic
                android.widget.Toast.makeText(context, "Sharing...", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        fun play() {
            exoplayer?.playWhenReady = true
            // Start spinning animation
            val vinyl = itemView.findViewById<View>(R.id.vinylRecord)
            vinyl?.animate()?.rotationBy(360f)?.setDuration(4000)?.withEndAction {
                if(exoplayer?.playWhenReady == true) {
                    vinyl.animate().rotationBy(360f).setDuration(4000).start()
                }
            }?.start()
        }

        fun pause() {
            exoplayer?.playWhenReady = false
        }
        
        fun release() {
             exoplayer?.release()
             exoplayer = null
        }
    }
}

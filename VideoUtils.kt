package com.example.mindmile.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mindmile.R

object VideoUtils {

    fun getYouTubeVideoId(url: String): String? {
        val pattern = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:[\\w-]+\\.)+\\w+(?:\\/(?:[\\w-]+\\?v=|embed\\/|v\\/)?)([\\w-]{11})(?:\\S+)?$"
        val compiledPattern = java.util.regex.Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            val shortPattern = "youtu.be\\/([\\w-]{11})"
            val shortCompiled = java.util.regex.Pattern.compile(shortPattern)
            val shortMatcher = shortCompiled.matcher(url)
            if (shortMatcher.find()) shortMatcher.group(1) else null
        }
    }

    fun getYouTubeThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/0.jpg"
    }

    fun loadThumbnail(context: Context, thumbnailUrl: String?, videoUrl: String?, targetImageView: ImageView) {
        if (!thumbnailUrl.isNullOrEmpty()) {
            if (thumbnailUrl.startsWith("/") || thumbnailUrl.startsWith("http") || thumbnailUrl.startsWith("content")) {
                Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.bg_relax)
                    .centerCrop()
                    .into(targetImageView)
            } else {
                // Assume Base64
                try {
                    val imageBytes = Base64.decode(thumbnailUrl, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    targetImageView.setImageBitmap(decodedImage)
                    targetImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                } catch (e: Exception) {
                    targetImageView.setImageResource(R.drawable.bg_relax)
                }
            }
        } else if (!videoUrl.isNullOrEmpty()) {
            val videoId = getYouTubeVideoId(videoUrl)
            if (videoId != null) {
                Glide.with(context)
                    .load(getYouTubeThumbnailUrl(videoId))
                    .placeholder(R.drawable.bg_relax)
                    .centerCrop()
                    .into(targetImageView)
            } else {
                targetImageView.setImageResource(R.drawable.bg_relax)
            }
        } else {
            targetImageView.setImageResource(R.drawable.bg_relax)
        }
    }
}

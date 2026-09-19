package com.example.mindmile.users

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.mindmile.R

class ShortsVideoActivity : AppCompatActivity() {

    data class VideoItem(val title: String, val description: String, val videoResId: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        setContentView(R.layout.activity_shorts_video)

        val viewPager = findViewById<ViewPager2>(R.id.videosViewPager)
        val closeButton = findViewById<android.view.View>(R.id.closeButton)

        closeButton.setOnClickListener { finish() }

        // Get mood from intent
        val mood = intent.getStringExtra("MOOD") ?: "Neutral"

        // Generate video list based on mood or generic
        val videos = getVideoListForMood(mood)

        val adapter = ShortsVideoAdapter(this, videos)
        viewPager.adapter = adapter
        
        // Preload behavior? ViewPager2 handles offscreen pages differently.
        viewPager.offscreenPageLimit = 1
    }

    private fun getVideoListForMood(mood: String): List<VideoItem> {
        return when (mood) {
            "Happy" -> listOf(
                VideoItem("Pure Joy", "Let this music lift your spirits even higher!", R.raw.happy_video),
                VideoItem("Start Your Day", "A beautiful morning starts with a smile.", R.raw.morning_sun),
                VideoItem("Dance Like Nobody's Watching", "Feel the rhythm and keep shining!", R.raw.dance_party)
            )
            "Sad" -> listOf(
                VideoItem("It's Okay Not to Be Okay", "Take a moment to breathe and be kind to yourself.", R.raw.sad_video),
                VideoItem("Nature's Embrace", "Find peace in the gentle sounds of the forest.", R.raw.calm_nature),
                VideoItem("Let it Rain", "Sometimes, a rainy day is exactly what we need for reflection.", R.raw.relaxing_rain)
            )
            "Angry" -> listOf(
                VideoItem("Release the Tension", "Focus your energy and find your inner strength.", R.raw.angry_video),
                VideoItem("Focus Your Mind", "Channel that energy into productivity.", R.raw.focus_flow),
                VideoItem("Calm the Storm", "Deep breaths. This too shall pass.", R.raw.calm_nature)
            )
            "Sleepy" -> listOf(
                VideoItem("Restful Nights", "Gentle sounds to help you drift into a peaceful sleep.", R.raw.sleepy_video),
                VideoItem("Soft Rain", "The perfect background for a cozy nap.", R.raw.relaxing_rain),
                VideoItem("Starlight Calm", "Quiet your mind and prepare for rest.", R.raw.calm_nature)
            )
            "SPEAKERS", "Motivation" -> listOf(
                VideoItem("Unlocking Greatness", "Discover what drives you to succeed.", R.raw.motivation_1),
                VideoItem("The Power of Now", "Focus on the present to build your future.", R.raw.focus_flow),
                VideoItem("Stay Resilient", "Challenges are just opportunities in disguise.", R.raw.neutral_video)
            )
            else -> listOf(
                VideoItem("Just for You", "A moment of calm in your busy day.", R.raw.neutral_video),
                VideoItem("Nature's Beauty", "A quick reset for your mind and soul.", R.raw.calm_nature),
                VideoItem("Stay Positive", "You've got this, no matter what.", R.raw.morning_sun)
            )
        }
    }
}

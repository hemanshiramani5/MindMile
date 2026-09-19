package com.example.mindmile.users

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.view.Gravity
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import kotlin.random.Random

class HappyGameActivity : AppCompatActivity() {

    private lateinit var gameArea: FrameLayout
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var catcher: TextView
    
    private val handler = Handler(Looper.getMainLooper())
    private var score = 0
    private var timerRunning = false
    private val gameTime = 30000L
    
    private val activeAnimators = mutableListOf<Animator>()
    private var mediaTap: MediaPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_game)

        gameArea = findViewById(R.id.gameArea)
        scoreText = findViewById(R.id.scoreText)
        timerText = findViewById(R.id.timerText)

        setupCatcher()
        mediaTap = MediaPlayer.create(this, R.raw.tap_sound)

        gameArea.post { startGame() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCatcher() {
        catcher = TextView(this)
        catcher.text = "🧺"
        catcher.textSize = 50f
        catcher.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.BOTTOM
        catcher.layoutParams = params
        gameArea.addView(catcher)

        gameArea.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_MOVE || event.action == android.view.MotionEvent.ACTION_DOWN) {
                val newX = event.x - (catcher.width / 2)
                catcher.x = newX.coerceIn(0f, (gameArea.width - catcher.width).toFloat())
            }
            true
        }
    }

    private fun startGame() {
        timerRunning = true
        startTimer()
        startSpawning()
    }

    private fun startSpawning() {
        handler.post(object : Runnable {
            override fun run() {
                if (timerRunning) {
                    spawnFallingItem()
                    val nextDelay = (1000 - (score * 10)).coerceAtLeast(400)
                    handler.postDelayed(this, nextDelay.toLong())
                }
            }
        })
    }

    private fun spawnFallingItem() {
        val item = TextView(this)
        item.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val isGold = Random.nextInt(100) < 15
        item.text = if (isGold) "⭐" else listOf("🍎", "🥤", "🎈", "🍭").random()
        item.textSize = 34f
        
        gameArea.addView(item)
        val maxX = (gameArea.width - 100).coerceAtLeast(1)
        item.x = Random.nextInt(0, maxX).toFloat()
        item.y = -100f

        val duration = Random.nextLong(2000, 3500)
        val anim = ObjectAnimator.ofFloat(item, "translationY", -100f, gameArea.height.toFloat() + 100f)
        anim.duration = duration
        anim.addUpdateListener {
            checkCollision(item, isGold)
        }
        activeAnimators.add(anim)
        anim.start()
        
        anim.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                activeAnimators.remove(animation)
                if (item.parent != null) gameArea.removeView(item)
            }
        })
    }

    private fun checkCollision(item: TextView, isGold: Boolean) {
        if (item.parent == null) return
        
        val itemRect = android.graphics.Rect()
        item.getHitRect(itemRect)
        
        val catcherRect = android.graphics.Rect()
        catcher.getHitRect(catcherRect)
        
        if (android.graphics.Rect.intersects(itemRect, catcherRect)) {
            mediaTap?.start()
            score += if (isGold) 5 else 1
            scoreText.text = "Score: $score"
            gameArea.removeView(item)
            
            if (isGold) {
                Toast.makeText(this, "✨ GOLDEN CATCH!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startTimer() {
        object : CountDownTimer(gameTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timerRunning = false
                Toast.makeText(this@HappyGameActivity, "Joy Catch Complete! Score: $score", Toast.LENGTH_LONG).show()
                handler.postDelayed({ finish() }, 2000)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaTap?.release()
        handler.removeCallbacksAndMessages(null)
        activeAnimators.forEach { it.cancel() }
        activeAnimators.clear()
    }
}


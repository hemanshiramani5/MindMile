package com.example.mindmile.users

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import kotlin.random.Random

class SadGameActivity : AppCompatActivity() {

    private lateinit var gameArea: FrameLayout
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var messageText: TextView
    private lateinit var glowOrb: TextView

    private var comfortLevel = 0f
    private var isHolding = false
    private var timerRunning = false
    private val gameTime = 25000L
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sad_game)

        gameArea = findViewById(R.id.gameArea)
        scoreText = findViewById(R.id.scoreText)
        timerText = findViewById(R.id.timerText)
        messageText = findViewById(R.id.messageText)

        mediaPlayer = MediaPlayer.create(this, R.raw.soft_tap)

        setupHoldGame()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHoldGame() {
        gameArea.setBackgroundColor(Color.parseColor("#1A237E")) // Start Dark Blue
        
        glowOrb = TextView(this)
        glowOrb.text = "🤍"
        glowOrb.textSize = 60f
        glowOrb.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.CENTER
        glowOrb.layoutParams = params
        gameArea.addView(glowOrb)

        glowOrb.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    isHolding = true
                    startGlowEffect()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    isHolding = false
                }
            }
            true
        }

        startGame()
    }

    private fun startGlowEffect() {
        handler.post(object : Runnable {
            override fun run() {
                if (isHolding && timerRunning) {
                    comfortLevel += 1f
                    updateGlowUI()
                    handler.postDelayed(this, 50)
                }
            }
        })
    }

    private fun updateGlowUI() {
        val progress = (comfortLevel / 400f).coerceAtMost(1f)
        
        // Grow orb
        val scale = 1f + (progress * 3f)
        glowOrb.scaleX = scale
        glowOrb.scaleY = scale
        
        // Brighten background
        val startColor = Color.parseColor("#1A237E") // Dark
        val endColor = Color.parseColor("#E3F2FD")  // Light
        
        val red = (Color.red(startColor) + (Color.red(endColor) - Color.red(startColor)) * progress).toInt()
        val green = (Color.green(startColor) + (Color.green(endColor) - Color.green(startColor)) * progress).toInt()
        val blue = (Color.blue(startColor) + (Color.blue(endColor) - Color.blue(startColor)) * progress).toInt()
        
        gameArea.setBackgroundColor(Color.rgb(red, green, blue))
        
        scoreText.text = "Inner Glow: ${(progress * 100).toInt()}%"
        
        if (progress > 0.5f) {
            messageText.text = "You are glowing 💙"
            glowOrb.text = "✨"
        }
        
        if (progress >= 1f && timerRunning) {
            finishGame()
        }
    }

    private fun startGame() {
        timerRunning = true
        startTimer()
        
        // Shrink logic if not holding
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isHolding && comfortLevel > 0) {
                    comfortLevel -= 0.5f
                    updateGlowUI()
                }
                handler.postDelayed(this, 100)
            }
        }, 100)
    }

    private fun startTimer() {
        object : CountDownTimer(gameTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timerRunning = false
                finishGame()
            }
        }.start()
    }

    private fun finishGame() {
        timerRunning = false
        handler.removeCallbacksAndMessages(null)
        val msg = if (comfortLevel > 200) "Your light is back! 💙" else "A little glow goes a long way. 🤍"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        handler.postDelayed({ finish() }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)
    }
}

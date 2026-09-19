package com.example.mindmile.users

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import kotlin.random.Random

class AngryGameActivity : AppCompatActivity() {

    private lateinit var gameArea: FrameLayout
    private lateinit var angerText: TextView
    private lateinit var timerText: TextView
    private lateinit var calmText: TextView

    private var angerLevel = 100
    private var timerRunning = false
    private val gameTime = 20000L
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_angry_game)

        gameArea = findViewById(R.id.gameArea)
        angerText = findViewById(R.id.angerText)
        timerText = findViewById(R.id.timerText)
        calmText = findViewById(R.id.calmText)

        mediaPlayer = MediaPlayer.create(this, R.raw.smash_sound)

        gameArea.post { setupScribbleGame() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupScribbleGame() {
        gameArea.setBackgroundResource(R.drawable.bg_relax)
        
        val cols = 8
        val rows = 12
        val cellWidth = gameArea.width / cols
        val cellHeight = gameArea.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val smoke = TextView(this)
                smoke.text = "☁️"
                smoke.textSize = 40f
                smoke.gravity = android.view.Gravity.CENTER
                smoke.setBackgroundColor(Color.parseColor("#44000000"))
                smoke.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                
                val params = FrameLayout.LayoutParams(cellWidth, cellHeight)
                params.leftMargin = c * cellWidth
                params.topMargin = r * cellHeight
                smoke.layoutParams = params
                
                gameArea.addView(smoke)
            }
        }

        gameArea.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_MOVE || event.action == android.view.MotionEvent.ACTION_DOWN) {
                checkCollision(event.x, event.y)
            }
            true
        }

        startGame()
    }

    private fun checkCollision(x: Float, y: Float) {
        val col = (x / (gameArea.width / 8)).toInt().coerceIn(0, 7)
        val row = (y / (gameArea.height / 12)).toInt().coerceIn(0, 11)
        val index = (row * 8) + col
        
        if (index < gameArea.childCount) {
            val child = gameArea.getChildAt(index)
            if (child is TextView && child.visibility == View.VISIBLE) {
                child.visibility = View.INVISIBLE
                updateAngerLevel()
                vibrate(40)
            }
        }
    }

    private fun updateAngerLevel() {
        var visibleCells = 0
        for (i in 0 until gameArea.childCount) {
            if (gameArea.getChildAt(i).visibility == View.VISIBLE) visibleCells++
        }
        
        val totalCells = 8 * 12
        angerLevel = ((visibleCells.toFloat() / totalCells.toFloat()) * 100).toInt()
        angerText.text = "Anger Level: $angerLevel%"
        
        if (angerLevel < 20) {
            calmText.text = "Looking better... keep going! ✨"
        }
        
        if (angerLevel == 0 && timerRunning) {
            finishGame()
        }
    }

    private fun startGame() {
        timerRunning = true
        startTimer()
        
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (timerRunning) {
                    if (Random.nextInt(100) < 15) {
                        val idx = Random.nextInt(gameArea.childCount)
                        val child = gameArea.getChildAt(idx)
                        if (child is TextView && child.visibility == View.INVISIBLE) {
                            child.visibility = View.VISIBLE
                            updateAngerLevel()
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }
        }, 3000)
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
        val msg = if (angerLevel < 10) "Anger wiped away! 🌿" else "Energy released. Breathe. 🌬️"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        handler.postDelayed({ finish() }, 2000)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate(duration: Long) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)
    }
}

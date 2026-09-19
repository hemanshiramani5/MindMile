package com.example.mindmile.users

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.*
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import java.util.Random

class SleepyGameActivity : AppCompatActivity() {

    private lateinit var gameArea: FrameLayout
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var instructionText: TextView

    private var score = 0
    private var isPlaying = false
    private val random = Random()
    private val handler = Handler(Looper.getMainLooper())
    private val gameDuration = 30000L
    
    private var currentTargetValue = 1
    private val sequenceMax = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleepy_game)

        gameArea = findViewById(R.id.gameArea)
        scoreText = findViewById(R.id.scoreText)
        timerText = findViewById(R.id.timerText)
        instructionText = findViewById(R.id.instructionText)

        gameArea.setOnClickListener {
            if (!isPlaying) startGame()
        }
    }

    private fun startGame() {
        isPlaying = true
        score = 0
        currentTargetValue = 1
        scoreText.text = "Sequence: 0"
        instructionText.visibility = View.GONE

        startTimer()
        spawnSequence()
    }

    private fun spawnSequence() {
        if (!isPlaying) return
        gameArea.removeAllViews()
        
        currentTargetValue = 1
        val numToSpawn = (3 + (score / 2)).coerceAtMost(sequenceMax)
        
        for (i in 1..numToSpawn) {
            spawnNumberedOrb(i)
        }
    }

    private fun spawnNumberedOrb(value: Int) {
        val orb = TextView(this)
        orb.text = value.toString()
        orb.textSize = 32f
        orb.setTextColor(Color.BLACK)
        orb.gravity = android.view.Gravity.CENTER
        orb.background = resources.getDrawable(R.drawable.circle_shape, null)
        orb.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        orb.elevation = 8f
        orb.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val size = 150
        val x = random.nextInt((gameArea.width - size).coerceAtLeast(1)).toFloat()
        val y = random.nextInt((gameArea.height - size).coerceAtLeast(1)).toFloat()

        orb.layoutParams = FrameLayout.LayoutParams(size, size)
        orb.x = x
        orb.y = y

        orb.setOnClickListener {
            handleOrbClick(orb, value)
        }

        gameArea.addView(orb)
        
        // Appear animation
        orb.scaleX = 0f
        orb.scaleY = 0f
        orb.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
    }

    private fun handleOrbClick(orb: TextView, value: Int) {
        if (!isPlaying) return

        if (value == currentTargetValue) {
            // Correct!
            orb.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            orb.animate().scaleX(0f).scaleY(0f).setDuration(200).withEndAction {
                gameArea.removeView(orb)
            }.start()
            
            currentTargetValue++
            
            // Check if sequence complete
            if (gameArea.childCount <= 1) { // 1 because the current one is being removed
                score++
                scoreText.text = "Sequences: $score"
                handler.postDelayed({ spawnSequence() }, 300)
            }
        } else {
            // Wrong! Reset sequence
            Toast.makeText(this, "Wrong Order! Resetting...", Toast.LENGTH_SHORT).show()
            vibrate(100)
            spawnSequence()
        }
    }

    private fun startTimer() {
        object : CountDownTimer(gameDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isPlaying = false 
                gameArea.removeAllViews()
                instructionText.text = "NEURAL SHARPENING COMPLETE! ⚡\nScore: $score"
                instructionText.visibility = View.VISIBLE
                
                Toast.makeText(this@SleepyGameActivity, "Mind Energized! ⚡", Toast.LENGTH_LONG).show()
                handler.postDelayed({ finish() }, 2000)
            }
        }.start()
    }

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
        handler.removeCallbacksAndMessages(null)
    }
}

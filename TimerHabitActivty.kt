package com.example.mindmile.users

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.example.mindmile.databinding.ActivityTimerHabitBinding

class TimerHabitActivity : AppCompatActivity() {

    private var isRunning = false
    private var seconds = 0
    private var goalMinutes: Long = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var goalReachedToastShown = false

    private lateinit var tvDigit1: TextView
    private lateinit var tvDigit2: TextView
    private lateinit var tvDigit3: TextView
    private lateinit var tvDigit4: TextView
    private lateinit var ivPlayPause: ImageView
    private lateinit var btnPlayPause: View
    private lateinit var tvTitle: TextView

    private var habitId: String? = null
    private var habitTitle: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_timer_habit)

            habitId = intent.getStringExtra("habitId")
            habitTitle = intent.getStringExtra("title")
            goalMinutes = intent.getLongExtra("goalValue", 1)

            tvTitle = findViewById(R.id.tvScreenTitle)
            tvTitle.text = habitTitle ?: "Timer"

            tvDigit1 = findViewById(R.id.tvDigit1)
            tvDigit2 = findViewById(R.id.tvDigit2)
            tvDigit3 = findViewById(R.id.tvDigit3)
            tvDigit4 = findViewById(R.id.tvDigit4)
            ivPlayPause = findViewById(R.id.ivPlayPause)
            btnPlayPause = findViewById(R.id.btnPlayPause)

            findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    if (isRunning) {
                        seconds++
                        updateTimerDisplay()

                        // Check if goal reached
                        if (seconds >= goalMinutes * 60 && !goalReachedToastShown) {
                            pauseTimer() // Auto-pause when goal is reached
                            Toast.makeText(this@TimerHabitActivity, "Goal Reached! Timer paused.", Toast.LENGTH_SHORT).show()
                            goalReachedToastShown = true
                        } else {
                            handler.postDelayed(this, 1000)
                        }
                    }
                }
            }

            btnPlayPause.setOnClickListener {
                toggleTimer()
            }

            findViewById<View>(R.id.btnReset).setOnClickListener {
                resetTimer()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening Timer: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun toggleTimer() {
        if (isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        isRunning = true
        handler.post(runnable)
        ivPlayPause.setImageResource(R.drawable.ic_pause)
    }

    private fun pauseTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
        ivPlayPause.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun resetTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
        seconds = 0
        goalReachedToastShown = false
        updateTimerDisplay()
        ivPlayPause.setImageResource(R.drawable.ic_play_arrow)
        Toast.makeText(this, "Timer Reset", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerDisplay() {
        // Format seconds to MM:SS
        val m = seconds / 60
        val s = seconds % 60

        // Split digits
        val m1 = m / 10
        val m2 = m % 10
        val s1 = s / 10
        val s2 = s % 10

        tvDigit1.text = m1.toString()
        tvDigit2.text = m2.toString()
        tvDigit3.text = s1.toString()
        tvDigit4.text = s2.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
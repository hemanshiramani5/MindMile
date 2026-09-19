package com.example.mindmile.users

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.example.mindmile.databinding.ActivityTimerHabitBinding

class TimerHabitActivity : AppCompatActivity() {
    
    private val db = FirebaseFirestore.getInstance()

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
            
            findViewById<ImageView>(R.id.btnMore).setOnClickListener { view ->
                showMoreMenu(view)
            }

            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    if (isRunning) {
                        seconds++
                        
                        val maxSeconds = (goalMinutes * 60).toInt()
                        
                        // Check if goal reached or exceeded
                        if (seconds >= maxSeconds) {
                            seconds = maxSeconds // Clamp to goal
                            updateTimerDisplay()
                            pauseTimer() // Auto-pause
                            
                            if (!goalReachedToastShown) {
                                Toast.makeText(this@TimerHabitActivity, "Goal Reached! Timer paused.", Toast.LENGTH_SHORT).show()
                                goalReachedToastShown = true
                            }
                        } else {
                            updateTimerDisplay()
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

            val colorStr = intent.getStringExtra("color")
            if (colorStr != null) {
                try {
                    val color = android.graphics.Color.parseColor(colorStr)
                    val lightColor = lightenColor(color, 0.9f) // Very light tint

                    // Root Background
                    findViewById<android.view.ViewGroup>(R.id.rootLayout).setBackgroundColor(lightColor)

                    // Start Button
                    findViewById<View>(R.id.btnPlayPause).background.setTint(color)

                    // Cards
                    val cards = listOf(R.id.card1, R.id.card2, R.id.card3, R.id.card4)
                    cards.forEach { cardId ->
                        findViewById<View>(cardId).background.setTint(color)
                    }

                    // Digits Text Color (White on colored card)
                    listOf(tvDigit1, tvDigit2, tvDigit3, tvDigit4).forEach { 
                        it.setTextColor(android.graphics.Color.WHITE) 
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            loadHabitData()
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening Timer: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun lightenColor(color: Int, factor: Float): Int {
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)

        val newRed = (red + (255 - red) * factor).toInt()
        val newGreen = (green + (255 - green) * factor).toInt()
        val newBlue = (blue + (255 - blue) * factor).toInt()

        return android.graphics.Color.rgb(newRed, newGreen, newBlue)
    }

    private fun toggleTimer() {
        if (habitId == null) return
        if (isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (habitId == null) return
        isRunning = true
        handler.post(runnable)
        ivPlayPause.setImageResource(R.drawable.ic_pause)
        
        // Update Firestore
        db.collection("user_habits").document(habitId!!)
            .update(
                "timerRunning", true,
                "timerStartTime", Timestamp.now()
            )
    }

    private fun pauseTimer() {
        if (habitId == null) return
        isRunning = false
        handler.removeCallbacks(runnable)
        ivPlayPause.setImageResource(R.drawable.ic_play_arrow)
        
        // Update Firestore
        db.collection("user_habits").document(habitId!!)
            .update(
                "timerRunning", false,
                "completedValue", seconds.toLong()
            )
    }

    private fun resetTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
        seconds = 0
        goalReachedToastShown = false
        updateTimerDisplay()
        ivPlayPause.setImageResource(R.drawable.ic_play_arrow)
        
        // Update Firestore
        habitId?.let { id ->
            db.collection("user_habits").document(id)
                .update(
                    "timerRunning", false,
                    "completedValue", 0L
                )
        }
        Toast.makeText(this, "Timer Reset", Toast.LENGTH_SHORT).show()
    }

    private fun loadHabitData() {
        if (habitId == null) return
        db.collection("user_habits").document(habitId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val savedProgress = document.getLong("completedValue") ?: 0L
                    val timerRunning = document.getBoolean("timerRunning") ?: false
                    val timerStartTime = document.getTimestamp("timerStartTime")
                    
                    if (timerRunning && timerStartTime != null) {
                        val now = Timestamp.now().seconds
                        val start = timerStartTime.seconds
                        val elapsed = now - start
                        seconds = (savedProgress + elapsed).toInt()
                        startTimer() // Resume immediately
                    } else {
                        seconds = savedProgress.toInt()
                        updateTimerDisplay()
                    }
                }
            }
    }

    private fun saveCurrentSeconds() {
        if (habitId == null) return
        db.collection("user_habits").document(habitId!!)
            .update("completedValue", seconds.toLong())
    }

    override fun onPause() {
        super.onPause()
        if (isRunning) {
            saveCurrentSeconds()
        }
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

    private fun showMoreMenu(view: android.view.View) {
        val popup = androidx.appcompat.widget.PopupMenu(this, view)
        popup.menu.add("Edit Goal")
        popup.menu.add("Delete Habit")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Edit Goal" -> {
                    showEditGoalDialog()
                    true
                }
                "Delete Habit" -> {
                    deleteHabit()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showEditGoalDialog() {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setText(goalMinutes.toString())

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Goal (Minutes)")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newValue = input.text.toString().toLongOrNull()
                if (newValue != null && newValue > 0) {
                    updateGoalInFirestore(newValue)
                } else {
                    Toast.makeText(this, "Invalid value", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateGoalInFirestore(newValue: Long) {
        if (habitId == null) return
        db.collection("user_habits").document(habitId!!)
            .update("goalValue", newValue)
            .addOnSuccessListener {
                goalMinutes = newValue
                goalReachedToastShown = false // Reset in case they increased goal
                Toast.makeText(this, "Goal updated to $newValue mins", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteHabit() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Delete") { _, _ ->
                habitId?.let { id ->
                    db.collection("user_habits").document(id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

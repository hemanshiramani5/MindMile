package com.example.mindmile.users

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.example.mindmile.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.appcompat.widget.SwitchCompat
import java.util.Calendar
import android.app.TimePickerDialog

class add_habits : AppCompatActivity() {

    private lateinit var tvHabitName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var etGoalValue: EditText
    private lateinit var tvGoalUnit: TextView
    private lateinit var ivHabitImage: ImageView
    private lateinit var viewSelectedColor: View
    private lateinit var btnSave: TextView
    
    // New UI Element Bindings
    private lateinit var tvHeaderTitle: TextView
    private lateinit var ivHeaderIcon: ImageView
    private lateinit var btnTypeBuild: TextView
    private lateinit var btnTypeQuit: TextView
    private lateinit var tvGoalSummary: TextView
    private lateinit var chipGroupTime: com.google.android.material.chip.ChipGroup
    private lateinit var switchReminders: androidx.appcompat.widget.SwitchCompat
    private lateinit var btnTime: TextView
    private lateinit var tvReminderTime: TextView
    private lateinit var rootLayout: View
    private lateinit var llReminderOptions: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Data holders
    private var colorString: String? = "#5E81AC"
    private var imageBase64: String? = null
    private var unit: String = "steps"
    private var habitType: String = "Build"
    private var selectedTimeRange: String = "Anytime"
    private var reminderTime: String = "19:30"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.add_habits)

            // 🔹 Bind Views
            tvHabitName = findViewById(R.id.tvHabitName)
            tvDescription = findViewById(R.id.tvDescription)
            etGoalValue = findViewById(R.id.etGoalValue)
            tvGoalUnit = findViewById(R.id.tvGoalUnit)
            ivHabitImage = findViewById(R.id.ivHabitImage)
            viewSelectedColor = findViewById(R.id.viewSelectedColor)
            btnSave = findViewById(R.id.btnSave)
            val btnBack = findViewById<ImageView>(R.id.btnBack)
            
            // New Bindings
            tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
            ivHeaderIcon = findViewById(R.id.ivHeaderIcon)
            btnTypeBuild = findViewById(R.id.btnTypeBuild)
            btnTypeQuit = findViewById(R.id.btnTypeQuit)
            tvGoalSummary = findViewById(R.id.tvGoalSummary)
            chipGroupTime = findViewById(R.id.chipGroupTime)
            switchReminders = findViewById(R.id.switchReminders)
            btnTime = findViewById(R.id.btnTime)
            tvReminderTime = findViewById(R.id.tvReminderTime)
            rootLayout = findViewById(R.id.rootLayout)
            llReminderOptions = findViewById(R.id.llReminderOptions)

            // 🔹 Reminder Toggle Logic
            switchReminders.setOnCheckedChangeListener { _, isChecked ->
                llReminderOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
            // Initial State
            llReminderOptions.visibility = if (switchReminders.isChecked) View.VISIBLE else View.GONE

            // 🔹 Habit Type Selection Logic
            btnTypeBuild.setOnClickListener {
                habitType = "Build"
                colorString?.let { applyColorToUI(Color.parseColor(it)) }
                updateGoalSummary()
            }

            btnTypeQuit.setOnClickListener {
                habitType = "Quit"
                colorString?.let { applyColorToUI(Color.parseColor(it)) }
                updateGoalSummary()
            }

            // 🔹 Goal Summary Logic (Dynamic)
            etGoalValue.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateGoalSummary()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            // 🔹 Time Range Chip Selection
            chipGroupTime.setOnCheckedChangeListener { group, checkedId ->
                val chip = findViewById<com.google.android.material.chip.Chip>(checkedId)
                selectedTimeRange = chip?.text?.toString() ?: "Anytime"
            }

            // 🔹 Time Picker for Reminders
            btnTime.setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = calendar.get(java.util.Calendar.MINUTE)

                android.app.TimePickerDialog(this, { _, h, m ->
                    reminderTime = String.format("%02d:%02d", h, m)
                    tvReminderTime.text = reminderTime
                }, hour, minute, true).show()
            }

            // 🔹 Load Data from Intent (Template)
            val habitId = intent.getStringExtra("habitId")
            if (!habitId.isNullOrEmpty()) {
                loadHabitTemplate(habitId)
            }

            // 🔹 Actions
            btnBack?.setOnClickListener { finish() }
            btnSave.setOnClickListener { saveHabitToUser() }
            
            // 🔹 Color Picker Action
            val colorContainer = findViewById<View>(R.id.viewSelectedColor).parent as View
            colorContainer.setOnClickListener {
                showColorPickerDialog()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error starting: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun updateGoalSummary() {
        val value = etGoalValue.text.toString().ifEmpty { "0" }
        if (habitType == "Build") {
            tvGoalSummary.text = "*Complete $value $unit each day"
            tvGoalSummary.setTextColor(Color.parseColor("#FFB347")) // Warning/Orange color
        } else {
            tvGoalSummary.text = "*Don't exceed $value $unit each day"
            tvGoalSummary.setTextColor(Color.parseColor("#FF3B30")) // Alert/Red color for 'Quit'
        }
    }

    private fun showColorPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_color_picker, null)
        val viewPreview = dialogView.findViewById<View>(R.id.viewColorPreview)
        val sbRed = dialogView.findViewById<SeekBar>(R.id.sbRed)
        val sbGreen = dialogView.findViewById<SeekBar>(R.id.sbGreen)
        val sbBlue = dialogView.findViewById<SeekBar>(R.id.sbBlue)

        // Initial Values
        var r = 94
        var g = 129
        var b = 172
        
        colorString?.let {
            try {
                val color = Color.parseColor(it)
                r = Color.red(color)
                g = Color.green(color)
                b = Color.blue(color)
            } catch (e: Exception) {}
        }

        sbRed.progress = r
        sbGreen.progress = g
        sbBlue.progress = b
        viewPreview.setBackgroundColor(Color.rgb(r, g, b))

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val color = Color.rgb(sbRed.progress, sbGreen.progress, sbBlue.progress)
                viewPreview.setBackgroundColor(color)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        sbRed.setOnSeekBarChangeListener(listener)
        sbGreen.setOnSeekBarChangeListener(listener)
        sbBlue.setOnSeekBarChangeListener(listener)

        android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Select") { _, _ ->
                val color = Color.rgb(sbRed.progress, sbGreen.progress, sbBlue.progress)
                colorString = String.format("#%06X", (0xFFFFFF and color))
                applyColorToUI(color)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyColorToUI(color: Int) {
        val lightColor = getLightColor(color)
        val colorStateList = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(color, Color.parseColor("#F2F2F7"))
        )
        
        // Background
        rootLayout.setBackgroundColor(lightColor)
        
        // UI Components (Full Color)
        viewSelectedColor.setBackgroundColor(color)
        btnSave.setTextColor(color)
        tvReminderTime.setTextColor(color)
        
        // Use background.setTint to preserve rounded corners
        btnTime.background?.setTint(color)
        
        // Switch Tint
        switchReminders.trackTintList = colorStateList
        switchReminders.thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)

        // Chips Tint
        for (i in 0 until chipGroupTime.childCount) {
            val chip = chipGroupTime.getChildAt(i) as? com.google.android.material.chip.Chip
            chip?.chipBackgroundColor = colorStateList
            chip?.setTextColor(android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(Color.WHITE, Color.parseColor("#C7C7CC"))
            ))
        }
        
        // Habit Type Selection (Current Selection)
        if (habitType == "Build") {
            btnTypeBuild.setBackgroundResource(R.drawable.segmented_item_selected)
            btnTypeBuild.background?.setTint(color)
            btnTypeBuild.setTextColor(Color.WHITE)
            
            btnTypeQuit.setBackground(null)
            btnTypeQuit.setTextColor(Color.parseColor("#C7C7CC"))
        } else {
            btnTypeQuit.setBackgroundResource(R.drawable.segmented_item_selected)
            btnTypeQuit.background?.setTint(color)
            btnTypeQuit.setTextColor(Color.WHITE)
            
            btnTypeBuild.setBackground(null)
            btnTypeBuild.setTextColor(Color.parseColor("#C7C7CC"))
        }
    }

    private fun getLightColor(color: Int): Int {
        val alpha = (Color.alpha(color) * 0.15).toInt() // 15% opacity for a very light background
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun loadHabitTemplate(habitId: String) {
        db.collection("habits")
            .document(habitId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val title = doc.getString("title") ?: ""
                tvHabitName.text = title
                tvHeaderTitle.text = title
                tvDescription.text = doc.getString("description") ?: ""

                // Goal value
                val goalAny = doc.get("goalValue")
                val gVal = goalAny?.toString() ?: "1"
                etGoalValue.setText(gVal)

                unit = doc.getString("goalUnit") ?: "steps"
                tvGoalUnit.text = unit
                updateGoalSummary()

                // Color
                colorString = doc.getString("color") ?: "#5E81AC"
                try {
                    val color = Color.parseColor(colorString)
                    applyColorToUI(color)
                } catch (e: Exception) {
                    applyColorToUI(Color.parseColor("#5E81AC"))
                }

                // Image
                imageBase64 = doc.getString("imageUrl")
                imageBase64?.let {
                    try {
                        val bitmap = base64ToBitmap(it)
                        ivHabitImage.setImageBitmap(bitmap)
                        ivHeaderIcon.setImageBitmap(bitmap)
                        ivHeaderIcon.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        ivHabitImage.setImageResource(R.drawable.ic_image_placeholder)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load template", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveHabitToUser() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val goalValStr = etGoalValue.text.toString().trim()
        if (goalValStr.isEmpty()) {
            Toast.makeText(this, "Please enter a goal value", Toast.LENGTH_SHORT).show()
            return
        }

        val goalValue = goalValStr.toIntOrNull() ?: 1

        val userHabitData = hashMapOf(
            "userId" to user.uid,
            "originalHabitId" to (intent.getStringExtra("habitId") ?: ""),
            "title" to tvHabitName.text.toString(),
            "description" to tvDescription.text.toString(),
            "habitType" to habitType,
            "goalValue" to goalValue,
            "goalUnit" to unit,
            "color" to colorString,
            "imageUrl" to imageBase64,
            "timeRange" to selectedTimeRange,
            "remindersEnabled" to switchReminders.isChecked,
            "reminderTime" to reminderTime,
            "startDate" to Timestamp.now(), // Use standard Firebase Timestamp
            "isActive" to true
        )

        db.collection("user_habits")
            .add(userHabitData)
            .addOnSuccessListener {
                Toast.makeText(this, "Habit Added to your Dashboard!", Toast.LENGTH_SHORT).show()
                finish() // Go back
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }
}

package com.example.mindmile.users

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindmile.R
import com.example.mindmile.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.util.*
import kotlin.math.roundToInt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var currentEmotion = "Neutral"
    private var currentEmoji = "😐"
    private var imageCapture: ImageCapture? = null
    private var isFaceDetected = false
    private var cameraProvider: ProcessCameraProvider? = null

    // Habits
    private lateinit var llHabitsContainer: LinearLayout
    private lateinit var emptyContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val quotes = listOf(
        Quote("Keep smiling, life is beautiful!", "Happy"),
        Quote("Stay calm, everything will be fine.", "Neutral"),
        Quote("It's okay to feel sad, you will rise again.", "Sad"),
        Quote("Control your anger, before it controls you.", "Angry"),
        Quote("Rest your mind, relax and breathe.", "Sleepy")
    )

    // For smoothing emotion detection
    private val recentSmiles = LinkedList<Float>()
    private val maxSmiles = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupSpinner()
            setupClicks()
            setupDateClicks()
            setupQuotesRecycler()
            setupCameraButtons()

            // Habits Init
            llHabitsContainer = findViewById(R.id.llHabitsContainer)
            emptyContainer = findViewById(R.id.emptyContainer)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in onCreate: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.time_periods,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.timeSpinner.adapter = adapter
    }

    private fun setupClicks() {
        binding.todayText.setOnClickListener(this)
        binding.emojiButton.setOnClickListener(this)
        binding.bookIcon.setOnClickListener(this)
        binding.groupIcon.setOnClickListener(this)
        binding.addIcon.setOnClickListener(this)
        binding.listIcon.setOnClickListener(this)
        binding.settingsIcon.setOnClickListener(this)
    }

    private fun setupDateClicks() {
        try {
            val dateScroller: LinearLayout = binding.dateScroller.getChildAt(0) as LinearLayout
            for (i in 0 until dateScroller.childCount) {
                val item = dateScroller.getChildAt(i)
                item.setOnClickListener {
                    Toast.makeText(
                        this,
                        "Selected date: ${getDayText(item)} ${getDateText(item)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
             // Ignore or log if specific child view is missing
        }
    }

    private fun setupQuotesRecycler() {
        binding.quotesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.quotesRecyclerView.adapter = QuoteAdapter(quotes)
        {
                quote -> Toast.makeText(this, "Quote clicked: ${quote.text}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDayText(layout: View): String =
        (layout as LinearLayout).getChildAt(0).let { it as TextView }.text.toString()

    private fun getDateText(layout: View): String =
        (layout as LinearLayout).getChildAt(1).let { it as TextView }.text.toString()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.todayText -> toast("Today clicked")
            R.id.emojiButton -> toast("Emoji button clicked")
            R.id.bookIcon -> toast("Book clicked")
            R.id.groupIcon -> toast("Group clicked")
            R.id.addIcon -> {
                startActivity(Intent(this, show_habits::class.java))
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
            }
            R.id.listIcon -> startActivity(Intent(this, HabitIdeaActivity::class.java))
            R.id.settingsIcon -> startActivity(Intent(this, Settings::class.java))
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // ----------------------- CAMERA logic -----------------------
    private fun setupCameraButtons() {
        binding.cameraButton.setOnClickListener { 
            // toast("Camera button clicked")
            showCameraOverlay() 
        }
        binding.closeCamera.setOnClickListener { hideCameraOverlay() }
        binding.captureButton.setOnClickListener { capturePhoto() }
    }

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
        FaceDetection.getClient(options)
    }

    private fun showCameraOverlay() {
        Toast.makeText(this, "Opening Camera...", Toast.LENGTH_SHORT).show()
        binding.previewView.visibility = View.VISIBLE
        binding.emojiOverlay.visibility = View.VISIBLE
        binding.emotionText.visibility = View.VISIBLE
        binding.emotionProgressBar.visibility = View.VISIBLE
        binding.moodQuoteText.visibility = View.VISIBLE
        binding.closeCamera.visibility = View.VISIBLE
        binding.captureButton.visibility = View.VISIBLE

        // Ensure camera UI is on top
        binding.previewView.bringToFront()
        binding.emojiOverlay.bringToFront()
        binding.emotionText.bringToFront()
        binding.emotionProgressBar.bringToFront()
        binding.moodQuoteText.bringToFront()
        binding.closeCamera.bringToFront()
        binding.captureButton.bringToFront()

        // Reset state
        stableEmotionCounter = 0
        isCapturing = false
        lastEmotion = ""
        currentEmotion = "Neutral"

        checkCameraPermission()
    }

    private fun hideCameraOverlay() {
        binding.previewView.visibility = View.GONE
        binding.emojiOverlay.visibility = View.GONE
        binding.emotionText.visibility = View.GONE
        binding.emotionProgressBar.visibility = View.GONE
        binding.moodQuoteText.visibility = View.GONE
        binding.closeCamera.visibility = View.GONE
        binding.captureButton.visibility = View.GONE

        // Unbind camera
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        // If camera was active, ensure it's re-bound
        if (binding.previewView.visibility == View.VISIBLE) {
            checkCameraPermission()
        }
        loadUserHabits()
    }

    private fun loadUserHabits() {
        try {
            val user = auth.currentUser
            if (user == null) {
                // Toast.makeText(this, "DEBUG: User is NULL", Toast.LENGTH_SHORT).show()
                if (::emptyContainer.isInitialized) emptyContainer.visibility = View.VISIBLE
                if (::llHabitsContainer.isInitialized) llHabitsContainer.visibility = View.GONE
                return
            }

            // Toast.makeText(this, "DEBUG: Fetching habits for ${user.uid}", Toast.LENGTH_SHORT).show()

            db.collection("user_habits")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener { result ->
                    try {
                        // Toast.makeText(this, "DEBUG: Found ${result.size()} habits", Toast.LENGTH_SHORT).show()
                        if (!::llHabitsContainer.isInitialized || !::emptyContainer.isInitialized) return@addOnSuccessListener

                        llHabitsContainer.removeAllViews() // Clear existing

                        if (result.isEmpty) {
                            // Only show empty state if we really have no habits
                            // But maybe we should keep the current empty container logic
                            // Wait, the user code explicitly handles this:
                            emptyContainer.visibility = View.VISIBLE
                            llHabitsContainer.visibility = View.GONE
                        } else {
                            emptyContainer.visibility = View.GONE
                            llHabitsContainer.visibility = View.VISIBLE

                            // Sort client-side to avoid index requirement
                            val documents = result.documents.sortedByDescending {
                                it.getTimestamp("startDate") ?: Timestamp.now()
                            }

                            for (document in documents) {
                                val view = LayoutInflater.from(this).inflate(R.layout.item_user_habit, llHabitsContainer, false)

                                val title = document.getString("title") ?: "Habit"
                                val goalVal = document.getLong("goalValue") ?: 1
                                val unit = document.getString("goalUnit") ?: "times"
                                val imageBase64 = document.getString("imageUrl")
                                val colorHex = document.getString("color") ?: "#FFFFFF"
                                
                                // Progress Logic
                                val currentProgress = document.getLong("progress") ?: 0
                                val isWithInTarget = currentProgress >= goalVal

                                val tvTitle = view.findViewById<TextView>(R.id.tvHabitTitle)
                                val tvGoal = view.findViewById<TextView>(R.id.tvHabitGoal)
                                val ivIcon = view.findViewById<ImageView>(R.id.ivHabitIcon)
                                val ivStatus = view.findViewById<ImageView>(R.id.ivStatus)
                                val llStreak = view.findViewById<LinearLayout>(R.id.llStreak)
                                val tvStreak = view.findViewById<TextView>(R.id.tvStreak)

                                tvTitle.text = title
                                
                                // Apply background and theme colors
                                try {
                                    val intenseColor = android.graphics.Color.parseColor(colorHex)
                                    val lightColor = lightenColor(intenseColor, 0.85) // Lighten for background
                                    
                                    // Set lightened background to the card
                                    view.background?.mutate()?.setTint(lightColor)
                                    
                                    // Set intense color to icon background and title
                                    val flIconContainer = view.findViewById<View>(R.id.flIconContainer)
                                    flIconContainer.background?.mutate()?.setTint(intenseColor)
                                    
                                    tvTitle.setTextColor(intenseColor)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                if (!imageBase64.isNullOrEmpty()) {
                                    try {
                                        val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        ivIcon.setImageBitmap(bitmap)
                                    } catch (e: Exception) {
                                        ivIcon.setImageResource(R.drawable.ic_image_placeholder)
                                    }
                                }
                                
                                // UI State based on Completion
                                if (isWithInTarget) {
                                    // COMPLETED STATE
                                    ivStatus.setImageResource(R.drawable.ic_check_circle)
                                    try {
                                        // Try to tint checkmark green or standard color
                                        ivStatus.setColorFilter(android.graphics.Color.parseColor("#34C759")) // Green
                                    } catch(e:Exception){}
                                    
                                    tvGoal.text = "Completed!"
                                    llStreak.visibility = View.VISIBLE
                                    tvStreak.text = "1 Day" // Mock streak for now
                                } else {
                                    // INCOMPLETE STATE
                                    ivStatus.setImageResource(R.drawable.ic_add)
                                    ivStatus.clearColorFilter() // Reset tint
                                    
                                    tvGoal.text = "$currentProgress / $goalVal $unit"
                                    llStreak.visibility = View.GONE
                                }

                                // Interactive Click Listener (Increment Progress)
                                ivStatus.setOnClickListener {
                                    if (!isWithInTarget) {
                                        // Increment Progress
                                        val newProgress = currentProgress + 1
                                        db.collection("user_habits").document(document.id)
                                            .update("progress", newProgress)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Great job!", Toast.LENGTH_SHORT).show()
                                                loadUserHabits() // Refresh UI
                                            }
                                            .addOnFailureListener {
                                                 Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(this, "Already completed for today!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                // Open Detail View on Card Click
                                view.setOnClickListener {
                                    val intent: android.content.Intent
                                    if (unit.contains("min", ignoreCase = true)) {
                                        intent = android.content.Intent(this, TimerHabitActivity::class.java)
                                    } else {
                                        intent = android.content.Intent(this, HabitDetailActivity::class.java)
                                    }
                                    intent.putExtra("habitId", document.id)
                                    intent.putExtra("title", title)
                                    intent.putExtra("goalValue", goalVal)
                                    startActivity(intent)
                                }

                                llHabitsContainer.addView(view)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "DEBUG: Error in onSuccess: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "DEBUG: Error loading habits: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "DEBUG: Error initiating load: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        // toast("Starting Camera Preview...")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val provider = cameraProvider!!

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    faceDetector.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                isFaceDetected = true
                                val face = faces[0]
                                val smile = face.smilingProbability ?: 0f
                                val leftEye = face.leftEyeOpenProbability ?: 0f
                                val rightEye = face.rightEyeOpenProbability ?: 0f
                                val headX = face.headEulerAngleX

                                updateEmotion(smile, leftEye, rightEye, headX)
                            } else {
                                isFaceDetected = false
                                runOnUiThread {
                                    binding.emotionText.text = "No face detected 😶"
                                    binding.emojiOverlay.text = "😐"
                                    binding.moodQuoteText.text = "Please align your face"
                                }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else imageProxy.close()
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageCapture,
                analysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    // ----------------- EMOTION DETECTION -----------------
    private fun updateEmotion(smile: Float, leftEye: Float, rightEye: Float, headX: Float) {
        if (recentSmiles.size >= maxSmiles) recentSmiles.poll()
        recentSmiles.offer(smile)
        val avgSmile = recentSmiles.average().toFloat()

        currentEmotion = when {
            avgSmile > 0.4f -> "Happy"
            leftEye < 0.25f && rightEye < 0.25f -> "Sleepy"
            headX < -15f && avgSmile < 0.2f -> "Sad"
            (leftEye < 0.5f || rightEye < 0.5f) && avgSmile < 0.2f -> "Angry"
            else -> "Neutral"
        }

        currentEmoji = when (currentEmotion) {
            "Happy" -> "😊"
            "Sad" -> "😔"
            "Angry" -> "😡"
            "Sleepy" -> "😴"
            "Neutral" -> "😐"
            else -> "😐"
        }

        val currentQuote = quotes.find { it.mood.equals(currentEmotion, ignoreCase = true) }?.text
            ?: "Stay positive!"

        runOnUiThread {
            binding.emojiOverlay.text = currentEmoji
            binding.emotionText.text = "Emotion: $currentEmotion"
            binding.emotionProgressBar.progress = (avgSmile * 100).roundToInt()
            binding.moodQuoteText.text = "\"$currentQuote\""
        }

        // Auto-capture logic
        if (currentEmotion != "Neutral" && currentEmotion == lastEmotion) {
            stableEmotionCounter++
            
            if (stableEmotionCounter >= 15 && !isCapturing) {
                isCapturing = true
                runOnUiThread {
                    Toast.makeText(this, "Mood detected! Capturing...", Toast.LENGTH_SHORT).show()
                }
                capturePhoto()
            }
        } else {
            stableEmotionCounter = 0
            isCapturing = false
        }
        lastEmotion = currentEmotion
    }

    private var lastEmotion = ""
    private var stableEmotionCounter = 0
    private var isCapturing = false

    // ----------------- PHOTO CAPTURE -----------------
    private fun capturePhoto() {
        if (!isFaceDetected) {
            showNoFacePopup()
            return
        }
        
        // toast("Capture process initiated...")

        val photoFile = File(
            getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "camera_capture_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "Photo captured!", Toast.LENGTH_SHORT).show()
                    showMoodPopup()
                }
            }
        )
    }

    // ----------------- MOOD POPUP -----------------
    private fun showMoodPopup() {
        // Stop analysis to save resources while user interacts with popup
        cameraProvider?.unbindAll()

        val view = layoutInflater.inflate(R.layout.popup_emotion, null)
        val emoji = view.findViewById<TextView>(R.id.dialogEmoji)
        val mood = view.findViewById<TextView>(R.id.dialogMood)
        val quoteText = view.findViewById<TextView>(R.id.dialogQuote)
        val watchVideo = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnWatchVideo)
        val playGame = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnPlayGame)
        val closeBtn = view.findViewById<TextView>(R.id.btnCloseDialog)
        val motivationBtn = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnMotivation)

        emoji.text = currentEmoji
        mood.text = currentEmotion
        val currentQuote = quotes.firstOrNull { it.mood == currentEmotion }?.text ?: "Stay positive!"
        quoteText.text = currentQuote

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        watchVideo.setOnClickListener {
            dialog.dismiss()
            hideCameraOverlay() // Clear camera state before navigation
            val intent = Intent(this, ShortsVideoActivity::class.java)
            intent.putExtra("MOOD", currentEmotion)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_scale, R.anim.fade_out_scale)
        }


        val comfortBtn = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnComfort)
        comfortBtn.setOnClickListener {
            dialog.dismiss()
            hideCameraOverlay()
            val intent = Intent(this, CounselingHubActivity::class.java)
            intent.putExtra("MOOD", currentEmotion)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }
        playGame.setOnClickListener {
            dialog.dismiss()
            hideCameraOverlay()
            openGameBasedOnMood()
        }
        motivationBtn.setOnClickListener {
            dialog.dismiss()
            hideCameraOverlay()
            openMotivationActivity()
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
            isCapturing = false // CRITICAL: Reset flag so user can capture again
            // Restart camera if it was active
            if (binding.previewView.visibility == View.VISIBLE) {
                startCamera()
            }
        }

        // Ensure camera is unbound when navigating away
        dialog.setOnDismissListener {
            // Optional: check if we should really unbind or if it was already unbound
        }

        dialog.show()
    }

    private fun showNoFacePopup() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("No Face Detected")
        builder.setMessage("User face not detected. Capture photo failed.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // ----------------- GAME NAVIGATION -----------------
    private fun openGameBasedOnMood() {
        val intent = when (currentEmotion) {
            "Happy" -> Intent(this, HappyGameActivity::class.java)
            "Sad" -> Intent(this, SadGameActivity::class.java)
            "Angry" -> Intent(this, AngryGameActivity::class.java)
            "Sleepy" -> Intent(this, SleepyGameActivity::class.java)
            else -> Intent(this, HappyGameActivity::class.java)
        }
        startActivity(intent)
    }
    private fun openMotivationActivity() {
        try {
            val intent = Intent(this, MotivationActivity::class.java)
            intent.putExtra("MOOD", currentEmotion.lowercase())
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open Motivation screen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun lightenColor(color: Int, fraction: Double): Int {
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        val alpha = android.graphics.Color.alpha(color)
        val newRed = (red + (255 - red) * fraction).toInt().coerceIn(0, 255)
        val newGreen = (green + (255 - green) * fraction).toInt().coerceIn(0, 255)
        val newBlue = (blue + (255 - blue) * fraction).toInt().coerceIn(0, 255)
        return android.graphics.Color.argb(alpha, newRed, newGreen, newBlue)
    }
}

package com.example.mindmile.users

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R

class ChatActivity : AppCompatActivity() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chatRecyclerView)
        inputField = findViewById(R.id.messageInput)
        val sendButton = findViewById<ImageView>(R.id.sendButton)
        val backButton = findViewById<ImageView>(R.id.backButton)
        val endButton = findViewById<Button>(R.id.btnEndAppointment)

        val mood = intent.getStringExtra("MOOD") ?: "Neutral"
        val counselorName = intent.getStringExtra("COUNSELOR_NAME") ?: "Dr. Counselor"
        val counselorSpecialty = intent.getStringExtra("COUNSELOR_SPECIALTY") ?: "Wellness Expert"

        // Update header name
        findViewById<TextView>(R.id.counselorName).text = counselorName

        // Initial welcome and introduction message
        messages.add(ChatMessage("Hello! I am $counselorName, your $counselorSpecialty. I'm here to support you.", false))
        messages.add(ChatMessage("I noticed you might be feeling $mood. I'm here to listen. How can I help you today?", false))

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backButton.setOnClickListener { finish() }

        sendButton.setOnClickListener {
            val text = inputField.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                inputField.text.clear()
            }
        }

        endButton.setOnClickListener {
            showEndAppointmentDialog()
        }
    }

    private fun showEndAppointmentDialog() {
        val address = "MindMile Wellness Clinic, 123 Tranquility Path, Wellness District, MC 45678"
        val finalMessage = "Thank you for sharing with me today. If you need in-person support, please visit us at:\n\n$address"
        
        AlertDialog.Builder(this)
            .setTitle("End Appointment")
            .setMessage(finalMessage)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setNegativeButton("Show on Map") { _, _ ->
                val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
                finish()
            }
            .show()
    }

    private fun sendMessage(text: String) {
        // User message
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)

        // Simulate bot typing/response delay
        Handler(Looper.getMainLooper()).postDelayed({
            receiveBotResponse(text)
        }, 1500)
    }

    private fun receiveBotResponse(userText: String) {
        val response = getBotResponse(userText.lowercase())
        messages.add(ChatMessage(response, false))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }

    private fun getBotResponse(input: String): String {
        return when {
            input.contains("problem") || input.contains("issue") -> "I understand. Talking about it is the first step. Can you tell me more about what's bothering you?"
            input.contains("sad") || input.contains("unhappy") || input.contains("cry") -> "I'm sorry to hear that. It's okay to feel this way. Have you tried taking deep breaths or drinking some water? I'm here for you."
            input.contains("angry") || input.contains("mad") || input.contains("hate") -> "It sounds like things are frustrating right now. Try counting to 10 or stepping away for a moment. What triggered these feelings?"
            input.contains("anxious") || input.contains("worried") || input.contains("scared") -> "Anxiety can be tough. Focus on the present moment. Look around and name 3 things you see. You are safe here."
            input.contains("happy") || input.contains("good") || input.contains("great") -> "That's wonderful! It's great to cherish these moments. What made you smile today? Joy is worth sharing."
            input.contains("tired") || input.contains("sleepy") || input.contains("exhausted") -> "Rest is important. Maybe it's time for a short nap or some relaxing music? Your body needs time to recharge."
            input.contains("help") -> "I'm here to help. We can talk about your feelings or I can suggest some relaxation techniques. What do you prefer?"
            input.contains("thank") -> "You're very welcome! I'm glad I could be here for you. Is there anything else on your mind?"
            input.contains("hello") || input.contains("hi") -> "Hello there! I'm listening. How has your day been so far?"
            else -> "I see. Tell me more about that. I'm listening and I want to understand."
        }
    }
}

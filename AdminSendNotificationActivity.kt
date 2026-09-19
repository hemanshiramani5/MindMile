package com.example.mindmile.admin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AdminSendNotificationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var edtMessage: TextInputEditText
    private var userId: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_send_notification)

        db = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId")
        userName = intent.getStringExtra("userName")
        val userEmail = intent.getStringExtra("userEmail")

        val txtRecipient = findViewById<TextView>(R.id.txtRecipient)
        edtMessage = findViewById(R.id.edtMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        
        txtRecipient.text = "$userName ($userEmail)"

        findViewById<android.view.View>(R.id.backButton).setOnClickListener { finish() }

        btnSend.setOnClickListener {
            sendNotification()
        }
    }

    private fun sendNotification() {
        val message = edtMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(this, "Error: User ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationData = hashMapOf(
            "userId" to userId,
            "message" to message,
            "timestamp" to Timestamp.now(),
            "read" to false,
            "sender" to "Admin"
        )

        val collectionName = if (userId == "BROADCAST_ALL") "broadcast_notifications" else "notifications"

        db.collection(collectionName)
            .add(notificationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Notification Sent!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

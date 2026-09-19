package com.example.mindmile.users

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class UserNotificationsActivity : AppCompatActivity() {

    data class NotificationItem(
        val docId: String,
        val message: String,
        val timestamp: com.google.firebase.Timestamp,
        val sender: String,
        val isPersonal: Boolean
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private val notificationList = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_notifications)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        emptyState = findViewById(R.id.emptyState)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotificationAdapter(notificationList)
        recyclerView.adapter = adapter

        // Swipe left to delete
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            private val background = ColorDrawable(Color.parseColor("#E53935"))
            private val paint = Paint().apply {
                color = Color.WHITE
                textSize = 42f
                isAntiAlias = true
            }

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return
                val item = notificationList[pos]

                // Show confirmation — restore item immediately, delete after confirm
                adapter.notifyItemChanged(pos)

                androidx.appcompat.app.AlertDialog.Builder(this@UserNotificationsActivity)
                    .setTitle("Delete Notification")
                    .setMessage("Remove this notification?")
                    .setPositiveButton("Delete") { _, _ -> deleteNotification(item, pos) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                background.setBounds(
                    itemView.right + dX.toInt(), itemView.top,
                    itemView.right, itemView.bottom
                )
                background.draw(c)

                // Draw delete label
                val text = "Delete"
                val textWidth = paint.measureText(text)
                val x = itemView.right - textWidth - 48f
                val y = itemView.top + (itemView.height / 2f) + (paint.textSize / 3f)
                c.drawText(text, x, y, paint)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        loadNotifications()

        val startMessage = intent.getStringExtra("start_message")
        if (!startMessage.isNullOrEmpty()) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("New Notification")
                .setMessage(startMessage)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        notificationList.clear()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                @Suppress("UNCHECKED_CAST")
                val dismissed = (userDoc.get("dismissedBroadcasts") as? List<String>) ?: emptyList()
                fetchAll(userId, dismissed)
            }
            .addOnFailureListener { fetchAll(userId = userId, dismissed = emptyList()) }
    }

    private fun fetchAll(userId: String, dismissed: List<String>) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { personalResult ->
                for (doc in personalResult) {
                    val message = doc.getString("message") ?: ""
                    val timestamp = doc.getTimestamp("timestamp") ?: com.google.firebase.Timestamp.now()
                    val sender = doc.getString("sender") ?: "Admin"
                    notificationList.add(NotificationItem(doc.id, message, timestamp, sender, isPersonal = true))
                }
                db.collection("broadcast_notifications").get()
                    .addOnSuccessListener { broadcastResult ->
                        for (doc in broadcastResult) {
                            if (doc.id in dismissed) continue
                            val message = doc.getString("message") ?: ""
                            val timestamp = doc.getTimestamp("timestamp") ?: com.google.firebase.Timestamp.now()
                            val sender = doc.getString("sender") ?: "Admin (Broadcast)"
                            notificationList.add(NotificationItem(doc.id, message, timestamp, sender, isPersonal = false))
                        }
                        notificationList.sortByDescending { it.timestamp }
                        updateEmptyState()
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load broadcasts", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteNotification(item: NotificationItem, position: Int) {
        val userId = auth.currentUser?.uid ?: return

        if (item.isPersonal) {
            db.collection("notifications").document(item.docId)
                .delete()
                .addOnSuccessListener {
                    notificationList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    updateEmptyState()
                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("users").document(userId)
                .update("dismissedBroadcasts", FieldValue.arrayUnion(item.docId))
                .addOnSuccessListener {
                    notificationList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    updateEmptyState()
                    Toast.makeText(this, "Notification dismissed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    db.collection("users").document(userId)
                        .set(mapOf("dismissedBroadcasts" to listOf(item.docId)))
                        .addOnSuccessListener {
                            notificationList.removeAt(position)
                            adapter.notifyItemRemoved(position)
                            updateEmptyState()
                            Toast.makeText(this, "Notification dismissed", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }

    private fun updateEmptyState() {
        if (notificationList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    inner class NotificationAdapter(private val list: MutableList<NotificationItem>) :
        RecyclerView.Adapter<NotificationAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val message: TextView = view.findViewById(R.id.txtMessage)
            val date: TextView = view.findViewById(R.id.txtDate)
            val sender: TextView = view.findViewById(R.id.txtSender)

            fun bind(item: NotificationItem) {
                message.text = item.message
                val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                date.text = dateFormat.format(item.timestamp.toDate())
                sender.text = item.sender

                itemView.setOnClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                        .setTitle("MindMile Message")
                        .setMessage(item.message)
                        .setPositiveButton("Close", null)
                        .show()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_notification, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}

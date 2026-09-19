package com.example.mindmile.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mindmile.R
import com.example.mindmile.users.UserNotificationsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return Result.success()

        val db = FirebaseFirestore.getInstance()
        val prefs = applicationContext.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val lastCheckTime = prefs.getLong("last_check_timestamp", System.currentTimeMillis() - 900000)

        var newMessagesCount = 0
        var latestMessage = ""
        var maxTimestamp = 0L

        try {
            // 1. Check Personal Notifications
            val personalSnapshot = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("timestamp", com.google.firebase.Timestamp(lastCheckTime / 1000, 0))
                .get()
                .await()
            
            newMessagesCount += personalSnapshot.size()
            for (doc in personalSnapshot) {
                val ts = doc.getTimestamp("timestamp")?.seconds ?: 0L
                if (ts > maxTimestamp) {
                    maxTimestamp = ts
                    latestMessage = doc.getString("message") ?: ""
                }
            }

            // 2. Check Broadcast Notifications
            val broadcastSnapshot = db.collection("broadcast_notifications")
                .whereGreaterThan("timestamp", com.google.firebase.Timestamp(lastCheckTime / 1000, 0))
                .get()
                .await()

            newMessagesCount += broadcastSnapshot.size()
            for (doc in broadcastSnapshot) {
                val ts = doc.getTimestamp("timestamp")?.seconds ?: 0L
                if (ts > maxTimestamp) {
                    maxTimestamp = ts
                    latestMessage = doc.getString("message") ?: ""
                }
            }

            if (newMessagesCount > 0) {
                if (latestMessage.isBlank()) latestMessage = "New message received"
                showSystemNotification(newMessagesCount, latestMessage)
            }
            
            prefs.edit().putLong("last_check_timestamp", System.currentTimeMillis()).apply()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }

        return Result.success()
    }

    private fun showSystemNotification(count: Int, message: String) {
        val context = applicationContext
        val channelId = "mindmile_alerts"
        val notificationId = 1001

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MindMile Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, UserNotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("start_message", message)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (count == 1) "New Message" else "New Messages"
        val content = if (count == 1) message else "You have $count new notifications."

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content)) // Expandable text
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setNumber(count)

        notificationManager.notify(notificationId, builder.build())
    }
}

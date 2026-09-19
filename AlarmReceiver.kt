package com.example.mindmile.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mindmile.R
import com.example.mindmile.users.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_HABIT_REMINDER = "com.example.mindmile.ACTION_HABIT_REMINDER"
        const val ACTION_DAILY_SUMMARY = "com.example.mindmile.ACTION_DAILY_SUMMARY"
        const val CHANNEL_ID = "mindmile_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Received action: $action")

        if (action == ACTION_HABIT_REMINDER) {
            val title = intent.getStringExtra("title") ?: "Habit Reminder"
            val message = intent.getStringExtra("message") ?: "Time to work on your habit!"
            val habitIdHash = intent.getIntExtra("habitIdHash", 0)
            
            // Show Notification
            showNotification(context, title, message, habitIdHash)
            
            // Reschedule for next day
            val habitId = intent.getStringExtra("habitId")
            val hour = intent.getIntExtra("hour", -1)
            val minute = intent.getIntExtra("minute", -1)
            
            if (habitId != null && hour != -1 && minute != -1) {
                NotificationHelper.scheduleHabitReminder(context, habitId, title, hour, minute)
                Log.d("AlarmReceiver", "Rescheduled habit $title for next day")
            }

        } else if (action == ACTION_DAILY_SUMMARY) {
            checkAndShowDailySummary(context)
        }
    }

    private fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Ensure this resource exists
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun checkAndShowDailySummary(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("user_habits")
            .whereEqualTo("userId", userId)
            //.whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                var incompleteCount = 0
                val calendar = Calendar.getInstance()
                val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                val todayYear = calendar.get(Calendar.YEAR)

                for (doc in documents) {
                    try {
                        // Check if habit is for TODAY (using startDate logic from MainActivity)
                        val startDate = doc.getTimestamp("startDate") ?: continue
                        val habitCal = Calendar.getInstance()
                        habitCal.time = startDate.toDate()

                        if (habitCal.get(Calendar.YEAR) == todayYear &&
                            habitCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                            
                            val goalValue = (doc.getLong("goalValue") ?: 1).toInt()
                            val completedValue = (doc.getLong("completedValue") ?: 0).toInt()

                            if (completedValue < goalValue) {
                                incompleteCount++
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (incompleteCount > 0) {
                    showNotification(
                        context,
                        "Daily Summary",
                        "You have $incompleteCount incomplete habits today. Let's finish them!",
                        9999 // Fixed ID for summary
                    )
                }
            }
            .addOnFailureListener {
                Log.e("AlarmReceiver", "Failed to fetch habits for summary")
            }
    }
}

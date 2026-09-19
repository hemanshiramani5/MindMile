package com.example.mindmile.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Channel for habit reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(AlarmReceiver.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleHabitReminder(context: Context, habitId: String, title: String, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val now = Calendar.getInstance()
        
        // Strict "Today Only" check
        if (calendar.before(now)) {
            android.widget.Toast.makeText(context, "Time has passed. Alarm NOT set.", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        val i = Intent(android.provider.AlarmClock.ACTION_SET_ALARM)
        i.putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "MindMile: $title")
        i.putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
        i.putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
        i.putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if (i.resolveActivity(context.packageManager) != null) {
            context.startActivity(i)
            android.widget.Toast.makeText(context, "System Alarm set for $hour:$minute", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "No Alarm app found!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun scheduleDailySummary(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DAILY_SUMMARY
        }
        
        // ID 9999 for summary
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d("NotificationHelper", "Scheduled Daily Summary at 8:00 PM")
        } catch (e: Exception) {
             Log.e("NotificationHelper", "Error scheduling summary: ${e.message}")
        }
    }
}

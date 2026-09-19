package com.example.mindmile.model

import com.google.firebase.Timestamp

data class UserHabit(
    val habitId: String? = "",
    val userId: String? = "",
    val originalHabitId: String? = "",
    val title: String? = "",
    val description: String? = "",
    val habitType: String? = "Build", // Build or Quit
    val goalValue: Int? = 0,
    val goalUnit: String? = "",
    val color: String? = "#5E81AC",
    val imageUrl: String? = null,
    val timeRange: String? = "Anytime",
    val remindersEnabled: Boolean? = false,
    val reminderTime: String? = "",
    val startDate: Timestamp? = null,
    val isActive: Boolean? = true
)

package com.example.mindmile.model

data class Habit(
    val icon: String = "",
    val title: String = "",
    val description: String = "",
    val color: String = "",
    val type: String = "",
    val goalPeriod: String = "",
    val goalValue: Int = 0,
    val goalUnit: String = "",
    val taskDays: String = "",
    val timeRanges: List<String> = emptyList(),
    val reminderEnabled: Boolean = false,
    val reminderTime: String = ""
)

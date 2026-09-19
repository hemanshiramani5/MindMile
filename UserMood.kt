package com.example.mindmile.model

import com.google.firebase.Timestamp

data class UserMood(
    val moodId: String? = "",
    val userId: String? = "",
    val userName: String? = "",
    val userEmail: String? = "",
    val moodDrawableName: String? = "",
    val timestamp: Timestamp? = null,
    val date: String? = "" // For easy grouping/filtering (yyyy-MM-dd)
)

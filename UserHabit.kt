package com.example.mindmile.users

data class UserHabit(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var goalValue: Long = 0,
    var goalUnit: String = "",
    var color: String = "#000000",
    var imageUrl: String = "",
    var currentValue: Long = 0
)

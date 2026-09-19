package com.example.mindmile.users

data class QuoteModel(val text: String, val mood: String, val videoUrl: String? = null)

private val allQuotes = listOf(
    Quote("Smile, life is beautiful!", "Happy", "https://www.youtube.com/watch?v=ZbZSe6N_BXs"),
    Quote("Every day is a new beginning.", "Happy", "https://www.youtube.com/watch?v=2OEL4P1Rz04"),
    Quote("Sadness is part of life, embrace it.", "Sad", "https://www.youtube.com/watch?v=ZbZSe6N_BXs"),
    Quote("Relax and calm your mind.", "Neutral", "https://www.youtube.com/watch?v=5qap5aO4i9A")
)

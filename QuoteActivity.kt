package com.example.mindmile.users

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.R

class QuoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote)

        val quoteTextView = findViewById<TextView>(R.id.fullQuoteText)

        val quote = intent.getStringExtra("quote")

        quoteTextView.text = quote ?: "No quote available"
    }
}

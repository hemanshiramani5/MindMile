package com.example.mindmile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.Login

class OpenActivity : AppCompatActivity() {

    private val quotes = listOf(
        "Breathe in peace. Breathe out stress.",
        "Your mind deserves kindness.",
        "Pause. Reflect. Heal.",
        "Every breath is a fresh beginning."
    )

    private var quoteIndex = 0
    private lateinit var txtQuote: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        txtQuote = findViewById(R.id.txtQuote)

        startQuoteAnimation()

        // Redirect to Login after 9 seconds
        handler.postDelayed({
            startActivity(Intent(this, Login::class.java))
            finish()
        }, 8000)
    }

    private fun startQuoteAnimation() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        handler.post(object : Runnable {
            override fun run() {
                txtQuote.startAnimation(fadeOut)

                txtQuote.text = quotes[quoteIndex]
                quoteIndex = (quoteIndex + 1) % quotes.size

                txtQuote.startAnimation(fadeIn)

                handler.postDelayed(this, 2000) // change quote every 2 sec
            }
        })
    }
}

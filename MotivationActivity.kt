package com.example.mindmile.users

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mindmile.R

class MotivationActivity : AppCompatActivity() {

    private val quotes = listOf(
        Quote("The only way to do great work is to love what you do.", "Steve Jobs", R.drawable.motivation),
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", R.drawable.calm1),
        Quote("Your time is limited, don't waste it living someone else's life.", "Steve Jobs", R.drawable.happy1),
        Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt", R.drawable.soft1),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius", R.drawable.relax1),
        Quote("Everything you've ever wanted is on the other side of fear.", "George Addair", R.drawable.happy2),
        Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", R.drawable.soft2),
        Quote("Hardships often prepare ordinary people for an extraordinary destiny.", "C.S. Lewis", R.drawable.calm2),
        Quote("Believe in yourself and all that you are. Know that there is something inside you that is greater than any obstacle.", "Christian D. Larson", R.drawable.bg_motivation),
        Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson", R.drawable.bg_soft)
    )

    data class Quote(val text: String, val author: String, val backgroundRes: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        setContentView(R.layout.activity_motivation)

        val viewPager = findViewById<ViewPager2>(R.id.motivationViewPager)
        val closeButton = findViewById<ImageView>(R.id.closeButton)

        val adapter = MotivationPagerAdapter(quotes)
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        closeButton.setOnClickListener { finish() }
    }

    inner class MotivationPagerAdapter(private val quotes: List<Quote>) :
        RecyclerView.Adapter<MotivationPagerAdapter.QuoteViewHolder>() {

        inner class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val quoteText: TextView = itemView.findViewById(R.id.quoteText)
            val quoteAuthor: TextView = itemView.findViewById(R.id.quoteAuthor)
            val background: ImageView = itemView.findViewById(R.id.quoteBackground)
            val btnShare: ImageView = itemView.findViewById(R.id.btnShare)
            val btnLike: ImageView = itemView.findViewById(R.id.btnLike)

            fun bind(quote: Quote) {
                quoteText.text = quote.text
                quoteAuthor.text = "- ${quote.author}"
                background.setImageResource(quote.backgroundRes)

                // Simple fade in animation for text
                quoteText.alpha = 0f
                quoteText.animate().alpha(1f).setDuration(1000).start()
                
                quoteAuthor.alpha = 0f
                quoteAuthor.animate().alpha(1f).setDuration(1000).setStartDelay(300).start()

                btnShare.setOnClickListener {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\" - ${quote.author} #MindMile")
                    startActivity(Intent.createChooser(shareIntent, "Share Quote"))
                }

                btnLike.setOnClickListener {
                    // Toggle heart state (visual only for now)
                    it.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                        it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                        Toast.makeText(this@MotivationActivity, "Saved to Favorites ❤️", Toast.LENGTH_SHORT).show()
                    }.start()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_fullscreen_quote, parent, false)
            return QuoteViewHolder(view)
        }

        override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
            holder.bind(quotes[position])
        }

        override fun getItemCount(): Int = quotes.size
    }
}
package com.example.mindmile.admin

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.example.mindmile.users.MotivationActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminShowMotivationActivity : AppCompatActivity() {

    data class AdminQuote(
        val id: String,
        val text: String,
        val author: String,
        val imageBase64: String?
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuoteAdapter
    private val quoteList = mutableListOf<AdminQuote>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_show_motivation)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = QuoteAdapter(quoteList)
        recyclerView.adapter = adapter

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        loadQuotes()
    }

    override fun onResume() {
        super.onResume()
        loadQuotes() // Refresh list when returning from Edit
    }

    private fun loadQuotes() {
        db.collection("MotivationTable")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                quoteList.clear()
                for (doc in documents) {
                    val text = doc.getString("text") ?: ""
                    val author = doc.getString("author") ?: ""
                    val imageBase64 = doc.getString("imageBase64")
                    quoteList.add(AdminQuote(doc.id, text, author, imageBase64))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load quotes", Toast.LENGTH_SHORT).show()
            }
    }

    inner class QuoteAdapter(private val list: List<AdminQuote>) : 
        RecyclerView.Adapter<QuoteAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val img: ImageView = view.findViewById(R.id.imgQuoteBg)
            val text: TextView = view.findViewById(R.id.txtQuoteText)
            val author: TextView = view.findViewById(R.id.txtQuoteAuthor)
            val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
            val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_motivation, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = list[position]
            holder.text.text = item.text
            holder.author.text = "- ${item.author}"

            if (item.imageBase64 != null) {
                try {
                    val bytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    holder.img.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    holder.img.setImageResource(R.drawable.bg_motivation)
                }
            } else {
                holder.img.setImageResource(R.drawable.bg_motivation)
            }

            holder.btnEdit.setOnClickListener {
                val intent = Intent(this@AdminShowMotivationActivity, AdminAddMotivationActivity::class.java)
                intent.putExtra("quoteId", item.id)
                intent.putExtra("text", item.text)
                intent.putExtra("author", item.author)
                intent.putExtra("imageBase64", item.imageBase64)
                startActivity(intent)
            }

            holder.btnDelete.setOnClickListener {
                deleteQuote(item.id)
            }
        }

        override fun getItemCount() = list.size
    }

    private fun deleteQuote(id: String) {
        db.collection("MotivationTable").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Quote Deleted", Toast.LENGTH_SHORT).show()
                loadQuotes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }
}

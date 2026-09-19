package com.example.mindmile.admin

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindmile.R
import com.google.firebase.firestore.FirebaseFirestore

class admin_card_show : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private var habitList = mutableListOf<HabitCard>()

    data class HabitCard(
        val docId: String,
        val title: String,
        val description: String,
        val imageBase64: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_card_show)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)

        // Grid with 2 columns
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        loadHabitCards()
    }

    private fun loadHabitCards() {
        db.collection("habit_cards")
            .get()
            .addOnSuccessListener { result ->
                habitList.clear()
                for (document in result) {
                    val title = document.getString("title")?.trim()
                    val description = document.getString("description")?.trim()
                    val imageBase64 = document.getString("imageBase64") ?: ""
                    val docId = document.id
                    if (title.isNullOrEmpty() || description.isNullOrEmpty()) continue

                    habitList.add(HabitCard(docId, title, description, imageBase64))
                }

                recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.admin_card_item, parent, false)
                        return object : RecyclerView.ViewHolder(view) {}
                    }

                    override fun getItemCount(): Int = habitList.size

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        val habit = habitList[position]
                        val view = holder.itemView
                        val img = view.findViewById<ImageView>(R.id.cardImage)
                        val titleTv = view.findViewById<TextView>(R.id.cardTitle)
                        val descTv = view.findViewById<TextView>(R.id.cardDesc)
                        val btnEdit = view.findViewById<ImageView>(R.id.btnEdit)
                        val btnDelete = view.findViewById<ImageView>(R.id.btnDelete)

                        titleTv.text = habit.title
                        descTv.text = habit.description

                        if (habit.imageBase64.isNotEmpty()) {
                            try {
                                val bytes = Base64.decode(habit.imageBase64, Base64.DEFAULT)
                                Glide.with(this@admin_card_show).asBitmap().load(bytes).into(img)
                            } catch (e: Exception) {
                                img.setImageResource(R.drawable.ic_launcher_background)
                            }
                        } else {
                            img.setImageResource(R.drawable.ic_launcher_background)
                        }

                        btnEdit.setOnClickListener {
                            val intent = Intent(this@admin_card_show, admin_edit_card::class.java)
                            intent.putExtra("docId", habit.docId)
                            startActivity(intent)
                        }

                        btnDelete.setOnClickListener {
                            db.collection("habit_cards").document(habit.docId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this@admin_card_show, "Habit deleted", Toast.LENGTH_SHORT).show()
                                    habitList.removeAt(position)
                                    notifyItemRemoved(position)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@admin_card_show, "Failed to delete habit", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load habit cards", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadHabitCards()
    }
}

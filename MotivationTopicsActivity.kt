package com.example.mindmile.users

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore

class MotivationTopicsActivity : AppCompatActivity() {

    data class Topic(val name: String, val bgRes: Int)

    private lateinit var db: FirebaseFirestore
    private val topics = mutableListOf<Topic>()
    private lateinit var adapter: TopicAdapter

    // Pool of backgrounds to rotate through
    private val bgResources = listOf(
        R.drawable.bg_motivation,
        R.drawable.calm1,
        R.drawable.happy1,
        R.drawable.soft1,
        R.drawable.bg_relax,
        R.drawable.soft2,
        R.drawable.happy2
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation_topics)

        db = FirebaseFirestore.getInstance()
        
        val recycler = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TopicAdapter(topics) { topic ->
            val intent = Intent(this, TopicContentActivity::class.java)
            intent.putExtra("TOPIC", topic.name)
            startActivity(intent)
        }
        recycler.adapter = adapter

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        loadDynamicTopics()
    }

    private fun loadDynamicTopics() {
        db.collection("videos")
            .get()
            .addOnSuccessListener { documents ->
                val uniqueCategories = mutableSetOf<String>()
                
                // Add defaults first to ensure they always show if desired
                uniqueCategories.add("Overcoming Fear")
                uniqueCategories.add("Daily Discipline")
                uniqueCategories.add("Self Love & Confidence")
                uniqueCategories.add("Motivational Speakers")

                for (doc in documents) {
                    val category = doc.getString("category")
                    if (!category.isNullOrEmpty()) {
                        uniqueCategories.add(category)
                    }
                }

                topics.clear()
                uniqueCategories.forEachIndexed { index, categoryName ->
                    val bgRes = bgResources[index % bgResources.size]
                    topics.add(Topic(categoryName, bgRes))
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load topics: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class TopicAdapter(private val list: List<Topic>, private val onClick: (Topic) -> Unit) :
        RecyclerView.Adapter<TopicAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.textTopic)
            val bg: ImageView = view.findViewById(R.id.imgBackground)
            val card: View = view.findViewById(R.id.cardTopic)

            fun bind(item: Topic) {
                text.text = item.name
                bg.setImageResource(item.bgRes)
                card.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic_card, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}

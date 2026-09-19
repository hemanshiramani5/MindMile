package com.example.mindmile.users

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R

class CounselorsActivity : AppCompatActivity() {

    data class Counselor(val name: String, val specialty: String, val quote: String, val imageRes: Int)

    private val counselors = listOf(
        Counselor("Dr. Emily Carter", "Anxiety & Stress Expert", "Healing happens when we allow ourselves to feel.", R.drawable.ic_profile),
        Counselor("Dr. Mark Wilson", "Depression & CBT Specialist", "Small steps everyday lead to big changes.", R.drawable.ic_profile), // Using same for now for safety if copies failed
        Counselor("Sarah Jenkins", "Life Coach & Motivation", "Your potential is limitless.", R.drawable.ic_profile),
        Counselor("David Chen", "Mindfulness Instructor", "Peace comes from within.", R.drawable.ic_profile)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counselors)

        val backButton = findViewById<View>(R.id.backButton)
        val recyclerView = findViewById<RecyclerView>(R.id.counselorsRecyclerView)

        backButton.setOnClickListener { 
            finish()
            overridePendingTransition(R.anim.fade_out_scale, R.anim.fade_in_scale)
        }

        val mood = intent.getStringExtra("MOOD") ?: "Neutral"

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CounselorAdapter(counselors) { counselor ->
            val intent = Intent(this, CounselorProfileActivity::class.java)
            intent.putExtra("NAME", counselor.name)
            intent.putExtra("SPECIALTY", counselor.specialty)
            intent.putExtra("QUOTE", counselor.quote)
            intent.putExtra("IMAGE_RES", counselor.imageRes)
            intent.putExtra("MOOD", mood)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }
    }

    inner class CounselorAdapter(
        private val list: List<Counselor>,
        private val onClick: (Counselor) -> Unit
    ) : RecyclerView.Adapter<CounselorAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.textName)
            val specialty: TextView = view.findViewById(R.id.textSpecialty)
            val image: ImageView = view.findViewById(R.id.imgCounselor)
            val card: View = view.findViewById(R.id.cardCounselor)

            fun bind(item: Counselor) {
                name.text = item.name
                specialty.text = item.specialty
                image.setImageResource(item.imageRes)
                card.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_counselor, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}

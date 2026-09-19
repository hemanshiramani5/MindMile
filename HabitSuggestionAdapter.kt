package com.example.mindmile.users

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R

data class HabitSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val imageBase64: String?
)

class HabitSuggestionAdapter(
    private val suggestions: List<HabitSuggestion>,
    private val onHabitClick: (HabitSuggestion) -> Unit
) : RecyclerView.Adapter<HabitSuggestionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvHabitTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvHabitDescription)
        val ivIcon: ImageView = view.findViewById(R.id.ivHabitIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = suggestions[position]
        holder.tvTitle.text = habit.title
        holder.tvDescription.text = habit.description

        if (!habit.imageBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(habit.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.ivIcon.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivIcon.setImageResource(R.drawable.ic_image_placeholder)
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_image_placeholder)
        }

        // Entrance animation
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation_fall_down)

        holder.itemView.setOnClickListener {
            onHabitClick(habit)
        }
    }

    override fun getItemCount() = suggestions.size
}

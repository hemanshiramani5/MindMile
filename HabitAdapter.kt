package com.example.mindmile.users

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R

class HabitAdapter(private val habits: List<UserHabit>) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvHabitTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvHabitDescription)
        val ivIcon: ImageView = view.findViewById(R.id.ivHabitImage)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_card, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.tvTitle.text = habit.title
        holder.tvSubtitle.text = "${habit.currentValue} / ${habit.goalValue} ${habit.goalUnit}"

        // Since the new layout doesn't have a color indicator or progress bar visible by default,
        // we might not need to set them here, or we can repurpose existing views.
        // For now, let's hide the Edit button since this is the user view (adapter), not admin.
        holder.btnEdit.visibility = View.GONE

        // Image
        if (habit.imageUrl.isNotEmpty()) {
            try {
                val bytes = Base64.decode(habit.imageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.ivIcon.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivIcon.setImageResource(R.drawable.ic_image_placeholder)
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_image_placeholder)
        }
    }

    override fun getItemCount() = habits.size
}

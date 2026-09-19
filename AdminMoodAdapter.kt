package com.example.mindmile.admin

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.example.mindmile.model.UserMood
import java.text.SimpleDateFormat
import java.util.Locale

class AdminMoodAdapter(private val moods: List<UserMood>) :
    RecyclerView.Adapter<AdminMoodAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvUserInitial: TextView = view.findViewById(R.id.tvUserInitial)
        val ivMoodIcon: ImageView = view.findViewById(R.id.ivMoodIcon)
        val tvMoodName: TextView = view.findViewById(R.id.tvMoodName)
        val userAvatar: View = view.findViewById(R.id.ivUserAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_mood_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mood = moods[position]

        holder.tvUserName.text = mood.userName ?: "Unknown User"
        holder.tvUserEmail.text = mood.userEmail ?: "No Email"
        holder.tvUserInitial.text = (mood.userName ?: "U").first().uppercase()
        
        // Random background for avatar
        val colors = listOf("#4285F4", "#34A853", "#FBBC05", "#EA4335", "#673AB7", "#FF9800")
        val color = Color.parseColor(colors[position % colors.size])
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.setColor(color)
        holder.userAvatar.background = shape

        // Date display
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateDisplay = mood.timestamp?.let { dateFormat.format(it.toDate()) } ?: (mood.date ?: "No Date")
        holder.tvDate.text = "Date: $dateDisplay"

        // Mood Icon and Name
        val moodName = (mood.moodDrawableName ?: "ic_emoji")
            .replace("ic_mood_", "")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        holder.tvMoodName.text = if (moodName == "ic_emoji") "Default" else moodName

        val context = holder.itemView.context
        val resourceId = context.resources.getIdentifier(mood.moodDrawableName ?: "ic_emoji", "drawable", context.packageName)
        if (resourceId != 0) {
            holder.ivMoodIcon.setImageResource(resourceId)
        } else {
            holder.ivMoodIcon.setImageResource(R.drawable.ic_emoji)
        }
    }

    override fun getItemCount() = moods.size
}

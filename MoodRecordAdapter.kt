package com.example.mindmile.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import java.text.SimpleDateFormat
import java.util.Locale

import com.google.firebase.Timestamp

data class MoodRecord(
    val date: String, 
    val moodDrawableName: String, 
    val timestamp: Timestamp? = null
)

class MoodRecordAdapter(private val records: List<MoodRecord>) :
    RecyclerView.Adapter<MoodRecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivMoodIcon: ImageView = view.findViewById(R.id.ivMoodIcon)
        val tvMoodName: TextView = view.findViewById(R.id.tvMoodName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        
        // Format mood name (e.g., ic_mood_excellent -> Excellent)
        val moodName = record.moodDrawableName
            .replace("ic_mood_", "")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        holder.tvMoodName.text = if (moodName == "ic_emoji") "Default" else moodName
        holder.tvDate.text = formatDate(record.date)

        val context = holder.itemView.context
        val resourceId = context.resources.getIdentifier(record.moodDrawableName, "drawable", context.packageName)
        if (resourceId != 0) {
            holder.ivMoodIcon.setImageResource(resourceId)
        } else {
            holder.ivMoodIcon.setImageResource(R.drawable.ic_emoji)
        }
    }

    override fun getItemCount() = records.size

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}

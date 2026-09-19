package com.example.mindmile.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R

data class ChatMessage(val text: String, val isSentByUser: Boolean)

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedLayout: LinearLayout = itemView.findViewById(R.id.receivedLayout)
        val receivedText: TextView = itemView.findViewById(R.id.receivedText)
        val sentText: TextView = itemView.findViewById(R.id.sentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.isSentByUser) {
            holder.receivedLayout.visibility = View.GONE
            holder.sentText.visibility = View.VISIBLE
            holder.sentText.text = message.text
        } else {
            holder.sentText.visibility = View.GONE
            holder.receivedLayout.visibility = View.VISIBLE
            holder.receivedText.text = message.text
        }
    }

    override fun getItemCount(): Int = messages.size
}

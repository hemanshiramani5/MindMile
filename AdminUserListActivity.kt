package com.example.mindmile.admin

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

class AdminUserListActivity : AppCompatActivity() {

    data class UserItem(
        val id: String,
        val name: String,
        val email: String
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private val userList = mutableListOf<UserItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_list)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        findViewById<View>(R.id.btnBroadcast).setOnClickListener {
            val intent = Intent(this, AdminSendNotificationActivity::class.java)
            intent.putExtra("userId", "BROADCAST_ALL")
            intent.putExtra("userName", "All Users")
            intent.putExtra("userEmail", "broadcast@mindmile.com")
            startActivity(intent)
        }

        loadUsers()
    }

    private fun loadUsers() {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (document in result) {
                    val name = document.getString("username") ?: document.getString("name") 
                        ?: document.getString("fullName") ?: "Unknown User"
                    val email = document.getString("email") ?: "No Email"
                    userList.add(UserItem(document.id, name, email))
                }
                recyclerView.adapter = UserAdapter(userList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    inner class UserAdapter(private val users: List<UserItem>) :
        RecyclerView.Adapter<UserAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.userName)
            val email: TextView = view.findViewById(R.id.userEmail)
            val initial: TextView = view.findViewById(R.id.userInitial)
            
            fun bind(user: UserItem) {
                name.text = user.name
                email.text = user.email
                initial.text = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                
                itemView.setOnClickListener {
                    val intent = Intent(this@AdminUserListActivity, AdminSendNotificationActivity::class.java)
                    intent.putExtra("userId", user.id)
                    intent.putExtra("userName", user.name)
                    intent.putExtra("userEmail", user.email)
                    startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_user, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemCount() = users.size
    }
}

package com.example.mindmile.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmile.Login
import com.example.mindmile.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class admin_dashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // TOP BAR ICONS
        val notificationIcon = findViewById<ImageView>(R.id.notificationIcon)
        val logoutIcon = findViewById<ImageView>(R.id.logoutIcon)

        // STATS VIEWS
        val usersCountText = findViewById<TextView>(R.id.totalUsersCount)
        val videosCountText = findViewById<TextView>(R.id.totalVideosCount)
        val habitsCountText = findViewById<TextView>(R.id.totalHabitsCount)

        // ADMIN CARD CLICKS (Now CardViews in new layout)
        val addHabitCard = findViewById<View>(R.id.addHabitCard)
        val manageHabitCard = findViewById<View>(R.id.manageHabitCard)
        val viewUsersCard = findViewById<View>(R.id.viewUsersCard)
        val addVideoCard = findViewById<View>(R.id.addVideoCard)

        // BOTTOM NAV ICONS
        val bookIcon = findViewById<ImageView>(R.id.bookIcon)
        val listIcon = findViewById<ImageView>(R.id.listIcon)
        val addIcon = findViewById<ImageView>(R.id.addIcon)
        val groupIcon = findViewById<ImageView>(R.id.groupIcon)
        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)

        // ----------------------------------------------------
        //                FETCH STATISTICS
        // ----------------------------------------------------
        
        // Fetch Users Count
        db.collection("users").get().addOnSuccessListener { snapshot ->
            usersCountText.text = snapshot.size().toString()
        }.addOnFailureListener { 
            usersCountText.text = "0" 
        }

        // Fetch Videos Count
        db.collection("videos").get().addOnSuccessListener { snapshot ->
            videosCountText.text = snapshot.size().toString()
        }.addOnFailureListener { 
            videosCountText.text = "0" 
        }

        // Fetch Habits Count
        db.collection("habits").get().addOnSuccessListener { snapshot ->
            habitsCountText.text = snapshot.size().toString()
        }.addOnFailureListener { 
            habitsCountText.text = "0" 
        }

        // ----------------------------------------------------
        //                TOP BAR ACTIONS
        // ----------------------------------------------------

        notificationIcon.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        logoutIcon.setOnClickListener {
            auth.signOut()
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // ----------------------------------------------------
        //                ADMIN CARD ACTIONS
        // ----------------------------------------------------

        addHabitCard.setOnClickListener {
            startActivity(Intent(this, admin_add_habits::class.java))
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }

        manageHabitCard.setOnClickListener {
            startActivity(Intent(this, admin_show_habits::class.java))
        }

        viewUsersCard.setOnClickListener {
            Toast.makeText(this, "User Management is coming soon!", Toast.LENGTH_SHORT).show()
        }

        addVideoCard.setOnClickListener {
            startActivity(Intent(this, AdminAddVideoActivity::class.java))
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }
        
        // Manage Videos Card
        val manageVideosCard = findViewById<View>(R.id.manageVideosCard)
        manageVideosCard.setOnClickListener {
            startActivity(Intent(this, AdminVideoListActivity::class.java))
            overridePendingTransition(R.anim.fade_in_scale, R.anim.fade_out_scale)
        }

        // ----------------------------------------------------
        //                 BOTTOM MENU ACTIONS
        // ----------------------------------------------------

        bookIcon.setOnClickListener {
            Toast.makeText(this, "Documentation", Toast.LENGTH_SHORT).show()
        }

        listIcon.setOnClickListener {
            startActivity(Intent(this, admin_cards::class.java))
        }

        addIcon.setOnClickListener {
            startActivity(Intent(this, admin_habits::class.java))
        }

        groupIcon.setOnClickListener {
            Toast.makeText(this, "Communities", Toast.LENGTH_SHORT).show()
        }

        settingsIcon.setOnClickListener {
            Toast.makeText(this, "Admin Settings", Toast.LENGTH_SHORT).show()
        }
    }
}

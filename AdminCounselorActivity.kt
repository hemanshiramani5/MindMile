package com.example.mindmile.admin

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmile.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class AdminCounselorActivity : AppCompatActivity() {

    data class Counselor(val docId: String, val name: String, val specialty: String, val quote: String)

    private lateinit var db: FirebaseFirestore
    private val counselorList = mutableListOf<Counselor>()
    private lateinit var adapter: CounselorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_counselor)

        db = FirebaseFirestore.getInstance()

        val recycler = findViewById<RecyclerView>(R.id.recyclerCounselors)
        val emptyState = findViewById<TextView>(R.id.emptyState)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddCounselor)

        adapter = CounselorAdapter(counselorList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Swipe left to delete
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val background = ColorDrawable(Color.parseColor("#E53935"))
            private val paint = Paint().apply { color = Color.WHITE; textSize = 40f; isAntiAlias = true }

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                adapter.notifyItemChanged(pos)
                val item = counselorList[pos]
                AlertDialog.Builder(this@AdminCounselorActivity)
                    .setTitle("Delete Counselor")
                    .setMessage("Remove \"${item.name}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.collection("counselors").document(item.docId)
                            .delete()
                            .addOnSuccessListener {
                                counselorList.removeAt(pos)
                                adapter.notifyItemRemoved(pos)
                                updateEmptyState(emptyState, recycler)
                                Toast.makeText(this@AdminCounselorActivity, "Counselor deleted", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@AdminCounselorActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean) {
                val iv = viewHolder.itemView
                background.setBounds(iv.right + dX.toInt(), iv.top, iv.right, iv.bottom)
                background.draw(c)
                val text = "Delete"
                val x = iv.right - paint.measureText(text) - 48f
                val y = iv.top + iv.height / 2f + paint.textSize / 3f
                c.drawText(text, x, y, paint)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler)

        // Back button
        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        // FAB — Add counselor dialog
        fab.setOnClickListener { showAddDialog(emptyState, recycler) }

        // Load counselors from Firestore
        loadCounselors(emptyState, recycler)
    }

    private fun loadCounselors(emptyState: TextView, recycler: RecyclerView) {
        db.collection("counselors").get()
            .addOnSuccessListener { result ->
                counselorList.clear()
                for (doc in result) {
                    counselorList.add(
                        Counselor(
                            docId = doc.id,
                            name = doc.getString("name") ?: "",
                            specialty = doc.getString("specialty") ?: "",
                            quote = doc.getString("quote") ?: ""
                        )
                    )
                }
                adapter.notifyDataSetChanged()
                updateEmptyState(emptyState, recycler)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load counselors", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddDialog(emptyState: TextView, recycler: RecyclerView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_counselor, null)
        val etName      = dialogView.findViewById<EditText>(R.id.etCounselorName)
        val etSpecialty = dialogView.findViewById<EditText>(R.id.etCounselorSpecialty)
        val etQuote     = dialogView.findViewById<EditText>(R.id.etCounselorQuote)

        AlertDialog.Builder(this)
            .setTitle("Add Counselor")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name      = etName.text.toString().trim()
                val specialty = etSpecialty.text.toString().trim()
                val quote     = etQuote.text.toString().trim()

                if (name.isEmpty() || specialty.isEmpty()) {
                    Toast.makeText(this, "Name and Specialty are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val data = hashMapOf("name" to name, "specialty" to specialty, "quote" to quote)
                db.collection("counselors").add(data)
                    .addOnSuccessListener { docRef ->
                        val newItem = Counselor(docRef.id, name, specialty, quote)
                        counselorList.add(newItem)
                        adapter.notifyItemInserted(counselorList.size - 1)
                        updateEmptyState(emptyState, recycler)
                        Toast.makeText(this, "Counselor added!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEmptyState(emptyState: TextView, recycler: RecyclerView) {
        emptyState.visibility = if (counselorList.isEmpty()) View.VISIBLE else View.GONE
        recycler.visibility   = if (counselorList.isEmpty()) View.GONE  else View.VISIBLE
    }

    inner class CounselorAdapter(private val list: List<Counselor>) :
        RecyclerView.Adapter<CounselorAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val name:      TextView = view.findViewById(R.id.tvCounselorName)
            val specialty: TextView = view.findViewById(R.id.tvCounselorSpecialty)
            val quote:     TextView = view.findViewById(R.id.tvCounselorQuote)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_counselor, parent, false)
            return Holder(v)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = list[position]
            holder.name.text      = item.name
            holder.specialty.text = item.specialty
            holder.quote.text     = if (item.quote.isNotEmpty()) "\"${item.quote}\"" else ""
        }

        override fun getItemCount() = list.size
    }
}

package com.example.moodly

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import java.time.LocalDate


class UserDiaryRvAdapter(

    private val userDiary: ArrayList<UserDiaryFormat>,
    val journalClass: Journal
) : RecyclerView.Adapter<UserDiaryRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_diary_rv_item,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = userDiary[userDiary.size - position - 1]
        holder.urDate.text = currentItem.dateDiary
        holder.urContent.text = currentItem.contentDiary.plus("\n\n")
        for (stag in currentItem.tagsDiary.indices){
            if(stag==currentItem.tagsDiary.size-1){
                holder.urContent.text=(holder.urContent.text.toString()).plus("#").plus(currentItem.tagsDiary[stag]).plus("\n")
            }else{
                holder.urContent.text=(holder.urContent.text.toString()).plus("#").plus(currentItem.tagsDiary[stag]).plus(", ")
            }
        }
        holder.urTB2.visibility = View.GONE
        holder.urMood.setImageDrawable(currentItem.moodDiary)

        if (position % 2 != 0) {
            holder.urTB1.setBackgroundColor(Color.parseColor("#FFE0E5F7"))
            holder.urTB2.setBackgroundResource(R.drawable.top_border_bg2)
        }

        holder.itemView.setOnClickListener {
            if (holder.urTB2.visibility == View.GONE) {
                holder.urTB2.visibility = View.VISIBLE
            } else {
                holder.urTB2.visibility = View.GONE
            }
        }

        holder.btnMore.setOnClickListener {
            showOptionsDialog(holder.itemView.context, currentItem.completeDate)
        }
    }

    override fun getItemCount(): Int {
        return userDiary.size
    }

    private fun showOptionsDialog(context: Context, date: LocalDate) {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        MaterialAlertDialogBuilder(context)
            .setTitle("More Options :")
            .setItems(arrayOf("Edit Entry", "Remove Entry")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(context, WriteJournal::class.java)
                        intent.putExtra("year", year)
                        intent.putExtra("month", month)
                        intent.putExtra("day", day)

                        Log.d("Test", "Editing $date")

                        context.startActivity(intent)
                    }
                    1 -> {
                        // Delete the entry from Firebase database
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val database = Firebase.database.reference.child(userId).child("JournalEntries").child(year.toString()).child(month.toString()).child(day.toString())
                            database.removeValue()
                                .addOnSuccessListener {
                                    Log.d("Test", "Entry deleted successfully")
                                }
                                .addOnFailureListener {
                                    Log.e("Test", "Error deleting entry: ${it.message}")
                                }

                            journalClass.loadOri(userId)
                        }
                    }
                }
            }
            .show()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urDate: TextView = itemView.findViewById(R.id.idTVDate)
        val urMood: ImageView = itemView.findViewById(R.id.idTVMood)
        val urContent: TextView = itemView.findViewById(R.id.idTVContent)
        val urTB2: TableRow = itemView.findViewById(R.id.tbhide)
        val urTB1: TableRow = itemView.findViewById(R.id.tbshow)
        val btnMore: ImageButton = itemView.findViewById(R.id.btn_more)
    }

    fun updateData(newData: List<UserDiaryFormat>) {
        userDiary.clear()
        userDiary.addAll(newData)
        notifyDataSetChanged()
    }
}

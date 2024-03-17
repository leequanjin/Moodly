package com.example.moodly

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UserDiaryRvAdapter(
    private val userDiary: ArrayList<UserDiaryFormat>
) : RecyclerView.Adapter<UserDiaryRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_diary_rv_item,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = userDiary[position]
        holder.urDate.text = currentItem.dateDiary
        holder.urContent.text = currentItem.contentDiary
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
    }

    override fun getItemCount(): Int {
        return userDiary.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urDate: TextView = itemView.findViewById(R.id.idTVDate)
        val urMood: ImageView = itemView.findViewById(R.id.idTVMood)
        val urContent: TextView = itemView.findViewById(R.id.idTVContent)
        val urTB2: TableRow = itemView.findViewById(R.id.tbhide)
        val urTB1: TableRow = itemView.findViewById(R.id.tbshow)
    }

    fun updateData(newData: List<UserDiaryFormat>) {
        userDiary.clear()
        userDiary.addAll(newData)
        notifyDataSetChanged()
    }
}

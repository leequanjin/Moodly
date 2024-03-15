package com.example.moodly

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        if (userDiary.size > 0) {
            holder.urDate.text = (userDiary.get(position).dateDiary)
            var emote= Html.fromHtml(userDiary.get(position).moodDiary)
            holder.urMood.text = (emote.toString())
        }
    }

    override fun getItemCount(): Int {
        return userDiary.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urDate: TextView = itemView.findViewById(R.id.idTVDate)
        val urMood: TextView = itemView.findViewById(R.id.idTVMood)
    }
}
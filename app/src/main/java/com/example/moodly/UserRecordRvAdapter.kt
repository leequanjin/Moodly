package com.example.moodly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class UserRecordRvAdapter(
    private val userRecord: ArrayList<UserRecordFormat>
) : RecyclerView.Adapter<UserRecordRvAdapter.ViewHolder>() {

    lateinit var userDiaryRVAdapter: UserDiaryRvAdapter
    lateinit var userDiary: ArrayList<UserDiaryFormat>

    //test
    //test

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_record_rv_item2,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if ((userRecord != null) and (userRecord.size > 0)) {
            holder.urIndex.text = (userRecord.get(position).months+" "+userRecord.get(position).rid.toString())

            //Nested Adapter
            userDiary = userRecord.get(position).diary

            holder.userDiaryRV.layoutManager = LinearLayoutManager(holder.itemView.context)
            userDiaryRVAdapter = UserDiaryRvAdapter(userDiary)
            holder.userDiaryRV.adapter = userDiaryRVAdapter

            //test end
        }
    }

    override fun getItemCount(): Int {
        return userRecord.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urIndex: TextView = itemView.findViewById(R.id.idTVIndex)
        val userDiaryRV: RecyclerView = itemView.findViewById(R.id.idRVDiary)
    }
}

package com.example.moodly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class UserRecordRvAdapter(private var userRecord: ArrayList<UserRecordFormat>) : RecyclerView.Adapter<UserRecordRvAdapter.ViewHolder>() {

    lateinit var userDiaryRVAdapter: UserDiaryRvAdapter
    lateinit var userDiary: ArrayList<UserDiaryFormat>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_record_rv_item2,
            parent, false
        )
        return ViewHolder(itemView)
    }
    fun filterList(filterlist: ArrayList<UserRecordFormat>) {
        // below line is to add our filtered
        // list in our course array list.
        userRecord = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (userRecord.size > 0) {
            val currentRecord = userRecord[userRecord.size - position - 1]
            holder.urIndex.text = "${currentRecord.months} ${currentRecord.rid}"

            //Nested Adapter
            holder.userDiaryRV.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.userDiaryRV.adapter = holder.userDiaryRVAdapter // Set the adapter

            // Pass the data to the nested adapter
            holder.userDiaryRVAdapter.updateData(currentRecord.diary)

            //test end
        }
    }

    override fun getItemCount(): Int {
        return userRecord.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urIndex: TextView = itemView.findViewById(R.id.idTVIndex)
        val userDiaryRV: RecyclerView = itemView.findViewById(R.id.idRVDiary)

        // Initialize UserDiaryRvAdapter here
        val userDiaryRVAdapter = UserDiaryRvAdapter(ArrayList())
    }
}

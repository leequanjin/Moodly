package com.example.moodly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodly.botpress.conversationData
import com.example.moodly.botpress.conversationTime
import com.example.moodly.databinding.RowsBotChatBinding
import com.example.moodly.databinding.RowsUserChatBinding
import java.time.format.DateTimeFormatter

const val BOT_CHAT = 0
const val USER_CHAT = 1

class ChatAdapter(private val conversationList: List<conversationData>, private val conversationTimeList: List<conversationTime>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    inner class chatBotConHolder(val binding: RowsBotChatBinding): RecyclerView.ViewHolder(binding.root){
        fun bindBot(conversationData: conversationData, conversationTime: conversationTime){
            binding.txtBotChat.text = conversationData.msg.replace("!!!WaitingInput!!!", "").replace("!!!SessionEnd!!!", "").replace("!!!PassJournal!!!", "").trim()
            binding.txtTime.text = conversationTime.time.format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    }

    inner class chatUserConHolder(val binding: RowsUserChatBinding): RecyclerView.ViewHolder(binding.root){
        fun bindUser(conversationData: conversationData, conversationTime: conversationTime){
            binding.txtUserChat.text = conversationData.msg.trim()
            binding.txtTime.text = conversationTime.time.format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(conversationList[position].role == "bot"){
            return BOT_CHAT
        }else{
            return USER_CHAT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == BOT_CHAT){
            val binding = RowsBotChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return chatBotConHolder(binding)
        }else{
            val binding = RowsUserChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return chatUserConHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == BOT_CHAT){
            (holder as chatBotConHolder).bindBot(conversationList[position], conversationTimeList[position])
        }else{
            (holder as chatUserConHolder).bindUser(conversationList[position], conversationTimeList[position])
        }
    }

}
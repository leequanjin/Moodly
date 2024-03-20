package com.example.moodly

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodly.databinding.ActivityChatbotBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class chatbot : AppCompatActivity() {
    lateinit var binding: ActivityChatbotBinding

    lateinit var chatHistory: RecyclerView

    val BOT_AI = botpress()

    val adapter = ChatAdapter(BOT_AI.conversation, BOT_AI.conversationTimeRecord)

    lateinit var edtChat: EditText
    lateinit var btnSend: ImageButton
    lateinit var btnAction: ImageButton

    lateinit var SLD: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.rvChatbot.layoutManager = LinearLayoutManager(this)

        binding.rvChatbot.adapter = adapter

        edtChat = findViewById(R.id.edtChat)
        btnSend = findViewById(R.id.btnSend)
        btnAction = findViewById(R.id.btnAction)

        SLD = SaveLoadData()
        SLD.LoadData(this)

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        lifecycleScope.launch {
            val result =  startConversationCoroutine(scope)
        }

        btnSend.setOnClickListener{
            val chat = edtChat.text.toString()
            if(chat.isNotEmpty()){
                lifecycleScope.launch {
                    val result =  continueConversationCoroutine(scope, chat)
                }
            }

            edtChat.text.clear()
        }

        btnAction.setOnClickListener{
            val action = arrayOf("I want to write a journal", "How to write a journal", "Stop")

            MaterialAlertDialogBuilder(this)
                .setTitle("Action")
                .setItems(action) { _, which ->
                    lifecycleScope.launch {
                        val result =  continueConversationCoroutine(scope, action[which])
                    }
                    //updateMood(selectedMood)
                }
                .show()
        }
    }

    private fun startConversationCoroutine(scope: CoroutineScope) {
        val job = scope.launch {
            runOnUiThread{
                edtChat.isEnabled = false
                edtChat.hint = "Waiting for reply"
                btnSend.isEnabled = false
                btnAction.isEnabled = false
            }

            BOT_AI.createNewUser()
            BOT_AI.createConversation()

            BOT_AI.username = SLD.username

            BOT_AI.createMessage(SLD.username)
            delay(2000)

            while (BOT_AI.getMessage()){
                delay(1000)
            }

            runOnUiThread {
                edtChat.isEnabled = true
                edtChat.hint = ""
                btnSend.isEnabled = true
                btnAction.isEnabled = true

                adapter.notifyDataSetChanged()
            }

            val extras = intent.extras
            val journal = extras?.getString("journal");

            println("pass journal: $journal")

            if(!journal.isNullOrEmpty()){
                lifecycleScope.launch {
                    val result =  continueConversationCoroutine(scope, journal.toString())
                }
            }
        }

        // use job.cancel() for cancelling the job or use job.join() for waiting for the job to finish
    }

    private fun continueConversationCoroutine(scope: CoroutineScope, chat: String) {
        val job = scope.launch {
            runOnUiThread{
                edtChat.isEnabled = false
                edtChat.hint = "Waiting for reply"
                btnSend.isEnabled = false
                btnAction.isEnabled = false
            }

            BOT_AI.createMessage(chat)

            runOnUiThread {
                adapter.notifyDataSetChanged()
            }

            while (BOT_AI.getMessage()){
                delay(1000)
            }

            runOnUiThread {
                edtChat.isEnabled = true
                edtChat.hint = ""
                btnSend.isEnabled = true
                btnAction.isEnabled = true

                adapter.notifyDataSetChanged()

                if(BOT_AI.conversation.last().msg.contains("!!!SessionEnd!!!")){
                    if(BOT_AI.conversation.get(BOT_AI.conversation.size - 2).msg.contains("!!!PassJournal!!!")){
                        val botJournal = BOT_AI.conversation.get(BOT_AI.conversation.size - 2).msg.replace("!!!PassJournal!!!", "")

                        println("Bot written journal: $botJournal")

                        val output = Intent()
                        output.putExtra("journal", botJournal)
                        output.putExtra("mood", BOT_AI.conversation.get(BOT_AI.conversation.size - 3).msg.replace("mood: ", ""))

                        setResult(RESULT_OK, output)
                    }

                    edtChat.isEnabled = false
                    edtChat.hint = "Redirect back to journal page in 4 seconds"
                    btnSend.isEnabled = false
                    btnAction.isEnabled = false
                }
            }

            if(BOT_AI.conversation.last().msg.contains("!!!SessionEnd!!!")){
                delay(4000)
                finish()
            }
        }

        // use job.cancel() for cancelling the job or use job.join() for waiting for the job to finish
    }
}
package com.example.moodly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moodly.databinding.ActivityWriteJournalBinding

class WriteJournal : AppCompatActivity() {

    private lateinit var binding: ActivityWriteJournalBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
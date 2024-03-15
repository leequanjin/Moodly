package com.example.moodly

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.moodly.databinding.ActivityWriteJournalBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class WriteJournal : AppCompatActivity() {

    private lateinit var binding: ActivityWriteJournalBinding
    private lateinit var database: DatabaseReference

    data class JournalEntry(
        val content: String,
        val date: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // connect to firebase
        database = Firebase.database.reference

        binding.btnSave.setOnClickListener {
            val date = binding.tvDate.toString()
            val content = binding.etContent.toString()

            database.child("DiaryRecords").child("Mar2024").child("Content").setValue(content)
            database.child("DiaryRecords").child("Mar2024").child("Content").setValue(date)
        }

        // enhance confirmation dialog
        binding.btnEnhance.setOnClickListener {
            showDialog()
        }
    }



    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheet_layout)
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setDimAmount(0.1f)
    }
}
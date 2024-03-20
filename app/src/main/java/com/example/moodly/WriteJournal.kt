package com.example.moodly

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.moodly.databinding.ActivityWriteJournalBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


data class JournalEntry(
    val date: String? = null,
    val content: String? = null,
    val mood: String? = null,
    val tags: List<String>? = null
)
class WriteJournal : AppCompatActivity() {
    private val RQ_SPEECH_REC = 102

    private lateinit var binding: ActivityWriteJournalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var SLD: SaveLoadData

    private val presetTags = arrayOf("Work", "Travel", "Food", "Health", "Relationships")
    private var selectedTags = ArrayList<String>()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val returnData = result.data?.getStringExtra("journal")
                val mood = result.data?.getStringExtra("mood")
                print("pass back: $returnData")

                if (returnData != "") {
                    binding.etContent.setText(returnData)
                    updateMood(mood.toString())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // connect to firebase
        auth = Firebase.auth
        database = Firebase.database.reference
        SLD = SaveLoadData()
        SLD.LoadData(this)

        val selectedYear = intent.getIntExtra("year", -1)
        val selectedMonth = intent.getIntExtra("month", -1)
        val selectedDay = intent.getIntExtra("day", -1)

        Log.d("Journey", "The selected date is $selectedYear-$selectedMonth-$selectedDay")

        // Retrieve and populate data from Firebase for the selected date
        retrieveAndPopulateDataFromFirebase(selectedYear.toString(),
            selectedMonth.toString(), selectedDay.toString()
        )

        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay)

        // update date
        binding.tvDate.setOnClickListener {
            showDatePickerDialog(binding.tvDate)
        }

        // cancel
        binding.btnCancel.setOnClickListener {
            showDialog()
        }

        // write data to firebase
        binding.btnSave.setOnClickListener {

            val date = binding.tvDate.text.toString()
            val content = binding.etContent.text.toString()

            val selectedDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).parse(date)
            val calendar = Calendar.getInstance().apply {
                selectedDate?.let { time = it }
            }

            val id = auth.currentUser?.uid.toString()
            val year = calendar.get(Calendar.YEAR).toString()
            val month = (calendar.get(Calendar.MONTH) + 1).toString()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
            var mood = binding.btnMood.text.toString()

            val entryRef = database.child(id).child("JournalEntries").child(year).child(month).child(day)
            entryRef.child("date").setValue(date)
            entryRef.child("content").setValue(content)
            entryRef.child("mood").setValue(mood)
            entryRef.child("tags").setValue(selectedTags)

            onBackPressedDispatcher.onBackPressed()
        }

        // select mood
        binding.btnMood.setOnClickListener {
            showMoodSelectionDialog()
        }

        // select tags
        binding.btnTag.setOnClickListener {
            showTagsSelectionDialog()
        }

        // enhance confirmation dialog
        /*binding.btnEnhance.setOnClickListener {
            showDialog()
        }*/

        binding.fabBot.setOnClickListener {
            showBotDialog()
        }
    }

    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                textView.text = formattedDate
                retrieveAndPopulateDataFromFirebase(year.toString(), (month + 1).toString(), dayOfMonth.toString())
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    //Avoid this function
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

        val btnReject = dialog.findViewById<Button>(R.id.btn_reject_bottom_sheet)
        val btnAccept = dialog.findViewById<Button>(R.id.btn_accept_bottom_sheet)

        btnReject.setOnClickListener {
            dialog.hide()
        }
        btnAccept.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showMoodSelectionDialog() {
        val moodOptions = arrayOf("Feeling Awesome!", "Feeling Good", "Feeling Meh", "Feeling Down", "Feeling Terrible...")

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Mood")
            .setItems(moodOptions) { _, which ->
                val selectedMood = moodOptions[which]
                updateMood(selectedMood)
            }
            .show()
    }

    private fun updateMood(selectedMood: String) {
        binding.btnMood.text = selectedMood
    }

    private var previousSelectedTags = ArrayList<String>()
    private fun showTagsSelectionDialog() {
        previousSelectedTags.clear()
        previousSelectedTags.addAll(selectedTags)

        val checkedItems = BooleanArray(presetTags.size) { selectedTags.contains(presetTags[it]) }

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Tags")
            .setMultiChoiceItems(presetTags, checkedItems) { _, which, isChecked ->
                val selectedTag = presetTags[which]
                if (isChecked) {
                    selectedTags.add(selectedTag)
                } else {
                    selectedTags.remove(selectedTag)
                }
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Restore the previous state of selectedTags
                selectedTags.clear()
                selectedTags.addAll(previousSelectedTags)
                dialog.dismiss()
            }
            .setOnCancelListener {
                // Restore the previous state of selectedTags
                selectedTags.clear()
                selectedTags.addAll(previousSelectedTags)
            }
            .show()
    }

    private fun showBotDialog() {
        if(SLD.showConfirmChatbot){
            MaterialAlertDialogBuilder(this)
                .setTitle("Select Tags")
                .setTitle("Chat with Moodly?")
                .setMessage("Moodly is your personal AI-chatbot that can help you write journal entries!\n\nExisting content will be passed to Moodly by default and overwritten by the new entry.")
                .setPositiveButton("OK") { dialog, _ ->
                    openChatbot()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setNeutralButton("Don't show this again"){ dialog, _ ->
                    SLD.showConfirmChatbot = false
                    SLD.SaveData(this)

                    openChatbot()
                }
                .show()
        }else{
            openChatbot()
        }
    }

    private fun openChatbot(){
        val intent = Intent(this, chatbot::class.java)
        intent.putExtra("journal", binding.etContent.text.toString().trim());

        println("journal to pass: ${binding.etContent.text.trim()}")

        startForResult.launch(intent)
    }

    private fun retrieveAndPopulateDataFromFirebase(year: String, month: String, day: String) {
        Log.d("Journey", "The day passed to this function is $year-$month-$day")
        val id = auth.currentUser?.uid.toString()
        val entryRef = database.child(id).child("JournalEntries").child(year).child(month).child(day)

        entryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val entry = dataSnapshot.getValue(JournalEntry::class.java)
                if (entry != null) {
                    // Entry exists, restore data and allow user to edit
                    binding.tvDate.text = entry.date
                    binding.etContent.setText(entry.content)
                    binding.btnMood.text = entry.mood

                    // Handle tags restoration if applicable
                    val tags = entry.tags ?: emptyList()
                    // Clear previously selected tags
                    selectedTags.clear()
                    // Add retrieved tags to the selectedTags list
                    selectedTags.addAll(tags)

                    val checkedItems = BooleanArray(presetTags.size) { false }
                    // Set selected state for each tag button based on tags list
                    for (i in presetTags.indices) {
                        checkedItems[i] = tags.contains(presetTags[i])
                    }

                } else {
                    // set to selected date with blank fields
                    val calendar = Calendar.getInstance()
                    calendar.set(year.toInt(), month.toInt() - 1, day.toInt())
                    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(calendar.time)
                    binding.tvDate.text = formattedDate
                    binding.etContent.text = null
                    binding.btnMood.text = "Mood"
                    selectedTags.clear()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {

            val res : ArrayList<String>? = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val newText = res?.get(0) ?: return // Ensure res is not null and has elements

            val currentText = binding.etContent.text.toString()

            // Choose whether to append or overwrite based on your logic
            val finalText = if (currentText.isNotEmpty()) {
                // Append new text with a space separator
                "$currentText $newText"
            } else {
                // Overwrite existing text with the new text
                newText
            }

            // Set the final text to the EditText
            binding.etContent.setText(finalText)
        }
    }

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this ,"Speech recognition is not available", Toast.LENGTH_SHORT).show()
        } else  {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")
            startActivityForResult(i, RQ_SPEECH_REC)
        }
    }
}
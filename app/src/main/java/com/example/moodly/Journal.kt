package com.example.moodly

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

class Journal : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var SLD: SaveLoadData

    lateinit var userRecordRV: RecyclerView
    lateinit var userRecordRVAdapter: UserRecordRvAdapter
    lateinit var userRecord: ArrayList<UserRecordFormat>
    lateinit var userDiary: ArrayList<UserDiaryFormat>
    lateinit var tv_quotes: TextView
    lateinit var tv_author: TextView
    lateinit var quoteslist: List<QuoteModel>
    lateinit var toolbar: Toolbar
    lateinit var btnFilter: Button
    lateinit var txtFilterBy: TextView

    private lateinit var currContext: Context

    //region find tag
    val presetTags = arrayOf("Work", "Travel", "Food", "Health", "Relationships")
    var selectedTags = ArrayList<String>()
    var clicked=false
    //endregion

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currContext = context
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_journal, container, false)


        auth = com.google.firebase.Firebase.auth
        database = Firebase.database.reference
        SLD = SaveLoadData()

        val id = auth.currentUser?.uid.toString()

        tv_quotes = view.findViewById(R.id.tv_quotes)
        tv_author = view.findViewById(R.id.tv_author)
        toolbar=view.findViewById(R.id.tbtoolbar)
        btnFilter = view.findViewById(R.id.btnFilter)
        txtFilterBy= view.findViewById(R.id.TVFilterBy)
        txtFilterBy.visibility=View.GONE
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar!!.title=""


        userRecordRV = view.findViewById(R.id.idRVRecord)
        userRecord = ArrayList()

        userRecordRV.layoutManager = LinearLayoutManager(requireContext())
        userRecordRVAdapter = UserRecordRvAdapter(userRecord)
        userRecordRV.adapter = userRecordRVAdapter
        //region RecyclerView
        loadOri(id)
        //endregion
        //region Quote
        // Make API call to get random quotes
        ApiCall().getRandomQuotes() { listquote ->
            if (listquote != null) {
                quoteslist = listquote
                var num = ((0..listquote.size - 1).shuffled().last())
                val trimmedAuthor = if (quoteslist.get(num).author.length > 10) quoteslist.get(num).author.substring(0, quoteslist.get(num).author.length - 10) else quoteslist.get(num).author
                tv_quotes.text = quoteslist.get(num).text
                tv_author.text = ("~ $trimmedAuthor")
            } else {
                Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
        }
        //endregion
        //region find tag 2
        btnFilter.setOnClickListener(){
            if (clicked==false) {
                showOptionsDialog(requireContext())
            }else{
                loadOri(id)
                userRecordRVAdapter.filterList(userRecord)
                txtFilterBy.visibility=View.GONE
                btnFilter.background=resources.getDrawable(R.drawable.filtericon, null)
                Toast.makeText(requireContext(), "Filter cancelled", Toast.LENGTH_SHORT).show()
                clicked=false
                selectedTags.clear()
            }
        }
        //endregion
        return view
    }
    //region FindMonthNum
    fun findMonthNum(month: String): Int {
        var monthNum = 0
        if (month == "Jan") {
            monthNum = 1
        } else if (month == "Feb") {
            monthNum = 2
        } else if (month == "Mar") {
            monthNum = 3
        } else if (month == "Apr") {
            monthNum = 4
        } else if (month == "May") {
            monthNum = 5
        } else if (month == "Jun") {
            monthNum = 6
        } else if (month == "Jul") {
            monthNum = 7
        } else if (month == "Aug") {
            monthNum = 8
        } else if (month == "Sep") {
            monthNum = 9
        } else if (month == "Oct") {
            monthNum = 10
        } else if (month == "Nov") {
            monthNum = 11
        } else {
            monthNum = 12
        }
        return monthNum
    }
    //endregion

    //region FindMood
    fun findMood(context: Context, mood: String): Drawable? {
        return when (mood) {
            "Feeling Awesome!" -> ContextCompat.getDrawable(context, R.drawable.img_very_happy)
            "Feeling Good" -> ContextCompat.getDrawable(context, R.drawable.img_happy)
            "Feeling Meh" -> ContextCompat.getDrawable(context, R.drawable.img_neutral)
            "Feeling Down" -> ContextCompat.getDrawable(context, R.drawable.img_sad)
            "Feeling Terrible..." -> ContextCompat.getDrawable(context, R.drawable.img_very_sad)
            else -> ContextCompat.getDrawable(context, R.drawable.img_no_mood)
        }
    }
    //endregion

    //region search
    override fun onCreateOptionsMenu(menu: Menu,inflater: MenuInflater){
        inflater.inflate(R.menu.search_menu, menu)

        // below line is to get our menu item.
        val searchItem: MenuItem = menu.findItem(R.id.search_bar)

        // getting search view of our item.
        val searchView = searchItem.actionView as SearchView

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(msg: String): Boolean {
                searching(msg)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun searching(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<UserRecordFormat> = ArrayList()

        // running a for loop to compare elements.
        for (item in userRecord) {
            val filteredlist2: ArrayList<UserDiaryFormat> = ArrayList()
            // check for matching results
            for (item2 in item.diary){
                if (item2.contentDiary.toLowerCase().contains(text.toLowerCase())) {
                    // if matched, then put into the array
                    filteredlist2.add(item2)
                }
            }
            if(filteredlist2.size>0){
                filteredlist.add(UserRecordFormat(item.rid, item.months, filteredlist2))
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list, display message
            Toast.makeText(requireContext(), "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // pass filtered list to adapter
            userRecordRVAdapter.filterList(filteredlist)
        }
    }
    //endregion

    //region convertMonth
    fun convertToMMMFormat(monthNumber: Int): String {
        return Month.of(monthNumber).toString().substring(0, 3)
    }
    //endregion

    //region find tag 3
    fun showTagsSelectionDialog() {
        val checkedItems = BooleanArray(presetTags.size) { selectedTags.contains(presetTags[it]) }

        MaterialAlertDialogBuilder(requireContext())
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
                filterByTags(selectedTags)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                // Restore the previous state of selectedTags
                selectedTags.clear()
            }
            .show()
    }
    private fun showMoodSelectionDialog() {
        val moodOptions = arrayOf("Feeling Awesome!", "Feeling Good", "Feeling Meh", "Feeling Down", "Feeling Terrible...")
        var selectedMood = ""

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Mood")
            .setSingleChoiceItems(moodOptions,0) { _, which ->
                selectedMood = moodOptions[which]
            }
            .setPositiveButton("OK") { dialog, _ ->
                filterByMood(selectedMood)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                selectedMood = ""
            }
            .show()
    }
    private fun showOptionsDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Filter By :")
            .setItems(arrayOf("Tags", "Mood")) { _, which ->
                when (which) {
                    0 -> {
                        showTagsSelectionDialog()
                    }
                    1 -> {
                        showMoodSelectionDialog()
                    }
                }
            }
            .show()
    }

    private fun filterByTags(text: ArrayList<String>) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<UserRecordFormat> = ArrayList()

        // running a for loop to compare elements.
        for (item in userRecord) {
            val filteredlist2: ArrayList<UserDiaryFormat> = ArrayList()
            // check for matching results
            for (item2 in item.diary){
                if ((isElementThere(item2.tagsDiary,text))&&item2.tagsDiary.isNotEmpty()) {
                    // if matched, then put into the array
                    filteredlist2.add(item2)
                }
            }

            if(filteredlist2.size>0){
                filteredlist.add(UserRecordFormat(item.rid, item.months, filteredlist2))
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list, display message
            Toast.makeText(requireContext(), "Tag not found.", Toast.LENGTH_SHORT).show()
        } else {
            // pass filtered list to adapter
            userRecordRVAdapter.filterList(filteredlist)
            btnFilter.background=resources.getDrawable(R.drawable.filtercancel, null)
            clicked=true
            Toast.makeText(requireContext(), "Filtered results by tags.", Toast.LENGTH_SHORT).show()
            txtFilterBy.visibility=View.VISIBLE
            txtFilterBy.text="Filter By: "
            for (stag in text.indices){
                if(stag==text.size-1){
                    txtFilterBy.text=(txtFilterBy.text.toString()).plus("#").plus(text[stag])
                }else{
                    txtFilterBy.text=(txtFilterBy.text.toString()).plus("#").plus(text[stag]).plus(", ")
                }
            }
        }
    }
    private fun filterByMood(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<UserRecordFormat> = ArrayList()

        // running a for loop to compare elements.
        for (item in userRecord) {
            val filteredlist2: ArrayList<UserDiaryFormat> = ArrayList()
            // check for matching results
            for (item2 in item.diary){
                if (item2.mood==text) {
                    // if matched, then put into the array
                    filteredlist2.add(item2)
                }
            }

            if(filteredlist2.size>0){
                filteredlist.add(UserRecordFormat(item.rid, item.months, filteredlist2))
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list, display message
            Toast.makeText(requireContext(), "Mood not found.", Toast.LENGTH_SHORT).show()
        } else {
            // pass filtered list to adapter
            userRecordRVAdapter.filterList(filteredlist)
            btnFilter.background=resources.getDrawable(R.drawable.filtercancel, null)
            clicked=true
            Toast.makeText(requireContext(), "Filtered results by mood.", Toast.LENGTH_SHORT).show()
            txtFilterBy.visibility=View.VISIBLE
            txtFilterBy.text="Filter By: "
            txtFilterBy.text=(txtFilterBy.text.toString()).plus(text)
        }
    }
    private fun loadOri(id:String){
        val myRef = database.child(id).child("JournalEntries")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userRecord.clear()
                for (result in snapshot.children) {
                    var years = result.key.toString()
                    for (result2 in result.children) {
                        var months = result2.key.toString()
                        userDiary = ArrayList()

                        for (result3 in result2.children) {
                            var mood = result3.child("mood").value.toString()
                            var diary = result3.child("content").value.toString()
                            var dateD = result3.child("date").value.toString()

                            if (dateD != null) {
                                val moodEmote = findMood(currContext, mood)
                                val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")

                                var date = LocalDate.parse(dateD, formatter)
                                var day = date.dayOfWeek
                                Log.d("Complete Date", "date: $date")
                                dateD = dateD.substring(0, 2) + " " + day.toString().substring(0, 3)
                                var tags = ArrayList<String>()
                                if(result3.hasChild("tags")){
                                    for (result4 in result3.child("tags").children){
                                        tags.add(result4.value.toString())
                                    }
                                }
                                userDiary.add(UserDiaryFormat(dateD, moodEmote, diary, date, tags, mood))
                            }
                        }
                        if (userRecord.size > 0) {
                            var count = userRecord.size
                            var supposedIndex = 0
                            for (currentIndex in 0..(count - 1)) {
                                if (userRecord[currentIndex].rid < years.toInt()) {
                                    supposedIndex = currentIndex
                                    break
                                } else if (userRecord[currentIndex].rid == years.toInt()) {
                                    var currentMonthNum = findMonthNum(userRecord[currentIndex].months)
                                    var newMonthNum = findMonthNum(months)
                                    if (newMonthNum > currentMonthNum && currentIndex == 0) {
                                        supposedIndex = currentIndex
                                        break
                                    } else if (newMonthNum > currentMonthNum) {
                                        break
                                    } else {
                                        supposedIndex += 1
                                    }
                                } else {
                                    supposedIndex += 1
                                }
                            }

                            userRecord.add(
                                supposedIndex,

                                UserRecordFormat(years.toInt(), convertToMMMFormat(months.toInt()), userDiary)
                            )
                        } else {
                            userRecord.add(
                                UserRecordFormat(years.toInt(), convertToMMMFormat(months.toInt()), userDiary))
                        }
                    }
                    // index++
                }

                userRecordRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ERROR", "Failed to read value.", error.toException())
            }

        })
    }

    private fun isElementThere(array1: ArrayList<String>, array2: ArrayList<String>): Boolean {
            for (i in array1.indices) {
                for(j in array2.indices){
                    if (array1[i] == array2[j]) {
                        return true
                    }
                }
            }
        return false
    }
    //endregion
}
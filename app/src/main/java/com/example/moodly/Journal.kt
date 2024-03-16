package com.example.moodly

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

class Journal : Fragment() {
    lateinit var txtTest: TextView
    lateinit var userRecordRV: RecyclerView
    lateinit var userRecordRVAdapter: UserRecordRvAdapter
    lateinit var userRecord: ArrayList<UserRecordFormat>
    lateinit var userDiary: ArrayList<UserDiaryFormat>
    lateinit var tv_quotes: TextView
    lateinit var tv_author: TextView
    lateinit var quoteslist: List<QuoteModel>
    lateinit var toolbar: Toolbar

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

        txtTest = view.findViewById(R.id.labelFilter)
        tv_quotes = view.findViewById(R.id.tv_quotes)
        tv_author = view.findViewById(R.id.tv_author)
        toolbar=view.findViewById(R.id.tbtoolbar)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar!!.title=""

        //region RecyclerView
        userRecordRV = view.findViewById(R.id.idRVRecord)
        userRecord = ArrayList()

        userRecordRV.layoutManager = LinearLayoutManager(requireContext())
        userRecordRVAdapter = UserRecordRvAdapter(userRecord)
        userRecordRV.adapter = userRecordRVAdapter
        val myRef = Firebase.database.getReference("DiaryRecords")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var index = 1

                for (result in snapshot.children) {
                    var months_years = result.key.toString()
                    var months = months_years.substring(0, 3)
                    var years = months_years.substring(3)
                    userDiary = ArrayList()
                    var indexSmall = 1
                    for (result2 in result.children) {
                        var mood = result2.child("Mood").value.toString()
                        var diary = result2.child("Content").value.toString()
                        var dateD = result2.child("Date").value.toString()
                        var moodEmote = findMood(mood)
                        var date = LocalDate.parse(dateD)
                        var day = date.dayOfWeek
                        dateD = dateD.substring(8) + " " + day.toString().substring(0, 3)
                        userDiary.add(UserDiaryFormat(dateD, moodEmote, diary))
                        indexSmall++
                        //txtTest.text= txtTest.text.toString().plus(diary)
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
                            UserRecordFormat(years.toInt(), months, userDiary)
                        )
                    } else {
                        userRecord.add(UserRecordFormat(years.toInt(), months, userDiary))
                    }
                    index++
                }

                userRecordRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ERROR", "Failed to read value.", error.toException())
            }

        })
        //endregion
        //region Quote
        // Make API call to get random quotes
        ApiCall().getRandomQuotes() { listquote ->
            if (listquote != null) {
                quoteslist = listquote
                var num = ((0..listquote.size).shuffled().last())
                tv_quotes.text = quoteslist.get(num).text
                tv_author.text = ("~ " + quoteslist.get(num).author)
            } else {
                Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()
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
    fun findMood(mood: String): String {
        var moodEmote = ""
        if (mood == "Happy") {
            moodEmote = "&#128516"
        } else if (mood == "Sad") {
            moodEmote = "&#128557"
        } else if (mood == "Mad") {
            moodEmote = "&#128545"
        } else {
            moodEmote = "&#128529"
        }
        return moodEmote
    }
    //endregion

    //region filter
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
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(msg)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<UserRecordFormat> = ArrayList()

        // running a for loop to compare elements.
        for (item in userRecord) {
            val filteredlist2: ArrayList<UserDiaryFormat> = ArrayList()
            // checking if the entered string matched with any item of our recycler view.
            for (item2 in item.diary){
                if (item2.contentDiary.toLowerCase().contains(text.toLowerCase())) {
                    // if the item is matched we are
                    // adding it to our filtered list.
                    filteredlist2.add(item2)
                }
            }
            if(filteredlist2.size>0){
                filteredlist.add(UserRecordFormat(item.rid, item.months, filteredlist2))
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(requireContext(), "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            userRecordRVAdapter.filterList(filteredlist)
        }
    }
    //endregion
}
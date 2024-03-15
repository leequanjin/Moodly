package com.example.moodly

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

class MyDiary : AppCompatActivity() {
    lateinit var txtTest: TextView
    lateinit var userRecordRV: RecyclerView
    lateinit var userRecordRVAdapter: UserRecordRvAdapter
    lateinit var userRecord: ArrayList<UserRecordFormat>
    lateinit var userDiary: ArrayList<UserDiaryFormat>
    lateinit var tv_quotes: TextView
    lateinit var tv_author: TextView
    lateinit var quoteslist: List<QuoteModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mydiary1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mydiary_1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        txtTest = findViewById(R.id.test)
        tv_quotes = findViewById(R.id.tv_quotes)
        tv_author = findViewById(R.id.tv_author)

        //region RecyclerView
        userRecordRV = findViewById(R.id.idRVRecord)
        userRecord = ArrayList()

        userRecordRV.layoutManager = LinearLayoutManager(this)
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
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
        }
        //endregion
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
}
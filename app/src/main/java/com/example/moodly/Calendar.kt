package com.example.moodly

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.moodly.calendar.displayText
import com.example.moodly.databinding.CalendarDayBinding
import com.example.moodly.databinding.CalendarHeaderBinding
import com.example.moodly.databinding.FragmentCalendarBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth


class Calendar : Fragment(R.layout.fragment_calendar){

    private var selectedDate: LocalDate? = null

    private lateinit var binding: FragmentCalendarBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalendarBinding.bind(view)

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        binding.exFiveCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exFiveCalendar.scrollToMonth(currentMonth)

        binding.exFiveCalendar.monthScrollListener = { month ->
            binding.exFiveMonthYearText.text = month.yearMonth.displayText()

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.exFiveCalendar.notifyDateChanged(it)
            }
        }

        binding.exFiveNextMonthImage.setOnClickListener {
            binding.exFiveCalendar.findFirstVisibleMonth()?.let {
                binding.exFiveCalendar.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.exFivePreviousMonthImage.setOnClickListener {
            binding.exFiveCalendar.findFirstVisibleMonth()?.let {
                binding.exFiveCalendar.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@Calendar.binding
                            binding.exFiveCalendar.notifyDateChanged(day.date)
                            oldDate?.let { binding.exFiveCalendar.notifyDateChanged(it) }
                        }
                    }
                }
            }

            fun updateMoodImage(mood: String?) {
                Log.d(tag, "Mood from firebase: " + mood.toString())
                when (mood) {
                    "Feeling Awesome!" -> binding.exFiveDayImage.setImageResource(R.drawable.img_very_happy)
                    "Feeling Good" -> binding.exFiveDayImage.setImageResource(R.drawable.img_happy)
                    "Feeling Meh" -> binding.exFiveDayImage.setImageResource(R.drawable.img_neutral)
                    "Feeling Down" -> binding.exFiveDayImage.setImageResource(R.drawable.img_sad)
                    "Feeling Terrible..." -> binding.exFiveDayImage.setImageResource(R.drawable.img_very_sad)
                    // Add more cases for other moods if needed
                    else -> binding.exFiveDayImage.setImageResource(0) // Set default image or null if mood is unknown
                }
            }
        }

        binding.exFiveCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.exFiveDayText
                val layout = container.binding.exFiveDayLayout
                textView.text = data.date.dayOfMonth.toString()

                // Get the date for the current day in the calendar
                val currentDate = data.date

                if (data.position == DayPosition.MonthDate) {
                    val textColor = ContextCompat.getColor(context, R.color.extra_dark_blue)
                    textView.setTextColor(textColor)
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.example_5_selected_bg else 0)
                    // Retrieve mood from Firebase and update the mood image accordingly
                    getMoodFromFirebase(currentDate) { mood ->
                        container.updateMoodImage(mood)
                    }
                } else {
                    val textColor = ContextCompat.getColor(context, R.color.faded_extra_dark_blue)
                    textView.setTextColor(textColor)
                    layout.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.exFiveCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.setTextColor(Color.BLACK)
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }

    // Function to retrieve mood from Firebase based on date
    private fun getMoodFromFirebase(date: LocalDate, callback: (String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val year = date.year.toString()
        val month = date.monthValue.toString()
        val day = date.dayOfMonth.toString()

        val entryReference =
            databaseReference.child("JournalEntries").child(year).child(month).child(day)

        // Read mood value from Firebase
        entryReference.child("mood").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mood = dataSnapshot.getValue(String::class.java)
                Log.d(tag, "$year-$month-$day")
                Log.d(tag, "Mood: " + mood.toString())
                callback.invoke(mood) // Invoke the callback with the mood data
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(tag, "Error")
                callback.invoke(null) // Invoke the callback with null if there's an error
            }
        })
    }
}

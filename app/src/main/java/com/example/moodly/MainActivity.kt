package com.example.moodly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.moodly.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Journal())

        // bottom navigation bar
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miJournal -> replaceFragment(Journal())
                R.id.miCalendar -> replaceFragment(Calendar())
                R.id.miAnalytics -> replaceFragment(Analytics())
                R.id.miSettings -> replaceFragment(Settings())

                else ->{
                }
            }
            true
        }

        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

        // floating action button
        binding.floatingActionButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = (calendar.get(Calendar.MONTH)) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val intent = Intent(this, WriteJournal::class.java)
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", day)

            Log.d("Journey", "The date passed is $year-$month-$day")

            startActivity(intent)
        }

    }

    fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
        fragmentTransaction.commit()
    }
}
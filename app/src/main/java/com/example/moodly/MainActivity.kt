package com.example.moodly

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.moodly.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Journal())

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
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
        fragmentTransaction.commit()
    }
}
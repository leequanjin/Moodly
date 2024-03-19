package com.example.moodly

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Analytics.newInstance] factory method to
 * create an instance of this fragment.
 */
class Analytics : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var SLD: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analytics, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you have a PieChart with the ID pieChart in your XML layout
        getData()
    }

    private fun getData(){
        auth = com.google.firebase.Firebase.auth
        database = Firebase.database.reference
        SLD = SaveLoadData()

        val id = auth.currentUser?.uid.toString()

        var awesomeCount = 0
        var goodCount = 0
        var mehCount = 0
        var downCount = 0
        var terribleCount = 0
        var moodlessCount = 0

        database.child(id).child("JournalEntries").get()
            .addOnSuccessListener {
                for (year in it.children){
                    for (month in year.children){
                        for (day in month.children) {
                            val mood = day.child("mood").value.toString()
                            Log.d("Moodly", "What's the mood: $mood")
                            if (mood == "Feeling Awesome!") {
                                awesomeCount++
                            } else if (mood == "Feeling Good"){
                                goodCount++
                            } else if (mood == "Feeling Meh"){
                                mehCount++
                            } else if (mood == "Feeling Down"){
                                println("run")
                                downCount++
                            } else if (mood == "Feeling Terrible..."){
                                terribleCount++
                            }else{
                                moodlessCount++
                            }
                        }
                    }
                }

                setupPieChart(awesomeCount, goodCount, mehCount, downCount, terribleCount, moodlessCount)
            }
    }

    private fun setupPieChart(awesomeCount: Int, goodCount: Int, mehCount: Int, downCount: Int, terribleCount: Int, moodlessCount: Int) {



        // Get reference to the PieChart
        val pieChart: PieChart = view?.findViewById(R.id.pieChart) ?: return

        //Initialize array
        val entries:ArrayList<PieEntry> = ArrayList()

        //Value is how big the area taken, Label is name ("n"f for number and "..." for string)
        // Filter entries based on value before adding them
        val filteredEntries = listOf(
            PieEntry(awesomeCount.toFloat(), "Feeling \nAwesome!"),
            PieEntry(goodCount.toFloat(), "Feeling \nGood"),
            PieEntry(mehCount.toFloat(), "Feeling \nMeh"),
            PieEntry(downCount.toFloat(), "Feeling \nDown"),
            PieEntry(terribleCount.toFloat(), "Feeling \nTerrible..."),
            PieEntry(moodlessCount.toFloat(), "Moodless")
        ).filter { it.value > 0f } // Filter entries with value greater than 0

        entries.addAll(filteredEntries)

        // Create a HashMap to map mood to color
        val moodColorMap = hashMapOf(
            "Feeling \nAwesome!" to Color.parseColor("#FFC0CB"),  // Pink
            "Feeling \nGood" to Color.RED,                           // Red
            "Feeling \nMeh" to Color.parseColor("#FF7F00"),        // Orange
            "Feeling \nDown" to Color.BLUE,                         // Blue
            "Feeling \nTerrible..." to Color.parseColor("#7F00FF"), // Indigo
            "Moodless" to Color.WHITE                             // White
        )

        // Colors representing the rainbow spectrum
        val colors = intArrayOf(
            Color.parseColor("#FFC0CB"), // Pink
            Color.RED,
            Color.parseColor("#FF7F00"), // Orange
            Color.BLUE,
            Color.parseColor("#7F00FF"), // Indigo
            Color.WHITE // Violet
        )

        // Assign color based on mood using the map
        for (i in entries.indices) {
            val mood = entries[i].label.toString()
            colors[i] = moodColorMap[mood] ?: Color.GRAY  // Use gray as default if mood not found
        }

        val pieDataSet = PieDataSet(entries, "").apply {setColors(colors, 255)  }

        pieDataSet.valueTextSize=12f
        pieDataSet.valueTextColor= Color.BLACK


        // Set the size of the center hole (inner circle)
        val holeRadius = 35f // Adjust the value to make the hole smaller
        pieChart.holeRadius = holeRadius

        // Set the size of the transparent circle around the hole
        pieChart.transparentCircleRadius = holeRadius

        pieChart.setEntryLabelTextSize(13f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setCenterTextColor(Color.BLACK)

        val pieData= PieData(pieDataSet)

        pieChart.data= pieData

        pieData.setValueTextSize(18f)

        pieChart.description.text="Pie Chart"
        pieChart.centerText= "Mood \nTracker"
        pieChart.setCenterTextSize(16f)
        //pieChart.setCenterTextColor(Color.BLUE);

        pieChart.animateY(2000)

        val legend: Legend = pieChart.legend
        legend.textSize= 23f

        pieChart.description.isEnabled=false
        pieChart.legend.isEnabled=false
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Analytics.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Analytics().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


package com.example.moodly

import android.graphics.Color
import android.os.Bundle
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
        setupPieChart()
    }

    private fun setupPieChart() {
        // Get reference to the PieChart
        val pieChart: PieChart = view?.findViewById(R.id.pieChart) ?: return

        //Initialize array
        val entries:ArrayList<PieEntry> = ArrayList()

        //Value is how big the area taken, Label is name ("n"f for number and "..." for string)
        entries.add(PieEntry(30f, "Happy"))
        entries.add(PieEntry(20f, "Angry"))
        entries.add(PieEntry(50f, "Sad"))

        val pieDataSet= PieDataSet(entries  , "")

        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)

        pieDataSet.valueTextSize=12f
        pieDataSet.valueTextColor= Color.BLACK

        // Set the size of the center hole (inner circle)
        val holeRadius = 35f // Adjust the value to make the hole smaller
        pieChart.holeRadius = holeRadius

        // Set the size of the transparent circle around the hole
        pieChart.transparentCircleRadius = holeRadius

        pieChart.setEntryLabelTextSize(15f)
        pieChart.setCenterTextColor(Color.BLUE)

        val pieData= PieData(pieDataSet)

        pieChart.data= pieData

        pieData.setValueTextSize(15f)

        pieChart.description.text="Pie Chart"
        pieChart.centerText= "Mood \nTracker"
        pieChart.setCenterTextSize(14f)
        //pieChart.setCenterTextColor(Color.BLUE);

        pieChart.animateY(2000)

        val legend: Legend = pieChart.legend
        legend.textSize= 28f

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


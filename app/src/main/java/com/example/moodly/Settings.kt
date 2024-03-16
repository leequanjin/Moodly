package com.example.moodly
//
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import android.widget.TimePicker
import androidx.cardview.widget.CardView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment() {
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

    @SuppressLint("SetTextI18n", "ScheduleExactAlarm")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val textViewRemind: TextView? = view.findViewById(R.id.textViewReminder) as? TextView
        val toggleButton: ToggleButton? = view.findViewById(R.id.toggleButton2) as? ToggleButton
        val timePicker: TimePicker? = view.findViewById(R.id.timePicker) as? TimePicker
        val cardSettings: CardView? = view.findViewById(R.id.cardSettings) as? CardView
        val buttonSetTime: Button? = view.findViewById(R.id.buttonSetTime)

        toggleButton?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Toggle is ON, show TimePicker
                textViewRemind!!.setText("Turn Reminder Off?")
                timePicker!!.visibility = View.VISIBLE
                val params = cardSettings!!.layoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.card_height_whenSpinner)
                cardSettings.layoutParams = params
                buttonSetTime!!.visibility = View.VISIBLE

                buttonSetTime.setOnClickListener {
                    val context = requireContext() // Get the context from fragment

                    // Check notification permission
                    val notificationManager = NotificationManagerCompat.from(context)
                    if (notificationManager.areNotificationsEnabled()) {
                        // Schedule notification and show toast
                        // Existing code from your previous implementation...

                        val selectedHour = timePicker.hour
                        val selectedMinute = timePicker.minute

                        // ... rest of the notification scheduling code ...

                        Toast.makeText(
                            context,
                            "Notification scheduled! You'll be reminded at the chosen time.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Notification permission not granted, show explanation dialog
                        val builder = AlertDialog.Builder(context)
                            .setTitle("Notifications Disabled")
                            .setMessage("Moodly needs notification permission to remind you. Would you like to enable it?")
                            .setPositiveButton("Open Settings") { dialog, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", context.packageName, null)
                                intent.data = uri
                                startActivity(intent)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            } else {
                // Toggle is OFF, hide TimePicker
                timePicker!!.visibility = View.GONE
                textViewRemind!!.setText("Turn Reminder On?")
                val params = cardSettings!!.layoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.card_height)
                cardSettings.layoutParams = params
            }
        }

        return view
    }
}

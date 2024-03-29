    package com.example.moodly

    import android.annotation.SuppressLint
    import android.app.AlarmManager
    import android.app.AlertDialog
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.app.PendingIntent
    import android.content.Context
    import android.content.Intent
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.Settings
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.TextView
    import android.widget.TimePicker
    import android.widget.Toast
    import android.widget.ToggleButton
    import androidx.cardview.widget.CardView
    import androidx.core.app.NotificationManagerCompat
    import androidx.fragment.app.Fragment
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.auth
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.ktx.database
    import com.google.firebase.ktx.Firebase
    import java.util.Calendar

    private lateinit var currContext: Context

    class Settings : Fragment() {
        private lateinit var auth: FirebaseAuth
        private lateinit var database: DatabaseReference
        private lateinit var SLD: SaveLoadData

        override fun onAttach(context: Context) {
            super.onAttach(context)
            currContext = context
        }

        override fun onDetach() {
            super.onDetach()
        }

        @SuppressLint("SetTextI18n", "ScheduleExactAlarm")
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            // Inflate the layout for this fragment
            val view = inflater.inflate(R.layout.fragment_settings, container, false)
            auth = com.google.firebase.Firebase.auth
            database = Firebase.database.reference
            SLD = SaveLoadData()

            val id = auth.currentUser?.uid.toString()

            val textViewRemind: TextView? = view.findViewById(R.id.textViewReminder) as? TextView
            val toggleButton: ToggleButton? = view.findViewById(R.id.toggleButton2) as? ToggleButton
            val timePicker: TimePicker? = view.findViewById(R.id.timePicker) as? TimePicker
            val cardSettings: CardView? = view.findViewById(R.id.cardGeneral) as? CardView
            val buttonSetTime: Button? = view.findViewById(R.id.buttonSetTime)
            val buttonRename: Button? = view.findViewById(R.id.buttonRename)
            val btnManual: Button = view.findViewById(R.id.btnManual)
            buttonRename?.setOnClickListener {
                val intent = Intent(requireContext(), Welcome::class.java)
                startActivity(intent)
            }
            val buttonExit: Button? = view.findViewById(R.id.buttonExit)
            buttonExit?.setOnClickListener {
                if (auth.currentUser != null) {
                    auth.signOut()

                    SLD.username = ""
                    SLD.email = ""
                    SLD.password = ""
                    SLD.showConfirmChatbot = true

                    SLD.SaveData(currContext)

                    val intent = Intent(context, Auth::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
            }

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
                             fun createNotificationChannel(context: Context) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channelName = "Moodly Channel"
                                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                                    val channel = NotificationChannel("Channel1", channelName, importance).apply {
                                        description = "Channel for Moodly notifications"
                                    }

                                    // Register the channel with the system
                                    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    notificationManager.createNotificationChannel(channel)
                                }
                            }
                            createNotificationChannel(context)
                            // Schedule notification based on selected time

                            val selectedHour = timePicker.hour
                            val selectedMinute = timePicker.minute

                            val triggerTime = ((selectedHour * 60 + selectedMinute) * 60 * 1000).toLong()

                            fun getCurrentHourAndTimeInMillis(): Long {
                                val now = System.currentTimeMillis() // Get current time in milliseconds
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = now

                                // Clear year, month, and day information
                                calendar.set(Calendar.YEAR, 1)
                                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                                calendar.set(Calendar.DAY_OF_MONTH, 1)

                                // Get current hour and minute in milliseconds
                                val hourInMillis = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L
                                val minuteInMillis = calendar.get(Calendar.MINUTE) * 60 * 1000L

                                // Combine hour and minute milliseconds
                                return hourInMillis + minuteInMillis
                            }

                            val now = getCurrentHourAndTimeInMillis()
                            val triggermoment = triggerTime - now
                            val triggermomentinsec = triggermoment/1000

                            Log.d("Moodly", "Selected Hour: $selectedHour")
                            Log.d("Moodly", "Selected Minute: $selectedMinute")
                            Log.d("Moodly", "Current time: $now")
                            Log.d("Moodly", "Trigger time: $triggerTime")
                            Log.d("Moodly", "Seconds until notification should be sent: $triggermomentinsec")

                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                            calendar.set(Calendar.MINUTE, selectedMinute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            val notificationIntent = Intent(context, Notification::class.java)

                            val pendingIntent = PendingIntent.getBroadcast(context, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (triggermoment < 0) {
                                val tomorrow = calendar.timeInMillis + (24 * 60 * 60 * 1000);
                                val tmrinfo = AlarmManager.AlarmClockInfo(tomorrow, pendingIntent)
                                // Schedule the notification with the adjusted delay (for next day)
                                Log.d("Moodly", "Milliseconds that it should trigger tommorow: $tmrinfo")
                                alarmManager.setAlarmClock(tmrinfo, pendingIntent)
                            } else {
                                // Schedule the notification with the positive delay (for today)
                                val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
                                Log.d("Moodly", "Milliseconds that it should trigger: $calendar.timeInMillis")
                                alarmManager.setAlarmClock(info, pendingIntent)
                            }
                            Toast.makeText(context, "Notification scheduled! You'll be reminded at the chosen time.", Toast.LENGTH_LONG).show()
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

            btnManual.setOnClickListener {
                (requireActivity() as MainActivity).replaceFragment(Manual())
            }

            return view
        }

    }

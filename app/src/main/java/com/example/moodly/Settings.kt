    package com.example.moodly

    import java.util.Calendar
    import android.app.AlarmManager
    import android.content.Context
    import android.provider.Settings
    import android.content.Intent
    import android.annotation.SuppressLint
    import android.app.AlertDialog
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.app.PendingIntent
    import android.icu.text.SimpleDateFormat
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
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
    import androidx.core.app.NotificationCompat
    import androidx.core.app.NotificationManagerCompat
    import java.time.LocalDateTime
    import java.util.Date
    import java.util.Locale


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
            val cardSettings: CardView? = view.findViewById(R.id.cardGeneral) as? CardView
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
                             fun createNotificationChannel(context: Context) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channelName = "Moodly Channel"
                                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                                    val channel = NotificationChannel("Channel1", channelName, importance).apply {
                                        description = "Channel for Moodly notifications"
                                    }

                                    // Register the channel with the system
                                    val notificationManager: NotificationManager =
                                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                            Log.d("Moodly", "Trigger time: $triggerTime")
                            Log.d("Moodly", "Current time: $now")
                            Log.d("Moodly", "Seconds until notification should be sent: $triggermomentinsec")



                            val notificationIntent = Intent(context, Notification::class.java)

                            val pendingIntent = PendingIntent.getBroadcast(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggermoment, pendingIntent)
                            if (triggermoment < 0) {
                                val tomorrow = now + (24*60*60*1000)
                                val targetTimeTomorrow = triggerTime + tomorrow
                                val adjustedDelay = targetTimeTomorrow - now
                                // Schedule the notification with the adjusted delay (for next day)
                                Log.d("Moodly", "The fuck is this: $adjustedDelay")
                                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, adjustedDelay, pendingIntent)
                            } else {
                                // Schedule the notification with the positive delay (for today)
                                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggermoment, pendingIntent)
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

            return view
        }

    }

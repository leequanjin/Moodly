package com.example.moodly

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val notificationID = 1
const val channelID = "Channel1"

class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent){
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_journal)
            .setContentTitle("Moodly Reminder") // Set title directly
            .setContentText("It's time to reflect on your mood!") // Set message directly
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}

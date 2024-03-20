package com.example.moodly

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val notificationID = 1
const val channelID = "Channel1"

class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent){
        val notificationIntent = Intent(context, MainActivity::class.java) // Replace with your app's main activity
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK) // Only use this flag
        val pendingIntent = PendingIntent.getActivity(context, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_journal)
            .setContentTitle("Moodly Reminder") // Set title directly
            .setContentText("It's time to reflect on your mood!") // Set message directly
            .setContentIntent(pendingIntent) // Set the pending intent here
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}

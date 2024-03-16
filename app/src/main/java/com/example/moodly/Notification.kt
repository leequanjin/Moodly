package com.example.moodly

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val notifcationID = 1
const val channelID = "Channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"
class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent){
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_journal)
            .setContentTitle(intent.getStringExtra(titleExtra))
            .setContentText(intent.getStringExtra(messageExtra))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifcationID, notification)
    }

}

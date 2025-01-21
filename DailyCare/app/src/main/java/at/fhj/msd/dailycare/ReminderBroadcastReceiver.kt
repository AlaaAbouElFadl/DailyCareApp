package at.fhj.msd.dailycare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val title = intent.getStringExtra("reminder_title") ?: "Erinnerung"
        val time = intent.getStringExtra("reminder_time") ?: ""

        NotificationHelper(context).sendNotification(reminderId, title, time)
    }
}
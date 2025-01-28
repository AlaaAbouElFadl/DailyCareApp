package at.fhj.msd.dailycare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import at.fhj.msd.dailycare.data.AppDatabase

class ResetReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        if (reminderId != -1) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "daily-care-database"
            ).build()

            val reminderDao = db.reminderDao()

            Thread {
                val reminder = reminderDao.getReminderById(reminderId)
                if (reminder != null) {
                    val updatedReminder = reminder.copy(isDone = false)
                    reminderDao.updateReminder(updatedReminder)
                }
            }.start()
        }
    }
}

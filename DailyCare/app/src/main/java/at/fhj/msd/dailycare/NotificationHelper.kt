package at.fhj.msd.dailycare

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "reminder_channel_id"
        const val CHANNEL_NAME = "Erinnerungen"
        const val CHANNEL_DESCRIPTION = "Benachrichtigungen für Erinnerungen"
    }

    /**
     * Erstellt den Notification-Kanal (nur für Android O und höher erforderlich).
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sendet sofort eine Benachrichtigung.
     * @param reminderId Die eindeutige ID der Erinnerung.
     * @param title Der Titel der Erinnerung.
     * @param time Die Zeit der Erinnerung.
     */
    fun sendNotification(reminderId: Int, title: String, time: String) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, "Bitte Benachrichtigungen aktivieren", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, RemindersActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText("Erinnerung: $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(reminderId, builder.build())
        }
    }

    /**
     * Plant eine Benachrichtigung zu einer bestimmten Zeit.
     * @param reminderId Die eindeutige ID der Erinnerung.
     * @param title Der Titel der Erinnerung.
     * @param time Die Zeit der Erinnerung im Format "HH:mm".
     */
    fun scheduleNotification(reminderId: Int, title: String, time: String, interval: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Toast.makeText(context, "AlarmManager nicht verfügbar", Toast.LENGTH_SHORT).show()
            return
        }

        // Ab Android 12 prüfen, ob exakte Alarme erlaubt sind
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Bitte erlauben Sie exakte Alarme in den Einstellungen.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        // Zeitformat prüfen
        if (!time.matches(Regex("\\d{2}:\\d{2}"))) {
            Toast.makeText(context, "Ungültiges Zeitformat (erwartet HH:mm)", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_time", time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Zeit für die Benachrichtigung berechnen
        val timeParts = time.split(":")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)

            // Falls die Zeit bereits vergangen ist, auf den nächsten Tag setzen
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            if (interval > 0) {
                // Wiederkehrende Benachrichtigung planen
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    interval,
                    pendingIntent
                )
            } else {
                // Einmalige Benachrichtigung planen
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Fehler beim Planen des Alarms. Bitte überprüfen Sie die Berechtigungen.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


}

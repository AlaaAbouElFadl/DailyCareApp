package at.fhj.msd.dailycare

import android.app.AlarmManager
import android.os.Bundle
import android.view.LayoutInflater
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.room.Room
import at.fhj.msd.dailycare.data.*

class RemindersActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var reminderDao: ReminderDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        NavigationBarHelper().setupNavigationBar(this)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "daily-care-database"
        ).fallbackToDestructiveMigration()
            .build()

        reminderDao = db.reminderDao()

        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener { showReminderTypeDialog() }

        val switchMedikament = findViewById<Switch>(R.id.switchMedikament)
        val switchSicherheit = findViewById<Switch>(R.id.switchSicherheit)

        switchMedikament.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                loadReminders("Medikament")
                switchSicherheit.isChecked = false
            } else if (!switchSicherheit.isChecked) {
                loadReminders()
            }
        }

        switchSicherheit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                loadReminders("Sicherheit")
                switchMedikament.isChecked = false
            } else if (!switchMedikament.isChecked) {
                loadReminders()
            }
        }

        loadReminders()
    }

    private fun showReminderTypeDialog() {
        val reminderTypes = arrayOf("Medikament", "Sicherheit")
        AlertDialog.Builder(this)
            .setTitle("Wähle den Erinnerungstyp")
            .setItems(reminderTypes) { _, which ->
                when (reminderTypes[which]) {
                    "Medikament" -> showAddReminderDialog("Medikament")
                    "Sicherheit" -> showAddReminderDialog("Sicherheit")
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showAddReminderDialog(type: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)

        AlertDialog.Builder(this)
            .setTitle("Erinnerung hinzufügen")
            .setView(dialogView)
            .setPositiveButton("Speichern") { _, _ ->
                val name = editTextName.text.toString()
                if (name.isNotEmpty()) {
                    if (type == "Medikament") {
                        showIntervalSelectionDialog(type, name)
                    } else {
                        showTimePickerDialog { time ->
                            saveReminder(ReminderEntity(title = name, time = time, type = type))
                        }
                    }
                } else {
                    Toast.makeText(this, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showIntervalSelectionDialog(type: String, name: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_interval, null)
        val spinnerInterval = dialogView.findViewById<Spinner>(R.id.spinnerInterval)

        val intervals = arrayOf("Täglich", "Alle 2 Tage", "Bestimmter Wochentag")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Intervall auswählen")
            .setView(dialogView)
            .setPositiveButton("Weiter") { _, _ ->
                val selectedInterval = spinnerInterval.selectedItem.toString()
                when (selectedInterval) {
                    "Täglich", "Alle 2 Tage" -> showStartDateDialog(type, name, selectedInterval)
                    "Bestimmter Wochentag" -> showWeekdaySelectionDialog(type, name)
                }
            }
            .setNegativeButton("Zurück") { _, _ ->
                showAddReminderDialog(type)
            }
            .show()
    }



    private fun showReminderOptionsDialog(reminder: ReminderEntity) {
        val options = arrayOf("Bearbeiten", "Löschen")
        AlertDialog.Builder(this)
            .setTitle(reminder.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditReminderDialog(reminder)
                    1 -> deleteReminder(reminder)
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }


    private fun showEditReminderDialog(reminder: ReminderEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val timePickerButton = dialogView.findViewById<Button>(R.id.timePickerButton)

        editTextName.setText(reminder.title)
        timePickerButton.text = reminder.time

        timePickerButton.setOnClickListener {
            showTimePickerDialog { time ->
                timePickerButton.text = time
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Erinnerung bearbeiten")
            .setView(dialogView)
            .setPositiveButton("Speichern") { _, _ ->
                val newTitle = editTextName.text.toString()
                val newTime = timePickerButton.text.toString()
                if (newTitle.isNotEmpty() && newTime.isNotEmpty()) {
                    updateReminder(reminder.copy(title = newTitle, time = newTime))
                } else {
                    Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showStartDateDialog(type: String, name: String, interval: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_start_date, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)

        AlertDialog.Builder(this)
            .setTitle("Startdatum auswählen")
            .setView(dialogView)
            .setPositiveButton("Speichern") { _, _ ->
                val day = datePicker.dayOfMonth
                val month = datePicker.month + 1
                val year = datePicker.year
                val startDate = String.format("%02d.%02d.%04d", day, month, year)

                showTimePickerDialog { time ->
                    saveReminder(
                        ReminderEntity(
                            title = name,
                            time = time,
                            type = type,
                            interval = "$interval (ab $startDate)"
                        )
                    )
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showWeekdaySelectionDialog(type: String, name: String) {
        val weekdays = arrayOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")

        AlertDialog.Builder(this)
            .setTitle("Wochentag auswählen")
            .setItems(weekdays) { _, which ->
                val selectedDay = weekdays[which]

                showTimePickerDialog { time ->
                    saveReminder(
                        ReminderEntity(
                            title = name,
                            time = time,
                            type = type,
                            interval = "Jeden $selectedDay"
                        )
                    )
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }


    private fun showTimePickerDialog(callback: (String) -> Unit) {
        val currentHour = 12
        val currentMinute = 0
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            callback(selectedTime)
        }, currentHour, currentMinute, true).apply {
            setTitle("Wähle eine Uhrzeit")
        }.show()
    }

    private fun saveReminder(reminder: ReminderEntity) {
        Thread {
            val reminderId = reminderDao.insertReminder(reminder) // Gibt Long zurück
            runOnUiThread {
                // Intervall basierend auf dem Typ der Erinnerung bestimmen
                val interval: Long? = when (reminder.interval) {
                    "Täglich" -> AlarmManager.INTERVAL_DAY
                    "Alle 2 Tage" -> AlarmManager.INTERVAL_DAY * 2
                    "Bestimmter Wochentag" -> AlarmManager.INTERVAL_DAY * 7
                    else -> null // Einmalige Erinnerung
                }

                // Überprüfung und Übergabe von Interval
                if (interval != null) {
                    NotificationHelper(this).scheduleNotification(
                        reminderId.toInt(),
                        reminder.title,
                        reminder.time,
                        interval
                    )
                } else {
                    // Einmalige Benachrichtigung
                    NotificationHelper(this).scheduleNotification(
                        reminderId.toInt(),
                        reminder.title,
                        reminder.time,
                        0L // Kein Wiederholungsintervall
                    )
                }

                loadReminders()
            }
        }.start()
    }


    private fun updateReminder(updatedReminder: ReminderEntity) {
        Thread {
            reminderDao.updateReminder(updatedReminder)
            runOnUiThread {
                loadReminders()
            }
        }.start()
    }

    private fun deleteReminder(reminder: ReminderEntity) {
        Thread {
            reminderDao.deleteReminder(reminder)
            runOnUiThread { loadReminders() }
        }.start()
    }


    private fun loadReminders(type: String? = null) {
        Thread {
            val reminders = type?.let {
                reminderDao.getRemindersByType(it)
            } ?: reminderDao.getAllReminders()

            runOnUiThread {
                displayReminders(reminders)
            }
        }.start()
    }


    private fun markReminderAsDone(reminder: ReminderEntity) {
        val updatedReminder = reminder.copy(isDone = true)
        Thread {
            reminderDao.updateReminder(updatedReminder)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                reminder.id,
                Intent(this, ReminderBroadcastReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            pendingIntent?.cancel() // Entfernt die geplante Benachrichtigung

            runOnUiThread { loadReminders() }
        }.start()
    }



    private fun displayReminders(reminders: List<ReminderEntity>) {
        val layout = findViewById<LinearLayout>(R.id.remindersSection)

        layout.removeAllViews()

        reminders.forEach { reminder ->
            val view = createReminderView(reminder)
            layout.addView(view)
        }
    }

    private fun createReminderView(reminder: ReminderEntity): View {
        val view = LayoutInflater.from(this).inflate(R.layout.reminder_item, null)
        val iconView = view.findViewById<ImageView>(R.id.reminderIcon)
        val titleView = view.findViewById<TextView>(R.id.reminderTitle)
        val timeView = view.findViewById<TextView>(R.id.reminderTime)
        val doneButton = view.findViewById<Button>(R.id.reminderDoneButton)

        titleView.text = reminder.title
        timeView.text = reminder.time

        if (reminder.isDone) {
            titleView.paintFlags = titleView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            doneButton.text = "✔️"
            doneButton.isEnabled = false
        } else {
            doneButton.setOnClickListener {
                markReminderAsDone(reminder)
                Toast.makeText(this, "${reminder.title} erledigt!", Toast.LENGTH_SHORT).show()
            }
        }

        when (reminder.type) {
            "Medikament" -> {
                iconView.setImageResource(R.drawable.meds)
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.green))
                doneButton.setBackgroundResource(R.color.green)
            }
            "Sicherheit" -> {
                iconView.setImageResource(R.drawable.shield)
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.blue))
                doneButton.setBackgroundResource(R.color.blue)
            }
        }

        doneButton.setOnClickListener {
            markReminderAsDone(reminder)
            Toast.makeText(this, "${reminder.title} erledigt!", Toast.LENGTH_SHORT).show()
        }

        view.setOnClickListener {
            showReminderOptionsDialog(reminder)
        }

        return view
    }
}



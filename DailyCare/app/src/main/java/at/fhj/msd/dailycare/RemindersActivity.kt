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
import android.view.ViewGroup
import android.widget.TextView
import java.util.Calendar


class RemindersActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var reminderDao: ReminderDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        NavigationBarHelper().setupNavigationBar(this)
        NavigationBarHelper().highlightActiveTab(this, Tab.REMINDERS)
        updateGlobalTextSize()

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

        switchMedikament.setOnCheckedChangeListener { _, _ -> loadReminders() }
        switchSicherheit.setOnCheckedChangeListener { _, _ -> loadReminders() }

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


                val categoryEnabled = isCategoryEnabledInSettings(reminder.type)
                if (!categoryEnabled) {

                    loadReminders()
                    return@runOnUiThread
                }


                if (interval != null) {
                    NotificationHelper(this).scheduleNotification(
                        reminderId.toInt(), reminder.title, reminder.time, interval
                    )
                } else {
                    NotificationHelper(this).scheduleNotification(
                        reminderId.toInt(), reminder.title, reminder.time, 0L
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


    private fun loadReminders() {
        Thread {
            val all = reminderDao.getAllReminders()

            val bySettings = all.filter { isCategoryEnabledInSettings(it.type) }

            val switchMed = findViewById<Switch>(R.id.switchMedikament).isChecked
            val switchSec = findViewById<Switch>(R.id.switchSicherheit).isChecked

            val byUi = when {
                switchMed && !switchSec -> bySettings.filter { it.type == "Medikament" }
                !switchMed && switchSec -> bySettings.filter { it.type == "Sicherheit" }
                !switchMed && !switchSec -> emptyList()
                else -> bySettings
            }

            runOnUiThread { displayReminders(byUi) }
        }.start()
    }


    private fun markReminderAsDone(reminder: ReminderEntity) {
        val updatedReminder = reminder.copy(isDone = true)
        Thread {
            reminderDao.updateReminder(updatedReminder)

            scheduleResetAtMidnight(reminder)

            runOnUiThread { loadReminders() }
        }.start()
    }

    private fun scheduleResetAtMidnight(reminder: ReminderEntity) {
        checkExactAlarmPermission()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ResetReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Berechtigung zum Planen von Alarme erforderlich.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
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
            titleView.paintFlags = titleView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            doneButton.text = "Erledigt"
            doneButton.isEnabled = true
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

    //Schriftgröße
    private fun updateGlobalTextSize() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val textSize = sharedPreferences.getFloat("textSize", 14f)

        val rootView = window.decorView.rootView
        applyTextSizeToViewGroup(rootView, textSize)
    }

    private fun applyTextSizeToViewGroup(view: View, textSize: Float) {
        when (view) {
            is ViewGroup -> {

                for (i in 0 until view.childCount) {
                    applyTextSizeToViewGroup(view.getChildAt(i), textSize)
                }
            }
            is TextView -> {
                if (view.id != R.id.toolbarTitle) {
                    view.textSize = textSize
                }
            }
        }
    }


    private fun isCategoryEnabledInSettings(type: String): Boolean {
        val sp = getSharedPreferences("Settings", MODE_PRIVATE)
        return when (type) {
            "Medikament" -> sp.getBoolean("medicationNotifications", true)
            "Sicherheit" -> sp.getBoolean("securityNotifications", true)
            else -> true
        }
    }

}
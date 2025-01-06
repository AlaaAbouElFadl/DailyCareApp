package at.fhj.msd.dailycare

import android.os.Bundle
import android.view.LayoutInflater
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener { showReminderTypeDialog() }
    }

    private fun showReminderTypeDialog() {
        val reminderTypes = arrayOf("Medikament", "Sicherheit")
        AlertDialog.Builder(this)
            .setTitle("Wähle den Erinnerungstyp")
            .setItems(reminderTypes) { _, which ->
                when (reminderTypes[which]) {
                    "Medikament" -> startMedicationFlow()
                    "Sicherheit" -> startSecurityFlow()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun startMedicationFlow() {
        createReminder("Medikament")
    }

    private fun startSecurityFlow() {
        createReminder("Sicherheit")
    }

    private fun createReminder(type: String) {
        var reminderName: String = ""
        var reminderTime: String = ""
        var reminderInterval: String = ""
        var reminderStartDate: String? = null
        var reminderDayOfWeek: String? = null

        // Schritt 1: Name
        showNameDialog(type) { name ->
            reminderName = name

            // Schritt 2: Uhrzeit
            showTimePickerDialog { time ->
                reminderTime = time

                // Schritt 3: Intervall
                showIntervalSelectionDialog { interval, dayOfWeek ->
                    reminderInterval = interval
                    reminderDayOfWeek = dayOfWeek

                    // Schritt 4: Startdatum
                    if (interval != "Bestimmter Wochentag") {
                        showStartDatePickerDialog { startDate ->
                            reminderStartDate = startDate
                            saveReminder(type, reminderName, reminderTime, reminderInterval, reminderStartDate, reminderDayOfWeek)
                        }
                    } else {
                        saveReminder(type, reminderName, reminderTime, reminderInterval, reminderStartDate, reminderDayOfWeek)
                    }
                }
            }
        }
    }

    private fun showNameDialog(type: String, callback: (String) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)

        editTextName.hint = if (type == "Medikament") "Medikament Name" else "Sicherheitsmaßnahme Name"

        AlertDialog.Builder(this)
            .setTitle("Erinnerung Name eingeben")
            .setView(dialogView)
            .setPositiveButton("Weiter") { _, _ ->
                val name = editTextName.text.toString()
                if (name.isNotEmpty()) {
                    callback(name)
                } else {
                    Toast.makeText(this, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show()
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

    private fun showIntervalSelectionDialog(callback: (String, String?) -> Unit) {
        val intervals = arrayOf("Täglich", "Alle 2 Tage", "Bestimmter Wochentag")
        AlertDialog.Builder(this)
            .setTitle("Wähle ein Intervall")
            .setItems(intervals) { _, which ->
                when (intervals[which]) {
                    "Bestimmter Wochentag" -> {
                        showDayOfWeekSelectionDialog { dayOfWeek ->
                            callback(intervals[which], dayOfWeek)
                        }
                    }
                    else -> {
                        callback(intervals[which], null)
                    }
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showDayOfWeekSelectionDialog(callback: (String) -> Unit) {
        val daysOfWeek = arrayOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
        AlertDialog.Builder(this)
            .setTitle("Wähle einen Wochentag")
            .setItems(daysOfWeek) { _, which ->
                callback(daysOfWeek[which])
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showStartDatePickerDialog(callback: (String) -> Unit) {
        val datePicker = DatePicker(this)

        AlertDialog.Builder(this)
            .setTitle("Wähle das Startdatum")
            .setView(datePicker)
            .setPositiveButton("Weiter") { _, _ ->
                val day = datePicker.dayOfMonth
                val month = datePicker.month + 1
                val year = datePicker.year
                callback(String.format("%02d.%02d.%04d", day, month, year))
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun saveReminder(
        type: String,
        name: String,
        time: String,
        interval: String,
        startDate: String?,
        dayOfWeek: String?
    ) {
        // Speichere die Erinnerung mit Typ, Name, Zeit, Intervall, Startdatum und Tag der Woche
        Toast.makeText(
            this,
            "Erinnerung gespeichert: \nTyp: $type\nName: $name\nZeit: $time\nIntervall: $interval\nStartdatum: $startDate\nWochentag: $dayOfWeek",
            Toast.LENGTH_LONG
        ).show()
    }
}

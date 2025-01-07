package at.fhj.msd.dailycare

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EmergencyContactsActivity : AppCompatActivity() {
    private val emergencyContacts = mutableListOf<Pair<String, String>>()
    private val otherContacts = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener { showContactTypeDialog() }
    }


    private fun showContactTypeDialog() {
        val contactTypes = arrayOf("Notfallkontakt", "Weitere Kontakte")
        AlertDialog.Builder(this)
            .setTitle("Wähle Kontaktart")
            .setItems(contactTypes) { _, which ->
                when (contactTypes[which]) {
                    "Notfallkontakt" -> showAddContactDialog { name, phone ->
                        emergencyContacts.add(name to phone)
                        saveContacts()
                    }
                    "Weitere Kontakte" -> showAddContactDialog { name, phone ->
                        otherContacts.add(name to phone)
                        saveContacts()
                    }
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showAddContactDialog(callback: (String, String) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editTextPhone = dialogView.findViewById<EditText>(R.id.editTextPhone)

        AlertDialog.Builder(this)
            .setTitle("Kontakt hinzufügen")
            .setView(dialogView)
            .setPositiveButton("Speichern") { _, _ ->
                val name = editTextName.text.toString()
                val phone = editTextPhone.text.toString()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    callback(name, phone)
                } else {
                    Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun saveContacts() {
        // Hier könnten die Kontakte in einer Datenbank oder SharedPreferences gespeichert werden.
        Toast.makeText(this, "Kontakte gespeichert", Toast.LENGTH_SHORT).show()
    }
}

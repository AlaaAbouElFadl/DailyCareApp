package at.fhj.msd.dailycare

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import android.app.AlertDialog
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import at.fhj.msd.dailycare.data.*
import android.net.Uri
import android.view.ViewGroup
import android.widget.TextView


class EmergencyContactsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var emergencyContactDao: EmergencyContactDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)
        NavigationBarHelper().setupNavigationBar(this)
        NavigationBarHelper().highlightActiveTab(this, Tab.EMERGENCY)
        updateGlobalTextSize()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "daily-care-database"
        ).build()
        emergencyContactDao = db.emergencyContactDao()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener { showContactTypeDialog() }
        loadContacts()
    }


    private fun showContactTypeDialog() {
        val contactTypes = arrayOf("Notfallkontakt", "Weitere Kontakte")
        AlertDialog.Builder(this)
            .setTitle("Wähle Kontaktart")
            .setItems(contactTypes) { _, which ->
                when (contactTypes[which]) {
                    "Notfallkontakt" -> showAddContactDialog { name, phone ->
                        saveContact(EmergencyContactEntity(name = name, phoneNumber = phone, contactType = "Notfallkontakt"))
                    }
                    "Weitere Kontakte" -> showAddContactDialog { name, phone ->
                        saveContact(EmergencyContactEntity(name = name, phoneNumber = phone, contactType = "Weitere Kontakte"))
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

    private fun saveContact(contact: EmergencyContactEntity) {
        Thread {
            emergencyContactDao.insertContact(contact)
            runOnUiThread { loadContacts() }
        }.start()
    }

    private fun updateContact(updatedContact: EmergencyContactEntity) {
        Thread {
            emergencyContactDao.updateContact(updatedContact)
            runOnUiThread { loadContacts() }
        }.start()
    }

    private fun deleteContact(contact: EmergencyContactEntity) {
        Thread {
            emergencyContactDao.deleteContact(contact)
            runOnUiThread { loadContacts() }
        }.start()
    }

    private fun loadContacts() {
        Thread {
            val contacts = emergencyContactDao.getAllContacts()
            runOnUiThread {
                displayContacts(contacts)
            }
        }.start()
    }

    private fun displayContacts(contacts: List<EmergencyContactEntity>) {
        val emergencyLayout = findViewById<LinearLayout>(R.id.emergencyContactsSection)
        val otherLayout = findViewById<LinearLayout>(R.id.otherContactsSection)

        emergencyLayout.removeAllViews()
        otherLayout.removeAllViews()

        contacts.forEach { contact ->
            val contactView = createContactView(contact)
            if (contact.contactType == "Notfallkontakt") {
                contactView.setBackgroundResource(R.drawable.contact_item_emergency)
                emergencyLayout.addView(contactView)
            } else {
                contactView.setBackgroundResource(R.drawable.contact_item_background)
                otherLayout.addView(contactView)
            }
        }
    }

    private fun createContactView(contact: EmergencyContactEntity): View {
        val view = LayoutInflater.from(this).inflate(R.layout.contact_item, null)

        val nameView = view.findViewById<TextView>(R.id.contactName)
        val phoneView = view.findViewById<TextView>(R.id.contactPhone)
        val callButton = view.findViewById<Button>(R.id.callButton)

        nameView.text = contact.name
        phoneView.text = contact.phoneNumber

        callButton.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
            startActivity(callIntent)
        }

        view.setOnClickListener {
            showContactOptionsDialog(contact)
        }

        return view
    }



    private fun showEditContactDialog(contact: EmergencyContactEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editTextPhone = dialogView.findViewById<EditText>(R.id.editTextPhone)

        editTextName.setText(contact.name)
        editTextPhone.setText(contact.phoneNumber)

        AlertDialog.Builder(this)
            .setTitle("Kontakt bearbeiten")
            .setView(dialogView)
            .setPositiveButton("Speichern") { _, _ ->
                val updatedName = editTextName.text.toString()
                val updatedPhone = editTextPhone.text.toString()
                if (updatedName.isNotEmpty() && updatedPhone.isNotEmpty()) {
                    val updatedContact = contact.copy(name = updatedName, phoneNumber = updatedPhone)
                    updateContact(updatedContact)
                } else {
                    Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
    private fun showContactOptionsDialog(contact: EmergencyContactEntity) {
        val options = arrayOf("Bearbeiten", "Löschen")
        AlertDialog.Builder(this)
            .setTitle("${contact.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditContactDialog(contact)
                    1 -> deleteContact(contact)
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
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
}

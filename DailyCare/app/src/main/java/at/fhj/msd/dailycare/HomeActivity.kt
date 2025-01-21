package at.fhj.msd.dailycare

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        NavigationBarHelper().setupNavigationBar(this)

        // Erinnerungen Button
        val btnReminders = findViewById<LinearLayout>(R.id.btnReminders)
        btnReminders.setOnClickListener {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
        }

        // Notfallkontakte Button
        val btnEmergencyContacts = findViewById<LinearLayout>(R.id.btnEmergencyContacts)
        btnEmergencyContacts.setOnClickListener {
            val intent = Intent(this, EmergencyContactsActivity::class.java)
            startActivity(intent)
        }

        // Einstellungen Button
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }
}
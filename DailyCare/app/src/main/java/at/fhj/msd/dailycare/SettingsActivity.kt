package at.fhj.msd.dailycare

import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val sampleText = findViewById<TextView>(R.id.sampleText)
        val textSizeSeekBar = findViewById<SeekBar>(R.id.textSizeSeekBar)
        textSizeSeekBar.progress = 14 // Default size
        textSizeSeekBar.max = 30

        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val textSize = progress.toFloat()
                sampleText.textSize = textSize
                saveTextSizePreference(textSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Switch for medication
        val medicationSwitch = findViewById<Switch>(R.id.medicationSwitch)
        medicationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference("medication", isChecked)
        }

        // Switch for security
        val securitySwitch = findViewById<Switch>(R.id.securitySwitch)
        securitySwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference("security", isChecked)
        }
    }

    private fun saveTextSizePreference(size: Float) {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("textSize", size)
        editor.apply()
    }

    private fun saveNotificationPreference(type: String, isEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("${type}Notifications", isEnabled)
        editor.apply()
    }
}

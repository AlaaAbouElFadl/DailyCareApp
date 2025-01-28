package at.fhj.msd.dailycare

import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        NavigationBarHelper().setupNavigationBar(this)
        updateGlobalTextSize()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val sampleText = findViewById<TextView>(R.id.sampleText)
        val textSizeSeekBar = findViewById<SeekBar>(R.id.textSizeSeekBar)

        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedTextSize = sharedPreferences.getFloat("textSize", 14f)

        textSizeSeekBar.progress = savedTextSize.toInt()
        sampleText.textSize = savedTextSize

        textSizeSeekBar.max = 30
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val textSize = progress.toFloat()
                sampleText.textSize = textSize
                saveTextSizePreference(textSize)
                updateGlobalTextSize()
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
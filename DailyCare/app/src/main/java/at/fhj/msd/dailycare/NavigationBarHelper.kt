package at.fhj.msd.dailycare

import android.app.Activity
import android.content.Intent
import android.widget.ImageView

enum class Tab { HOME, REMINDERS, EMERGENCY, SETTINGS }
class NavigationBarHelper {

    fun setupNavigationBar(activity: Activity) {
        val homeIcon = activity.findViewById<ImageView>(R.id.iconHome)
        val bellIcon = activity.findViewById<ImageView>(R.id.iconBell)
        val phoneIcon = activity.findViewById<ImageView>(R.id.iconPhone)
        val settingsIcon = activity.findViewById<ImageView>(R.id.iconSettings)

        homeIcon.setOnClickListener {
            if (activity !is HomeActivity) {
                activity.startActivity(Intent(activity, HomeActivity::class.java))
            }
        }
        bellIcon.setOnClickListener {
            if (activity !is RemindersActivity) {
                activity.startActivity(Intent(activity, RemindersActivity::class.java))
            }
        }
        phoneIcon.setOnClickListener {
            if (activity !is EmergencyContactsActivity) {
                activity.startActivity(Intent(activity, EmergencyContactsActivity::class.java))
            }
        }
        settingsIcon.setOnClickListener {
            if (activity !is SettingsActivity) {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
            }
        }
    }

    fun highlightActiveTab(activity: Activity, active: Tab) {
        val homeIcon = activity.findViewById<ImageView>(R.id.iconHome)
        val bellIcon = activity.findViewById<ImageView>(R.id.iconBell)
        val phoneIcon = activity.findViewById<ImageView>(R.id.iconPhone)
        val settingsIcon = activity.findViewById<ImageView>(R.id.iconSettings)

        // Alle grau
        val gray = activity.getColor(R.color.nav_inactive)
        homeIcon.setColorFilter(gray)
        bellIcon.setColorFilter(gray)
        phoneIcon.setColorFilter(gray)
        settingsIcon.setColorFilter(gray)

        // Aktives Icon mit farbe
        when (active) {
            Tab.HOME      -> homeIcon.setColorFilter(activity.getColor(android.R.color.holo_blue_dark))
            Tab.REMINDERS -> bellIcon.setColorFilter(activity.getColor(R.color.green_primary))
            Tab.EMERGENCY -> phoneIcon.setColorFilter(activity.getColor(R.color.red_accent))
            Tab.SETTINGS  -> settingsIcon.setColorFilter(activity.getColor(R.color.purple_secondary))
        }
    }
}


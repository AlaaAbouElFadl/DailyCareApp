package at.fhj.msd.dailycare

import android.app.Activity
import android.content.Intent
import android.widget.ImageView

class NavigationBarHelper {

    fun setupNavigationBar(activity: Activity) {
        val homeIcon = activity.findViewById<ImageView>(R.id.iconHome)
        val bellIcon = activity.findViewById<ImageView>(R.id.iconBell)
        val phoneIcon = activity.findViewById<ImageView>(R.id.iconPhone)
        val settingsIcon = activity.findViewById<ImageView>(R.id.iconSettings)

        // Klick-Listener für Home
        homeIcon.setOnClickListener {
            if (activity !is HomeActivity) {
                val intent = Intent(activity, HomeActivity::class.java)
                activity.startActivity(intent)
            }
        }

        // Klick-Listener für Bell
        bellIcon.setOnClickListener {
            if (activity !is RemindersActivity) {
                val intent = Intent(activity, RemindersActivity::class.java)
                activity.startActivity(intent)
            }
        }

        // Klick-Listener für Phone
        phoneIcon.setOnClickListener {
            if (activity !is EmergencyContactsActivity) {
                val intent = Intent(activity, EmergencyContactsActivity::class.java)
                activity.startActivity(intent)
            }
        }

        // Klick-Listener für Settings
        settingsIcon.setOnClickListener {
            if (activity !is SettingsActivity) {
                val intent = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }
}

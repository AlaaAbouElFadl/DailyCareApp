package at.fhj.msd.dailycare.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EmergencyContactEntity::class, ReminderEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun reminderDao(): ReminderDao
}

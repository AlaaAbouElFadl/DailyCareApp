package at.fhj.msd.dailycare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE type = :type")
    fun getRemindersByType(type: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Int): ReminderEntity?

    @Insert
    fun insertReminder(reminder: ReminderEntity): Long

    @Update
    fun updateReminder(reminder: ReminderEntity)

    @Delete
    fun deleteReminder(reminder: ReminderEntity)

}
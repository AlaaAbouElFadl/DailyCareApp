package at.fhj.msd.dailycare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val time: String,
    val type: String,
    val isDone: Boolean = false,
    val interval: String? = null,
    val repeatInterval: Long? = null
)

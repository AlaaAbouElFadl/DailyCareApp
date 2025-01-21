package at.fhj.msd.dailycare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val contactType: String
)

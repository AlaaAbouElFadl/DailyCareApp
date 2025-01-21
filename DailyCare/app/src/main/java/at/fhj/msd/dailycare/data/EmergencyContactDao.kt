package at.fhj.msd.dailycare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM contacts")
    fun getAllContacts(): List<EmergencyContactEntity>

    @Insert
    fun insertContact(contact: EmergencyContactEntity)

    @Update
    fun updateContact(contact: EmergencyContactEntity)

    @Delete
    fun deleteContact(contact: EmergencyContactEntity)
}

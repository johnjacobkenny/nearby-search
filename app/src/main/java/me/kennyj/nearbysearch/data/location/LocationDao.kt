package me.kennyj.nearbysearch.data.location

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    @Query("SELECT * FROM location LIMIT 10")
    fun getLastTenLocations(): List<Location>

    @Insert
    fun insert(location: Location)
}
package me.kennyj.nearbysearch.data.location

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    var latitude: Double,
    var longitude: Double
) {
    @PrimaryKey(autoGenerate = true) var id: Int? = null
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorId: String,
    val creatorName: String,
    val creatorHandle: String,
    val title: String,
    val description: String,
    val dateString: String,
    val ticketPrice: Double,
    val originalAvailable: Int,
    val ticketsBought: Int = 0,
    val currency: String = "USD",
    val location: String = "Virtual Live Stream"
)

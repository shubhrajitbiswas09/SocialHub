package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey val creatorId: String,
    val creatorName: String,
    val creatorHandle: String,
    val tierName: String, // "BRONZE", "SILVER", "GOLD"
    val amount: Double,
    val active: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

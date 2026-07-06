package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorId: String,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatar: String,
    val caption: String,
    val contentImage: String, // name of a custom visual or template color or illustration
    val isPremium: Boolean,
    val requiredTier: String = "FREE", // "FREE", "BRONZE", "SILVER", "GOLD"
    val likesCount: Int = 0,
    val tipsTotal: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false
)

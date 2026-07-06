package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marketplace_banners")
data class MarketplaceBanner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String,
    val targetProductId: Int? = null,
    val externalUrl: String? = null
)

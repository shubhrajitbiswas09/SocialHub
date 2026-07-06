package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marketplace_products")
data class MarketplaceProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val logoUrl: String,
    val affiliateLink: String,
    val creatorName: String = "Admin Store"
)

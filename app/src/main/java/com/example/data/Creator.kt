package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creators")
data class Creator(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val description: String,
    val followersCount: Int,
    val bronzeTierPrice: Double,
    val silverTierPrice: Double,
    val goldTierPrice: Double,
    val isVerified: Boolean = true,
    val currency: String = "USD",
    val isFollowed: Boolean = false,
    
    // Customizable Tier Perks & Names for Verified Creators
    val bronzeTierName: String = "Bronze Watcher",
    val bronzeTierPerks: String = "Latest daily stock alerts & tech watchlists.",
    val silverTierName: String = "Silver Analyst",
    val silverTierPerks: String = "Exclusive audio transcripts & deep portfolio wiring sheets.",
    val goldTierName: String = "Gold Partner",
    val goldTierPerks: String = "Weekly 1-on-1 portfolio review on secure live rooms."
)


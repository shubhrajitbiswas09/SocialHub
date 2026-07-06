package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "TIP", "SUBSCRIPTION", "TICKET_BUY", "WALLET_FUND"
    val description: String,
    val amount: Double,
    val currency: String = "USD",
    val recipientHandle: String,
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val paymentId: String, // Razorpay-like reference pay_HJKsa217as
    val timestamp: Long = System.currentTimeMillis()
)

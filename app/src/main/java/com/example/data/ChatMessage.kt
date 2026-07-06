package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val receiverName: String,
    val encryptedContent: String,
    val isEncrypted: Boolean = true,
    val isFinancialRequest: Boolean = false,
    val amountRequested: Double = 0.0,
    val payRefId: String? = null, // Pay reference for chat tipping
    val paymentStatus: String = "NONE", // "NONE", "PAID", "DECLINED"
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val isDelivered: Boolean = true
)

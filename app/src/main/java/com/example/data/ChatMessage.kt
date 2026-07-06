package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val receiverName: String,
    val encryptedContent: String,
    val isEncrypted: Boolean = true, // Always true for security
    val isFinancialRequest: Boolean = false,
    val amountRequested: Double = 0.0,
    val payRefId: String? = null, // Payment reference for chat tipping
    val paymentStatus: String = "NONE", // "NONE", "PAID", "DECLINED"
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val isDelivered: Boolean = true,
    val isDeletedForMe: Boolean = false,
    val isDeletedForEveryone: Boolean = false
) {
    // === SECURITY: Validate message ===
    fun isValid(): Boolean {
        return senderName.isNotBlank() && senderName.length <= 100 &&
               receiverName.isNotBlank() && receiverName.length <= 100 &&
               encryptedContent.isNotBlank() && encryptedContent.length <= 10000 &&
               isEncrypted && // Encryption always required
               if (isFinancialRequest) amountRequested > 0.0 && amountRequested <= 100000.0 else true
    }
}

package com.example.data

import java.util.UUID

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: String = "SYSTEM",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    // === SECURITY: Validate notification ===
    fun isValid(): Boolean {
        return title.isNotBlank() && message.isNotBlank() &&
               title.length <= 255 && message.length <= 1000 &&
               id.isNotBlank() && type.isNotBlank()
    }
}

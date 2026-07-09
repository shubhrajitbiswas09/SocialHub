package com.example.ui

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = "user" // This matches your Firebase security rules exactly
)

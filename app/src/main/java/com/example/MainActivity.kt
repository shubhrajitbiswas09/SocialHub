package com.example

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.SocialHubApp
import com.example.ui.SocialHubViewModel
import com.example.ui.theme.SocialHubTheme

class MainActivity : ComponentActivity() {
  private val viewModel: SocialHubViewModel by viewModels()
  private lateinit var auth: com.google.firebase.auth.FirebaseAuth
  private var authStateListener: com.google.firebase.auth.FirebaseAuth.AuthStateListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Firebase Auth
    auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    // Set up the safety listener to counter auth latency
    authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
      val currentUser = firebaseAuth.currentUser
      if (currentUser != null) {
        // User is securely logged in! Sync state with viewmodel
        viewModel.onFirebaseUserDetected(currentUser)
      }
    }

    setContent {
      val isDarkMode by viewModel.isDarkMode.collectAsState()
      SocialHubTheme(darkTheme = isDarkMode) {
        SocialHubApp(viewModel)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    authStateListener?.let { auth.addAuthStateListener(it) }
  }

  override fun onStop() {
    super.onStop()
    authStateListener?.let { auth.removeAuthStateListener(it) }
  }
}

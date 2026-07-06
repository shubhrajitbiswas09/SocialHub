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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkMode by viewModel.isDarkMode.collectAsState()
      SocialHubTheme(darkTheme = isDarkMode) {
        SocialHubApp(viewModel)
      }
    }
  }
}

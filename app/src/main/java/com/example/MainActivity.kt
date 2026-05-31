package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.navigation.MainScreen
import com.example.ui.navigation.NavigationViewModel
import com.example.viewmodel.QuranViewModel

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: QuranViewModel = hiltViewModel()
      val navigationViewModel: NavigationViewModel = hiltViewModel()
      MainScreen(
        viewModel = viewModel,
        navigationViewModel = navigationViewModel,
      )
    }
  }
}

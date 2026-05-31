package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.data.audio.QuranAudioController
import com.example.ui.navigation.MainScreen
import com.example.ui.navigation.NavigationViewModel
import com.example.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var audioController: QuranAudioController

    private var deepLinkUri by mutableStateOf<Uri?>(null)

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* playback notification requires this on Android 13+ */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkUri = intent?.data
        requestPlaybackNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val viewModel: QuranViewModel = hiltViewModel()
            val navigationViewModel: NavigationViewModel = hiltViewModel()
            val pendingDeepLink = deepLinkUri
            MainScreen(
                viewModel = viewModel,
                navigationViewModel = navigationViewModel,
                deepLinkUri = pendingDeepLink,
                onDeepLinkConsumed = { deepLinkUri = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkUri = intent.data
    }

    override fun onDestroy() {
        if (isFinishing) {
            audioController.shutdown()
        }
        super.onDestroy()
    }

    private fun requestPlaybackNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

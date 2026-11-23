package com.example.lfcapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.lfcapp.presentation.imageupload.ImageUploadScreen
import com.example.lfcapp.ui.theme.LFCAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LFCAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageUploadScreen(
                        // consumeWindowInsets stops the screen's imePadding from
                        // re-adding the navigation-bar inset Scaffold already applied.
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    )
                }
            }
        }
    }
}

package com.example.offlinenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.offlinenotes.presentation.navigation.AppNavGraph
import com.example.offlinenotes.presentation.theme.OfflineNotesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflineNotesTheme {
                AppNavGraph()
            }
        }
    }
}
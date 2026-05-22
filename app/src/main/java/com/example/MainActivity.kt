package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.StudyShelfApp
import com.example.ui.theme.StudyShelfTheme
import com.example.ui.theme.ThemeOption
import com.example.ui.viewmodel.StudyShelfViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full Edge to Edge content layout
        enableEdgeToEdge()
        
        setContent {
            // Instantiate central ViewModel referencing Room DB and study repository
            val viewModel: StudyShelfViewModel = viewModel(
                factory = StudyShelfViewModel.Factory(applicationContext)
            )
            
            // Observe permanent user theme custom options from DB
            val themeOption by viewModel.themeOption.collectAsStateWithLifecycle()
            val darkTheme = when (themeOption) {
                ThemeOption.LIGHT -> false
                ThemeOption.DARK -> true
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }
            
            StudyShelfTheme(darkTheme = darkTheme) {
                StudyShelfApp(viewModel = viewModel)
            }
        }
    }
}

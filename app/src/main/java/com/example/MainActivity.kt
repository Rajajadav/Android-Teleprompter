package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.PromptDeskNavGraph
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.PromptDeskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesManager = PromptDeskApp.instance.preferencesManager
            val themeMode by preferencesManager.themeMode.collectAsStateWithLifecycle(initialValue = "DARK")
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "SYSTEM" -> androidx.compose.foundation.isSystemInDarkTheme()
                else -> true
            }

            PromptDeskTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    PromptDeskNavGraph(navController = navController)
                }
            }
        }
    }
}


package com.example.intellipaatassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.intellipaatassignment.ui.navigation.AppNavHost
import com.example.intellipaatassignment.ui.theme.IntellipaatAssignmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntellipaatAssignmentTheme {
                AppNavHost()
            }
        }
    }
}

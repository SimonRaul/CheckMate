package com.checkmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.checkmate.app.ui.navigation.CheckMateNavHost
import com.checkmate.app.ui.theme.CheckMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckMateTheme {
                CheckMateNavHost()
            }
        }
    }
}

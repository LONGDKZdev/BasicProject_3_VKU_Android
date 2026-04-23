package com.vohuy.mixueapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.vohuy.mixueapp.ui.theme.MixueOrderAppTheme
import com.vohuy.mixueapp.ui.navigation.NavGraph

/**
 * MainActivity - Activity chính quản lý Compose navigation
 * Sử dụng Jetpack Compose với Navigation Compose
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MixueOrderAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}
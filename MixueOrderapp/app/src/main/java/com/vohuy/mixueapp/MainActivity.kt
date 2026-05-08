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
import com.vohuy.mixueapp.utils.FirebaseHealthCheck
import com.vohuy.mixueapp.utils.FirestoreSampleDataSeeder

/**
 * MainActivity - Activity chính quản lý Compose navigation
 * Sử dụng Jetpack Compose với Navigation Compose
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Test Firebase connections (debug only)
        if (BuildConfig.DEBUG) {
            FirebaseHealthCheck.runAll()

            // Optional: seed sample products so the Home screen can show data immediately.
            // Comment out if you don't want automatic writes on startup.
            FirestoreSampleDataSeeder.seedProducts()
            FirestoreSampleDataSeeder.verifyProductsReadable()
        }
        
        setContent {
            MixueOrderAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}
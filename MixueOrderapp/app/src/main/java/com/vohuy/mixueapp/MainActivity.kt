package com.vohuy.mixueapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.vohuy.mixueapp.ui.navigation.NavGraph
import com.vohuy.mixueapp.ui.theme.MixueAppTheme
import com.vohuy.mixueapp.utils.FirebaseHealthCheck
import com.vohuy.mixueapp.utils.FirestoreSampleDataSeeder

class MainActivity : ComponentActivity() {

    // 1. Tạo biến toàn cục để điều khiển Giao Diện Tối từ bất kỳ đâu
    companion object {
        var isAppInDarkMode = mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (BuildConfig.DEBUG) {
            FirebaseHealthCheck.runAll()
            FirestoreSampleDataSeeder.verifyProductsReadable()
        }

        // 2. Tải cài đặt giao diện từ bộ nhớ máy (SharedPreferences)
        val prefs = getSharedPreferences("MixuePrefs", MODE_PRIVATE)
        isAppInDarkMode.value = prefs.getBoolean("dark_mode", false)

        setContent {
            // 3. Móc biến toàn cục vào Theme của App
            MixueAppTheme(darkTheme = isAppInDarkMode.value) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}
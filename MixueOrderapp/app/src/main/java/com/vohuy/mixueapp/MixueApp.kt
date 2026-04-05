package com.vohuy.mixueapp

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * MixueApp - Application class cho ứng dụng Mixue
 * Khởi tạo Firebase và các configurations toàn cộng
 */
class MixueApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this)
    }
}


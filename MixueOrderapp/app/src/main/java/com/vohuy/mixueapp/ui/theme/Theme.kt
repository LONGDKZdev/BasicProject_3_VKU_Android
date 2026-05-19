package com.vohuy.mixueapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Map màu thương hiệu vào hệ thống Material 3
private val LightColorScheme = lightColorScheme(
    primary = MixueRed, // Đỏ Mixue làm màu chủ đạo (TopBar, Nút bấm, Text nổi bật)
    onPrimary = Color.White, // Chữ trên nền đỏ sẽ màu trắng
    secondary = MixueGreen,
    tertiary = MixueOrange,
    background = Color(0xFFF8F9FA), // Màu nền xám nhạt cho App giống ShopeeFood
    surface = Color.White, // Màu nền của các Card (Thẻ)
)

private val DarkColorScheme = darkColorScheme(
    primary = MixueRed,
    secondary = MixueGreen,
    tertiary = MixueOrange,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun MixueOrderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // TẮT dynamic color để ép máy dùng màu Mixue thay vì màu hình nền điện thoại
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
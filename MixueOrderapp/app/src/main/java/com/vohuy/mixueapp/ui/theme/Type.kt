package com.vohuy.mixueapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.vohuy.mixueapp.utils.ssp


@Composable
fun getResponsiveTypography(): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.ssp,
            lineHeight = 24.ssp,
            letterSpacing = 0.5.ssp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 20.ssp,
            lineHeight = 28.ssp,
            letterSpacing = 0.ssp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 22.ssp,
            lineHeight = 28.ssp,
            letterSpacing = 0.ssp
        ),
        titleMedium = TextStyle( // Bổ sung thêm cho các tiêu đề phụ
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 16.ssp,
            lineHeight = 24.ssp,
            letterSpacing = 0.ssp
        ),
        titleSmall = TextStyle( // Bổ sung cho tên sản phẩm trong Grid
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 14.ssp,
            lineHeight = 20.ssp,
            letterSpacing = 0.ssp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.ssp,
            lineHeight = 20.ssp,
            letterSpacing = 0.25.ssp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.ssp,
            lineHeight = 16.ssp,
            letterSpacing = 0.5.ssp
        )
    )
}
package com.vohuy.mixueapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Lấy chuẩn màn hình cơ bản là 360dp (kích thước chuẩn của Android)
private const val STANDARD_SCREEN_WIDTH = 360f

val Number.sdp: Dp
    @Composable
    get() {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        return (this.toFloat() * (screenWidth / STANDARD_SCREEN_WIDTH)).dp
    }

val Number.ssp: TextUnit
    @Composable
    get() {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        return (this.toFloat() * (screenWidth / STANDARD_SCREEN_WIDTH)).sp
    }
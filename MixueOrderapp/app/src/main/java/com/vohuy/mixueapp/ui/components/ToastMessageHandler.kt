package com.vohuy.mixueapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.vohuy.mixueapp.base.BaseViewModel
import kotlinx.coroutines.delay

@Composable
fun ToastMessageHandler(vararg viewModels: BaseViewModel) {
    var toastMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    viewModels.forEach { viewModel ->
        val errorMessage by viewModel.errorMessage.observeAsState()
        val successMessage by viewModel.successMessage.observeAsState()

        LaunchedEffect(errorMessage) {
            errorMessage?.let {
                if (it.isNotBlank()) {
                    toastMessage = it
                    isError = true
                    isVisible = true
                    viewModel.clearMessages()
                }
            }
        }

        LaunchedEffect(successMessage) {
            successMessage?.let {
                if (it.isNotBlank()) {
                    toastMessage = it
                    isError = false
                    isVisible = true
                    viewModel.clearMessages()
                }
            }
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000)
            isVisible = false
        }
    }

    if (isVisible) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(excludeFromSystemGesture = true)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .background(
                            color = if (isError) Color(0xFFD32F2F) else Color(0xFF388E3C),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = toastMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
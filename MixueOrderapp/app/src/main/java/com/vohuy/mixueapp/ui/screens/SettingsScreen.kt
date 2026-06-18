package com.vohuy.mixueapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import com.vohuy.mixueapp.utils.sdp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    LocalContext.current
    val context = LocalContext.current
    val authVm: com.vohuy.mixueapp.ui.viewmodel.AuthViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    com.vohuy.mixueapp.ui.components.ToastMessageHandler(authVm)

    var darkModeEnabled by remember { mutableStateOf<Boolean>(com.vohuy.mixueapp.MainActivity.isAppInDarkMode.value) }
    var showChangePassword by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordSuccess by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.sdp)
        ) {
            Spacer(modifier = Modifier.height(12.sdp))

            // ==================== ACCOUNT SECTION ====================
            Text(
                text = "Tài Khoản",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.sdp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.sdp)
                    .clickable { navController.navigate("account_management") },
                shape = RoundedCornerShape(12.sdp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.sdp))
                        Column {
                            Text(
                                text = "Quản Lý Tài Khoản",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Xem thông tin & cập nhật tài khoản",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.sdp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.sdp))

            // ==================== CHANGE PASSWORD SECTION ====================
            Text(
                text = "Bảo Mật",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.sdp)
            )

            if (!showChangePassword) {
                Button(
                    onClick = {
                        showChangePassword = true
                        passwordError = ""
                        passwordSuccess = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.sdp),
                    shape = RoundedCornerShape(12.sdp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.sdp)
                    )
                    Spacer(modifier = Modifier.width(8.sdp))
                    Text("Đổi Mật Khẩu", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.sdp),
                    shape = RoundedCornerShape(12.sdp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.sdp)
                    ) {
                        Text(
                            text = "Đổi Mật Khẩu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.sdp))

                        if (passwordError.isNotEmpty()) {
                            Text(
                                text = "❌ $passwordError",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.sdp))
                        }

                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Mật khẩu hiện tại") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.sdp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.sdp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu mới") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(16.sdp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.sdp),
                            horizontalArrangement = Arrangement.spacedBy(12.sdp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showChangePassword = false
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    passwordError = ""
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(10.sdp)
                            ) {
                                Text("Hủy")
                            }
                            Button(
                                onClick = {
                                    // Validate inputs
                                    when {
                                        oldPassword.isBlank() -> passwordError =
                                            "Nhập mật khẩu hiện tại"

                                        newPassword.isBlank() -> passwordError = "Nhập mật khẩu mới"
                                        confirmPassword.isBlank() -> passwordError =
                                            "Xác nhận mật khẩu mới"

                                        newPassword.length < 6 -> passwordError =
                                            "Mật khẩu phải ≥ 6 ký tự"

                                        newPassword != confirmPassword -> passwordError =
                                            "Mật khẩu không khớp"

                                        else -> {
                                            // 1. Gọi lệnh Firebase đổi mật khẩu
                                            authVm.changePassword(oldPassword, newPassword)

                                            // 2. Dọn dẹp ô nhập (GIỮ NGUYÊN FORM ĐỂ XEM THÔNG BÁO TOAST)
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            passwordError = ""
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(10.sdp)
                                // ĐÃ XÓA HOÀN TOÀN DÒNG 'enabled = ...' LÀM LIỆT NÚT
                            ) {
                                Text("Cập nhật")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.sdp))

            // ==================== DISPLAY SECTION ====================

            Text(
                text = "Hiển Thị",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.sdp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.sdp),
                shape = RoundedCornerShape(12.sdp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.sdp))
                        Column {
                            Text(
                                text = "Chế Độ Tối",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Bảo vệ mắt với chế độ tối",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { isDark ->
                            // 1. Cập nhật UI cái nút gạt
                            darkModeEnabled = isDark
                            // 2. Kích hoạt đổi màu toàn App ngay lập tức
                            com.vohuy.mixueapp.MainActivity.isAppInDarkMode.value = isDark
                            // 3. Lưu vào bộ nhớ máy để lần sau mở App vẫn nhớ
                            context.getSharedPreferences(
                                "MixuePrefs",
                                android.content.Context.MODE_PRIVATE
                            )
                                .edit().putBoolean("dark_mode", isDark).apply()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.sdp))

            // ==================== ABOUT SECTION ====================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.sdp),
                shape = RoundedCornerShape(12.sdp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.sdp)
                ) {
                    Text(
                        text = "Về Ứng Dụng",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.sdp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Phiên Bản",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.sdp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nhà Phát Triển",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mixue Team",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.sdp))
        }
    }
}



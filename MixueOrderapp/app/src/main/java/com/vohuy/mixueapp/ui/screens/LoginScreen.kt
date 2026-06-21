package com.vohuy.mixueapp.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vohuy.mixueapp.ui.components.ToastMessageHandler
import com.vohuy.mixueapp.ui.navigation.Routes
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.utils.sdp

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel? = null,
) {
    // FIX 1: Khởi tạo ViewModel an toàn
    val vm: AuthViewModel = viewModel ?: viewModel()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    ToastMessageHandler(vm)

    // Focus Requesters
    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    // States
    val sharedPrefs = remember {
        context.getSharedPreferences(
            "MixueLogin",
            android.content.Context.MODE_PRIVATE
        )
    }

    var email by remember { mutableStateOf(sharedPrefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf(sharedPrefs.getString("saved_password", "") ?: "") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Observers
    val isLoading by vm.isLoading.observeAsState(false)
    val currentUser by vm.currentUser.observeAsState()
    val isLoginTab by vm.isLoginTab.observeAsState(true)

    // Logic validation
    val showConfirm = !isLoginTab
    val passwordMismatch = remember(password, confirmPassword, showConfirm) {
        showConfirm && confirmPassword.isNotBlank() && password != confirmPassword
    }

    val canSubmit = remember(
        isLoginTab,
        email,
        password,
        fullName,
        confirmPassword,
        isLoading,
        passwordMismatch
    ) {
        if (isLoading) return@remember false
        val isEmailValid =
            email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim())
                .matches()
        if (!isEmailValid || password.length < 6) return@remember false
        if (isLoginTab) return@remember true
        fullName.trim().isNotBlank() && confirmPassword.isNotBlank() && !passwordMismatch
    }

    // FIX 2: Xử lý điều hướng an toàn (tránh văng app do lặp điều hướng)
    // Chỉ navigate một lần khi currentUser được set
    val hasNavigated = remember { mutableStateOf(false) }
    LaunchedEffect(currentUser) {
        if (currentUser != null && !hasNavigated.value) {
            hasNavigated.value = true
            navController.navigate(Routes.HOME) {
                popUpTo(0) {
                    inclusive = true
                } // Thay Routes.LOGIN bằng số 0 để dọn sạch hoàn toàn cô lập màn hình cũ
                launchSingleTop = true
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.sdp, vertical = 16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.sdp))

            // Logo hoặc Tiêu đề
            Text(
                text = if (isLoginTab) "Chào mừng trở lại!" else "Tạo tài khoản mới",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.sdp))

            // Tab chọn chế độ
            TabRow(
                selectedTabIndex = if (isLoginTab) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(48.sdp)
                    .fillMaxWidth(),
                indicator = {} // Tùy chỉnh indicator nếu cần
            ) {
                Tab(
                    selected = isLoginTab,
                    onClick = { vm.setLoginTab(true) },
                    text = { Text("Đăng nhập") }
                )
                Tab(
                    selected = !isLoginTab,
                    onClick = { vm.setLoginTab(false) },
                    text = { Text("Đăng ký") }
                )
            }

            Spacer(modifier = Modifier.height(32.sdp))

            // Field: Họ và tên (Chỉ hiện khi Đăng ký)
            AnimatedVisibility(
                visible = !isLoginTab,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester),
                    singleLine = true,
                    shape = RoundedCornerShape(16.sdp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { emailFocusRequester.requestFocus() })
                )
            }

            if (!isLoginTab) Spacer(modifier = Modifier.height(12.sdp))

            // Field: Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                singleLine = true,
                shape = RoundedCornerShape(16.sdp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
            )

            Spacer(modifier = Modifier.height(12.sdp))

            // Field: Mật khẩu
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.sdp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isLoginTab) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { if (!isLoginTab) confirmPasswordFocusRequester.requestFocus() },
                    onDone = {
                        focusManager.clearFocus()
                        if (canSubmit && isLoginTab) vm.loginUser(email.trim(), password)
                    }
                )
            )

            AnimatedVisibility(visible = isLoginTab) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.sdp, end = 4.sdp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            if (email.isNotBlank()) {
                                vm.resetPassword(email.trim())
                            } else {
                                vm.setError("Vui lòng nhập Email vào ô trống phía trên trước!")
                            }
                        }
                    )
                }
            }

            // Field: Nhập lại mật khẩu
            AnimatedVisibility(visible = !isLoginTab) {
                Column {
                    Spacer(modifier = Modifier.height(12.sdp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Xác nhận mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(confirmPasswordFocusRequester),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.sdp),
                        isError = passwordMismatch,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (canSubmit) vm.registerUser(email.trim(), password, fullName.trim())
                        })
                    )
                    if (passwordMismatch) {
                        Text(
                            "Mật khẩu không khớp",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 8.sdp, top = 4.sdp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.sdp))

            // Nút bấm hành động
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isLoginTab) {
                        // LƯU TÀI KHOẢN VÀO BỘ NHỚ
                        sharedPrefs.edit()
                            .putString("saved_email", email.trim())
                            .putString("saved_password", password)
                            .apply()
                        vm.loginUser(email.trim(), password)
                    } else {
                        vm.registerUser(email.trim(), password, fullName.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.sdp),
                shape = RoundedCornerShape(16.sdp),
                enabled = canSubmit
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.sdp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.sdp
                    )
                } else {
                    Text(
                        if (isLoginTab) "ĐĂNG NHẬP" else "TẠO TÀI KHOẢN",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }
}
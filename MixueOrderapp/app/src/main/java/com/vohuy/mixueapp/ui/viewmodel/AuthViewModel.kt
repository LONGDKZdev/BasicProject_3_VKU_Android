package com.vohuy.mixueapp.ui.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.User
import com.vohuy.mixueapp.data.repository.AuthRepository
import com.vohuy.mixueapp.utils.Result

/**
 * AuthViewModel - Xử lý logic xác thực
 * Kế thừa BaseViewModel để quản lý isLoading, errorMessage, successMessage chung
 */
class AuthViewModel : BaseViewModel() {

    private val repository = AuthRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // UI state: Login/Register tabs are merged in LoginScreen.
    private val _isLoginTab = MutableLiveData(true)
    val isLoginTab: LiveData<Boolean> = _isLoginTab

    // Track logout completion for navigation
    private val _logoutComplete = MutableLiveData<Boolean>(false)
    val logoutComplete: LiveData<Boolean> = _logoutComplete

    fun setLoginTab(isLogin: Boolean) {
        _isLoginTab.value = isLogin
        // Clear old messages when switching modes
        clearMessages()
    }

    /**
     * Helper for navigation: check current FirebaseAuth state.
     */
    fun repositoryIsUserLoggedIn(): Boolean = repository.isUserLoggedIn()

    /**
     * Đăng nhập
     */
    fun loginUser(email: String, password: String) {
        setLoading(true)
        val liveData = repository.loginUser(email, password)

        // Tạo một observer có khả năng tự hủy sau khi nhận kết quả
        val observer = object : androidx.lifecycle.Observer<Result<User>> {
            override fun onChanged(value: Result<User>) {
                when (value) {
                    is Result.Success -> {
                        _currentUser.value = value.data
                        setSuccess("Đăng nhập thành công!")
                        // HỦY LẮNG NGHE NGAY LẬP TỨC để chặn đứng vòng lặp nhấp nháy màn hình
                        liveData.removeObserver(this)
                        // Reset currentUser sau khi điều hướng để tránh vòng lặp
                        // Delay 500ms để cho LoginScreen có thời gian navigate tới HOME
                        Handler(Looper.getMainLooper()).postDelayed({
                            _currentUser.value = null
                        }, 500)
                    }
                    is Result.Error -> {
                        setError(value.exception.message ?: "Đăng nhập thất bại")
                        // Thất bại cũng phải hủy lắng nghe để giải phóng bộ nhớ
                        liveData.removeObserver(this)
                    }
                    is Result.Loading -> setLoading(true)
                }
            }
        }
        liveData.observeForever(observer)
    }

    /**
     * Đăng ký người dùng mới
     */
    fun registerUser(email: String, password: String, fullName: String) {
        setLoading(true)
        val liveData = repository.registerUser(email, password, fullName)

        val observer = object : androidx.lifecycle.Observer<Result<User>> {
            override fun onChanged(value: Result<User>) {
                when (value) {
                    is Result.Success -> {
                        _currentUser.value = null
                        setSuccess("Đăng ký thành công! Vui lòng đăng nhập.")
                        _isLoginTab.value = true
                        // Hủy lắng nghe sau khi hoàn tất đăng ký
                        liveData.removeObserver(this)
                    }
                    is Result.Error -> {
                        setError(value.exception.message ?: "Đăng ký thất bại")
                        liveData.removeObserver(this)
                    }
                    is Result.Loading -> setLoading(true)
                }
            }
        }
        liveData.observeForever(observer)
    }

    /**
     * Đăng xuất
     */
    fun logoutUser() {
        setLoading(true)
        _logoutComplete.value = false
        val liveData = repository.logoutUser()

        val observer = object : androidx.lifecycle.Observer<Result<Unit>> {
            override fun onChanged(value: Result<Unit>) {
                when (value) {
                    is Result.Success -> {
                        _currentUser.value = null
                        setSuccess("Đã đăng xuất")
                        _logoutComplete.value = true
                        liveData.removeObserver(this) // Hủy lắng nghe
                    }
                    is Result.Error -> {
                        setError(value.exception.message ?: "Đăng xuất thất bại")
                        _logoutComplete.value = true
                        liveData.removeObserver(this) // Hủy lắng nghe
                    }
                    is Result.Loading -> setLoading(true)
                }
            }
        }
        liveData.observeForever(observer)
    }

    /**
     * Lấy thông tin user hiện tại
     */
    fun fetchCurrentUser() {
        repository.getCurrentUser().observeForever { user ->
            _currentUser.value = user
        }
    }

    /**
     * Reset logout complete flag để tránh lặp navigation
     */
    fun resetLogoutComplete() {
        _logoutComplete.value = false
    }
}


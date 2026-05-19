package com.vohuy.mixueapp.ui.viewmodel

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
     * Đăng ký người dùng mới
     */
    fun registerUser(email: String, password: String, fullName: String) {
        setLoading(true)
        repository.registerUser(email, password, fullName).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    // Repository signs out after creating the account to enforce email verification.
                    // Do NOT set _currentUser here, otherwise UI might navigate to HOME incorrectly.
                    _currentUser.value = null
                    setSuccess("Đăng ký thành công! Vui lòng kiểm tra Email để xác minh trước khi đăng nhập.")
                    _isLoginTab.value = true
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Đăng ký thất bại")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Đăng nhập
     */
    fun loginUser(email: String, password: String) {
        setLoading(true)
        repository.loginUser(email, password).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    setSuccess("Đăng nhập thành công!")
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Đăng nhập thất bại")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Đăng xuất
     */
    fun logoutUser() {
        setLoading(true)
        repository.logoutUser().observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _currentUser.value = null
                    setSuccess("Đã đăng xuất")
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Đăng xuất thất bại")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Lấy thông tin user hiện tại
     */
    fun fetchCurrentUser() {
        repository.getCurrentUser().observeForever { user ->
            _currentUser.value = user
        }
    }
}


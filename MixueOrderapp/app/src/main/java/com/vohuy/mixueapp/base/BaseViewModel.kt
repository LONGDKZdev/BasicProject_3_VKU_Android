package com.vohuy.mixueapp.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Abstract BaseViewModel - Quản lý trạng thái chung cho mọi ViewModel
 * DRY Principle: isLoading, errorMessage, successMessage được sử dụng chung
 * Tất cả ViewModel phải kế thừa lớp này
 */
open class BaseViewModel : ViewModel() {

    // Trạng thái loading (hiển thị/ẩn progress bar)
    val isLoading = MutableLiveData<Boolean>(false)

    // Thông báo lỗi chung
    val errorMessage = MutableLiveData<String>()

    // Thông báo thành công chung
    val successMessage = MutableLiveData<String>()

    /**
     * Set trạng thái loading
     */
    fun setLoading(loading: Boolean) {
        isLoading.value = loading
    }

    /**
     * Set thông báo lỗi
     */
    fun setError(message: String) {
        errorMessage.value = message
        isLoading.value = false
    }

    /**
     * Set thông báo thành công
     */
    fun setSuccess(message: String) {
        successMessage.value = message
        isLoading.value = false
    }

    /**
     * Clear thông báo (sử dụng sau khi hiển thị)
     */
    fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }
}


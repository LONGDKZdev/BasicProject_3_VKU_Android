package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.data.repository.ProductRepository
import com.vohuy.mixueapp.utils.Result

class HomeViewModel : BaseViewModel() {

    private val repository = ProductRepository()
    private var productsListener: ListenerRegistration? = null

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    init {
        loadAllProducts()
    }

    fun loadAllProducts() {
        setLoading(true)
        productsListener?.remove()

        // Gọi đúng hàm lắng nghe Real-time từ Repository mới
        productsListener = repository.listenAllProducts { result ->
            when (result) {
                is Result.Success -> {
                    _products.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải danh sách sản phẩm")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        productsListener?.remove()
    }
}
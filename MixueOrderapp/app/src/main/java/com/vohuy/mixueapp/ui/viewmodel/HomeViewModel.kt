package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.data.repository.ProductRepository
import com.vohuy.mixueapp.utils.Result

/**
 * HomeViewModel - Xử lý logic cho Home Screen
 */
class HomeViewModel : BaseViewModel() {

    private val repository = ProductRepository()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    init {
        loadAllProducts()
    }

    fun loadAllProducts() {
        setLoading(true)
        repository.getAllProducts().observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _products.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải sản phẩm")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }
}


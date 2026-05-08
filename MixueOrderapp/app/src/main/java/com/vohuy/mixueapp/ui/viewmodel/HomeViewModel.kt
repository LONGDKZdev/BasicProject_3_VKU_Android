package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
        val liveData = repository.getAllProducts()
        val observer = object : Observer<Result<List<Product>>> {
            override fun onChanged(value: Result<List<Product>>) {
                when (value) {
                    is Result.Success -> {
                        _products.value = value.data
                        setLoading(false)
                        liveData.removeObserver(this)
                    }
                    is Result.Error -> {
                        setError(value.exception.message ?: "Không thể tải sản phẩm")
                        liveData.removeObserver(this)
                    }
                    is Result.Loading -> setLoading(true)
                }
            }
        }
        liveData.observeForever(observer)
    }
}


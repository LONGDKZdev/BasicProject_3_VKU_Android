package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.data.repository.ProductRepository
import com.vohuy.mixueapp.utils.Result

class ProductViewModel : BaseViewModel() {

    private val repository = ProductRepository()
    private var productListener: ListenerRegistration? = null

    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> = _selectedProduct

    fun loadProductById(productId: String) {
        setLoading(true)
        productListener?.remove()

        productListener = repository.listenProductById(productId) { result ->
            when (result) {
                is Result.Success -> {
                    _selectedProduct.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải chi tiết sản phẩm")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        productListener?.remove()
    }
}
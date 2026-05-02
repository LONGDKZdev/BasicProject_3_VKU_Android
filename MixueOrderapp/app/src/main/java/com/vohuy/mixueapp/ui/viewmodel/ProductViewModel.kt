package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.data.repository.ProductRepository
import com.vohuy.mixueapp.utils.Result

/**
 * ProductViewModel - Xử lý logic sản phẩm
 * Kế thừa BaseViewModel để quản lý isLoading, errorMessage, successMessage chung
 */
class ProductViewModel : BaseViewModel() {

    private val repository = ProductRepository()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> = _selectedProduct

    private val _productsByCategory = MutableLiveData<List<Product>>()
    val productsByCategory: LiveData<List<Product>> = _productsByCategory

    private val _createdProductId = MutableLiveData<String?>()
    val createdProductId: LiveData<String?> = _createdProductId

    /**
     * Tải tất cả sản phẩm
     */
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

    /**
     * Tải sản phẩm theo danh mục
     */
    fun loadProductsByCategory(category: String) {
        setLoading(true)
        repository.getProductsByCategory(category).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _productsByCategory.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải sản phẩm")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Tải sản phẩm theo ID
     */
    fun loadProductById(productId: String) {
        setLoading(true)
        repository.getProductById(productId).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _selectedProduct.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải sản phẩm")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Tải sản phẩm khả dụng
     */
    fun loadAvailableProducts() {
        setLoading(true)
        repository.getAvailableProducts().observeForever { result ->
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

    /**
     * Phase 2: Create product + upload main image to Supabase.
     */
    fun createProductWithImage(
        product: Product,
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ) {
        setLoading(true)
        repository.createProductWithImage(product, imageBytes, contentType).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _createdProductId.value = result.data
                    setSuccess("Tạo sản phẩm thành công")
                }

                is Result.Error -> setError(result.exception.message ?: "Không thể tạo sản phẩm")
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Phase 2: Update product image (upsert) to Supabase + update Firestore fields.
     */
    fun updateProductImage(
        productId: String,
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ) {
        setLoading(true)
        repository.updateProductImage(productId, imageBytes, contentType).observeForever { result ->
            when (result) {
                is Result.Success -> setSuccess("Cập nhật ảnh sản phẩm thành công")
                is Result.Error -> setError(result.exception.message ?: "Không thể cập nhật ảnh")
                is Result.Loading -> setLoading(true)
            }
        }
    }
}


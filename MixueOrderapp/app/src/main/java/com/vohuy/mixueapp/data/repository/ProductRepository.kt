package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

/**
 * ProductRepository - Xử lý tất cả logic sản phẩm từ Firebase
 * DRY Principle: Tất cả Firestore Product operations ở đây
 */
class ProductRepository : BaseRepository() {

    /**
     * Lấy tất cả sản phẩm
     */
    fun getAllProducts(): LiveData<Result<List<Product>>> {
        val result = MutableLiveData<Result<List<Product>>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_PRODUCTS)
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects(Product::class.java)
                result.value = Result.Success(products)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    fun getProductsByCategory(category: String): LiveData<Result<List<Product>>> {
        val result = MutableLiveData<Result<List<Product>>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_CATEGORY, category)
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects(Product::class.java)
                result.value = Result.Success(products)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Lấy sản phẩm theo ID
     */
    fun getProductById(productId: String): LiveData<Result<Product>> {
        val result = MutableLiveData<Result<Product>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    result.value = Result.Success(product)
                } else {
                    result.value = Result.Error(Exception("Không tìm thấy sản phẩm"))
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Lấy sản phẩm khả dụng
     */
    fun getAvailableProducts(): LiveData<Result<List<Product>>> {
        val result = MutableLiveData<Result<List<Product>>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_AVAILABLE, true)
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects(Product::class.java)
                result.value = Result.Success(products)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }
}


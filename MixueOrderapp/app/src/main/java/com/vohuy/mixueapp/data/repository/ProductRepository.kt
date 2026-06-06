package com.vohuy.mixueapp.data.repository

import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

/**
 * ProductRepository - Xử lý logic sản phẩm từ Firestore.
 *
 * Danh sách sản phẩm dùng Snapshot Listener để cập nhật thời gian thực.
 * Ảnh sản phẩm chỉ lưu URL trong field imageUrl.
 */
class ProductRepository : BaseRepository() {

    /**
     * Lắng nghe tất cả sản phẩm theo thời gian thực.
     */
    fun listenAllProducts(
        onResult: (Result<List<Product>>) -> Unit
    ): ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_PRODUCTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                val products = snapshot
                    ?.toObjects(Product::class.java)
                    .orEmpty()
                    .sortedBy { it.name }

                onResult(Result.Success(products))
            }
    }

    /**
     * Lắng nghe sản phẩm theo danh mục theo thời gian thực.
     */
    fun listenProductsByCategory(
        category: String,
        onResult: (Result<List<Product>>) -> Unit
    ): ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_CATEGORY, category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                val products = snapshot
                    ?.toObjects(Product::class.java)
                    .orEmpty()
                    .sortedBy { it.name }

                onResult(Result.Success(products))
            }
    }

    /**
     * Lắng nghe sản phẩm khả dụng theo thời gian thực.
     */
    fun listenAvailableProducts(
        onResult: (Result<List<Product>>) -> Unit
    ): ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_AVAILABLE, true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                val products = snapshot
                    ?.toObjects(Product::class.java)
                    .orEmpty()
                    .sortedBy { it.name }

                onResult(Result.Success(products))
            }
    }

    /**
     * Lắng nghe chi tiết một sản phẩm theo thời gian thực.
     */
    fun listenProductById(
        productId: String,
        onResult: (Result<Product>) -> Unit
    ): ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_PRODUCTS)
            .document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                val product = snapshot?.toObject(Product::class.java)
                if (product != null) {
                    onResult(Result.Success(product))
                } else {
                    onResult(Result.Error(Exception("Không tìm thấy sản phẩm")))
                }
            }
    }
}
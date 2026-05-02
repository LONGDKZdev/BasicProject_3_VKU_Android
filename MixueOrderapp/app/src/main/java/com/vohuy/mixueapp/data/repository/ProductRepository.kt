package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.data.repository.impl.SupabaseImageRepository
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ProductRepository - Xử lý tất cả logic sản phẩm từ Firebase
 * DRY Principle: Tất cả Firestore Product operations ở đây
 */
class ProductRepository : BaseRepository() {

    private val imageRepository: ImageRepository = SupabaseImageRepository()

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

    /**
     * Tạo sản phẩm mới kèm ảnh (ảnh được upload lên Supabase, url/path lưu vào Firestore).
     */
    fun createProductWithImage(
        product: Product,
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ): LiveData<Result<String>> {
        val result = MutableLiveData<Result<String>>()
        result.value = Result.Loading()

        val productId = firestore.collection(Constants.COLLECTION_PRODUCTS).document().id

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageInfo = imageRepository.uploadProductMainImage(
                    productId = productId,
                    bytes = imageBytes,
                    contentType = contentType,
                )

                val newProduct = product.copy(
                    id = productId,
                    imageUrl = imageInfo.publicUrl,
                    imagePath = imageInfo.path,
                )

                firestore.collection(Constants.COLLECTION_PRODUCTS)
                    .document(productId)
                    .set(newProduct)
                    .addOnSuccessListener { result.postValue(Result.Success(productId)) }
                    .addOnFailureListener { exception ->
                        val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                        result.postValue(Result.Error(Exception(errorMessage)))
                    }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.getErrorMessage(e)
                result.postValue(Result.Error(Exception(errorMessage)))
            }
        }

        return result
    }

    /**
     * Cập nhật lại ảnh sản phẩm (upload lên Supabase với upsert=true, rồi update Firestore fields).
     */
    fun updateProductImage(
        productId: String,
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageInfo = imageRepository.uploadProductMainImage(
                    productId = productId,
                    bytes = imageBytes,
                    contentType = contentType,
                )

                firestore.collection(Constants.COLLECTION_PRODUCTS)
                    .document(productId)
                    .update(
                        mapOf(
                            Constants.FIELD_PRODUCT_IMAGE_URL to imageInfo.publicUrl,
                            Constants.FIELD_PRODUCT_IMAGE_PATH to imageInfo.path,
                        )
                    )
                    .addOnSuccessListener { result.postValue(Result.Success(Unit)) }
                    .addOnFailureListener { exception ->
                        val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                        result.postValue(Result.Error(Exception(errorMessage)))
                    }
            } catch (e: Exception) {
                val errorMessage = ErrorHandler.getErrorMessage(e)
                result.postValue(Result.Error(Exception(errorMessage)))
            }
        }

        return result
    }
}


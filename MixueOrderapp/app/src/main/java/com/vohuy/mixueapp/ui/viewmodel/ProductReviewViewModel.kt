package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.ProductReview
import com.vohuy.mixueapp.data.repository.ProductReviewRepository
import com.vohuy.mixueapp.utils.Result

class ProductReviewViewModel : BaseViewModel() {

    private val repository = ProductReviewRepository()
    private var reviewsListener: ListenerRegistration? = null

    private val _reviews = MutableLiveData<List<ProductReview>>(emptyList())
    val reviews: LiveData<List<ProductReview>> = _reviews

    fun listenReviews(productId: String) {
        reviewsListener?.remove()
        if (productId.isBlank()) return

        reviewsListener = repository.listenReviewsByProduct(productId) { result ->
            when (result) {
                is Result.Success -> {
                    _reviews.value = result.data
                    setLoading(false)
                }
                is Result.Error -> setError(result.exception.message ?: "Không thể tải đánh giá")
                is Result.Loading -> setLoading(true)
            }
        }
    }

    fun submitReview(
        productId: String,
        userId: String?,
        userName: String,
        rating: Int,
        comment: String
    ) {
        val uid = userId.orEmpty()
        if (uid.isBlank()) {
            setError("Bạn cần đăng nhập để đánh giá sản phẩm")
            return
        }
        if (comment.trim().length < 3) {
            setError("Vui lòng nhập bình luận ít nhất 3 ký tự")
            return
        }

        repository.submitReview(productId, uid, userName, rating, comment)
            .observeForever { result ->
                when (result) {
                    is Result.Success -> setSuccess("Đã lưu đánh giá của bạn")
                    is Result.Error -> setError(result.exception.message ?: "Không thể lưu đánh giá")
                    is Result.Loading -> setLoading(true)
                }
            }
    }

    fun deleteOwnReview(productId: String, userId: String?) {
        val uid = userId.orEmpty()
        if (uid.isBlank()) {
            setError("Bạn cần đăng nhập để xóa đánh giá")
            return
        }

        repository.deleteOwnReview(productId, uid).observeForever { result ->
            when (result) {
                is Result.Success -> setSuccess("Đã xóa đánh giá")
                is Result.Error -> setError(result.exception.message ?: "Không thể xóa đánh giá")
                is Result.Loading -> setLoading(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reviewsListener?.remove()
    }
}


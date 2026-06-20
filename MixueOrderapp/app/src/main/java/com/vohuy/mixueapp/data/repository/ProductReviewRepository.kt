package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.ProductReview
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

class ProductReviewRepository : BaseRepository() {

    fun listenReviewsByProduct(
        productId: String,
        onResult: (Result<List<ProductReview>>) -> Unit
    ): ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_PRODUCT_REVIEWS)
            .whereEqualTo(Constants.FIELD_REVIEW_PRODUCT_ID, productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.Error(Exception(ErrorHandler.getErrorMessage(error))))
                    return@addSnapshotListener
                }

                val reviews = snapshot?.toObjects(ProductReview::class.java).orEmpty()
                    .sortedByDescending { it.updatedAt }
                onResult(Result.Success(reviews))
            }
    }

    fun submitReview(
        productId: String,
        userId: String,
        userName: String,
        rating: Int,
        comment: String
    ): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        if (productId.isBlank() || userId.isBlank()) {
            result.value = Result.Error(Exception("Bạn cần đăng nhập để đánh giá sản phẩm"))
            return result
        }

        val normalizedRating = rating.coerceIn(1, 5)
        val reviewId = buildReviewId(productId, userId)
        val reviewRef = firestore.collection(Constants.COLLECTION_PRODUCT_REVIEWS).document(reviewId)
        val productRef = firestore.collection(Constants.COLLECTION_PRODUCTS).document(productId)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val productSnapshot = transaction.get(productRef)
            val oldReview = transaction.get(reviewRef).toObject(ProductReview::class.java)
            val oldRating = oldReview?.rating ?: 0

            val currentSum = productSnapshot.getDouble(Constants.FIELD_PRODUCT_RATING_SUM) ?: 0.0
            val currentCount = productSnapshot.getLong(Constants.FIELD_PRODUCT_RATING_COUNT)?.toInt() ?: 0

            val newSum = if (oldReview == null) {
                currentSum + normalizedRating
            } else {
                currentSum - oldRating + normalizedRating
            }.coerceAtLeast(0.0)

            val newCount = if (oldReview == null) currentCount + 1 else currentCount
            val newAverage = if (newCount > 0) newSum / newCount else 0.0

            val review = ProductReview(
                id = reviewId,
                productId = productId,
                userId = userId,
                userName = userName.ifBlank { "Khách hàng" },
                rating = normalizedRating,
                comment = comment.trim(),
                createdAt = oldReview?.createdAt ?: now,
                updatedAt = now
            )

            transaction.set(reviewRef, review)
            transaction.update(
                productRef,
                mapOf(
                    Constants.FIELD_PRODUCT_RATING_SUM to newSum,
                    Constants.FIELD_PRODUCT_RATING_COUNT to newCount,
                    Constants.FIELD_PRODUCT_RATING_AVERAGE to newAverage,
                    Constants.FIELD_PRODUCT_RATING_UPDATED_AT to now
                )
            )
            Unit
        }.addOnSuccessListener {
            result.value = Result.Success(Unit)
        }.addOnFailureListener { exception ->
            result.value = Result.Error(Exception(ErrorHandler.getErrorMessage(exception as Exception)))
        }

        return result
    }

    fun deleteOwnReview(productId: String, userId: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        val reviewId = buildReviewId(productId, userId)
        val reviewRef = firestore.collection(Constants.COLLECTION_PRODUCT_REVIEWS).document(reviewId)
        val productRef = firestore.collection(Constants.COLLECTION_PRODUCTS).document(productId)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val productSnapshot = transaction.get(productRef)
            val oldReview = transaction.get(reviewRef).toObject(ProductReview::class.java)
                ?: return@runTransaction Unit

            val currentSum = productSnapshot.getDouble(Constants.FIELD_PRODUCT_RATING_SUM) ?: 0.0
            val currentCount = productSnapshot.getLong(Constants.FIELD_PRODUCT_RATING_COUNT)?.toInt() ?: 0
            val newCount = (currentCount - 1).coerceAtLeast(0)
            val newSum = (currentSum - oldReview.rating).coerceAtLeast(0.0)
            val newAverage = if (newCount > 0) newSum / newCount else 0.0

            transaction.delete(reviewRef)
            transaction.update(
                productRef,
                mapOf(
                    Constants.FIELD_PRODUCT_RATING_SUM to newSum,
                    Constants.FIELD_PRODUCT_RATING_COUNT to newCount,
                    Constants.FIELD_PRODUCT_RATING_AVERAGE to newAverage,
                    Constants.FIELD_PRODUCT_RATING_UPDATED_AT to now
                )
            )
            Unit
        }.addOnSuccessListener {
            result.value = Result.Success(Unit)
        }.addOnFailureListener { exception ->
            result.value = Result.Error(Exception(ErrorHandler.getErrorMessage(exception as Exception)))
        }

        return result
    }

    private fun buildReviewId(productId: String, userId: String): String = "${productId}_${userId}"
}


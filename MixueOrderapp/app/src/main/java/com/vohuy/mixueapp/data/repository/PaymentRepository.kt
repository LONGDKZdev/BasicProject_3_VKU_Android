package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Transaction
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

/**
 * PaymentRepository - Xử lý lịch sử giao dịch thanh toán từ Firestore.
 */
class PaymentRepository : BaseRepository() {

    /**
     * Tạo giao dịch mới.
     */
    fun createTransaction(transaction: Transaction): LiveData<Result<String>> {
        val result = MutableLiveData<Result<String>>()
        result.value = Result.Loading()

        val normalizedPaymentMethod = transaction.paymentMethod.uppercase()
        val normalizedStatus = transaction.status.uppercase()

        if (normalizedPaymentMethod !in Constants.VALID_PAYMENT_METHODS) {
            result.value = Result.Error(Exception("Phương thức thanh toán không hợp lệ"))
            return result
        }

        if (normalizedStatus !in Constants.VALID_TRANSACTION_STATUSES) {
            result.value = Result.Error(Exception("Trạng thái giao dịch không hợp lệ"))
            return result
        }

        val transactionId = firestore
            .collection(Constants.COLLECTION_TRANSACTIONS)
            .document()
            .id

        val newTransaction = transaction.copy(
            id = transactionId,
            paymentMethod = normalizedPaymentMethod,
            status = normalizedStatus,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection(Constants.COLLECTION_TRANSACTIONS)
            .document(newTransaction.id)
            .set(newTransaction)
            .addOnSuccessListener {
                result.value = Result.Success(newTransaction.id)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Lắng nghe lịch sử giao dịch của user theo thời gian thực.
     */
    fun listenUserTransactions(
        userId: String,
        onResult: (Result<List<Transaction>>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_TRANSACTIONS)
            .whereEqualTo(Constants.FIELD_TRANSACTION_USER_ID, userId)
            // Tạm thời bỏ .orderBy nếu bạn chưa tạo Index trên Firebase để tránh crash
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                val transactions = snapshot?.toObjects(Transaction::class.java).orEmpty()
                    .sortedByDescending { it.createdAt } // Sắp xếp bằng Kotlin
                onResult(Result.Success(transactions))
            }
    }

    /**
     * Lấy giao dịch theo ID.
     */
    fun getTransactionById(transactionId: String): LiveData<Result<Transaction>> {
        val result = MutableLiveData<Result<Transaction>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_TRANSACTIONS)
            .document(transactionId)
            .get()
            .addOnSuccessListener { snapshot ->
                val transaction = snapshot.toObject(Transaction::class.java)
                if (transaction != null) {
                    result.value = Result.Success(transaction)
                } else {
                    result.value = Result.Error(Exception("Giao dịch không tồn tại"))
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Cập nhật trạng thái giao dịch.
     */
    fun updateTransactionStatus(transactionId: String, status: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        val normalizedStatus = status.uppercase()
        if (normalizedStatus !in Constants.VALID_TRANSACTION_STATUSES) {
            result.value = Result.Error(Exception("Trạng thái giao dịch không hợp lệ"))
            return result
        }

        firestore.collection(Constants.COLLECTION_TRANSACTIONS)
            .document(transactionId)
            .update(Constants.FIELD_TRANSACTION_STATUS, normalizedStatus)
            .addOnSuccessListener {
                result.value = Result.Success(Unit)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Cập nhật trạng thái giao dịch theo orderId.
     */
    fun updateTransactionsStatusByOrderId(
        orderId: String,
        status: String
    ): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        val normalizedStatus = status.uppercase()
        if (normalizedStatus !in Constants.VALID_TRANSACTION_STATUSES) {
            result.value = Result.Error(Exception("Trạng thái giao dịch không hợp lệ"))
            return result
        }

        firestore.collection(Constants.COLLECTION_TRANSACTIONS)
            .whereEqualTo(Constants.FIELD_TRANSACTION_ORDER_ID, orderId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    result.value = Result.Success(Unit)
                    return@addOnSuccessListener
                }

                var pendingUpdates = snapshot.size()
                var hasError = false

                snapshot.documents.forEach { document ->
                    document.reference
                        .update(Constants.FIELD_TRANSACTION_STATUS, normalizedStatus)
                        .addOnSuccessListener {
                            pendingUpdates--
                            if (pendingUpdates == 0 && !hasError) {
                                result.value = Result.Success(Unit)
                            }
                        }
                        .addOnFailureListener { exception ->
                            if (!hasError) {
                                hasError = true
                                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                                result.value = Result.Error(Exception(errorMessage))
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }
}
package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Transaction
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result
import com.google.firebase.firestore.Query

/**
 * PaymentRepository - Xử lý lịch sử giao dịch thanh toán từ Firebase
 */
class PaymentRepository : BaseRepository() {

    /**
     * Tạo giao dịch mới
     */
    fun createTransaction(transaction: Transaction): LiveData<Result<String>> {
        val result = MutableLiveData<Result<String>>()
        result.value = Result.Loading()

        val newTransaction = transaction.copy(
            id = firestore.collection("transactions").document().id,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("transactions")
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
     * Lấy lịch sử giao dịch của user
     */
    fun getUserTransactions(userId: String): LiveData<Result<List<Transaction>>> {
        val result = MutableLiveData<Result<List<Transaction>>>()
        result.value = Result.Loading()

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    result.value = Result.Error(Exception(errorMessage))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    result.value = Result.Success(transactions)
                }
            }

        return result
    }

    /**
     * Lấy giao dịch theo ID
     */
    fun getTransactionById(transactionId: String): LiveData<Result<Transaction>> {
        val result = MutableLiveData<Result<Transaction>>()
        result.value = Result.Loading()

        firestore.collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val transaction = snapshot.toObject(Transaction::class.java)
                    if (transaction != null) {
                        result.value = Result.Success(transaction)
                    } else {
                        result.value = Result.Error(Exception("Không thể chuyển đổi dữ liệu"))
                    }
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
     * Cập nhật trạng thái giao dịch
     */
    fun updateTransactionStatus(transactionId: String, status: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        firestore.collection("transactions")
            .document(transactionId)
            .update("status", status)
            .addOnSuccessListener {
                result.value = Result.Success(Unit)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }
}


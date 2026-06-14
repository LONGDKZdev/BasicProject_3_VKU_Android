package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

/**
 * OrderRepository - Xử lý tất cả logic đơn hàng từ Firebase
 * DRY Principle: Tất cả Firestore Order operations ở đây
 */
class OrderRepository : BaseRepository() {

    /**
     * Lắng nghe danh sách đơn hàng theo user (real-time)
     */
    /**
     * Lắng nghe danh sách đơn hàng theo user (real-time)
     */
    fun listenUserOrders(
        userId: String,
        onResult: (Result<List<Order>>) -> Unit
    ): ListenerRegistration {
        onResult(Result.Loading())

        return firestore.collection(Constants.COLLECTION_ORDERS)
            .whereEqualTo(Constants.FIELD_ORDER_USER_ID, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                        .sortedByDescending { it.createdAt }
                    onResult(Result.Success(orders))
                }
            }
    }

    /**
     * Tạo đơn hàng mới
     */
    fun createOrder(order: Order): LiveData<Result<String>> {
        val result = MutableLiveData<Result<String>>()
        result.value = Result.Loading()

        val newOrder = order.copy(
            id = firestore.collection(Constants.COLLECTION_ORDERS).document().id,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection(Constants.COLLECTION_ORDERS)
            .document(newOrder.id)
            .set(newOrder)
            .addOnSuccessListener {
                result.value = Result.Success(newOrder.id)
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Lấy đơn hàng theo ID
     */
    fun getOrderById(orderId: String): LiveData<Result<Order>> {
        val result = MutableLiveData<Result<Order>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_ORDERS)
            .document(orderId)
            .get()
            .addOnSuccessListener { document ->
                val order = document.toObject(Order::class.java)
                if (order != null) {
                    result.value = Result.Success(order)
                } else {
                    result.value = Result.Error(Exception("Không tìm thấy đơn hàng"))
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    /**
     * Cập nhật trạng thái đơn hàng (Có kèm logic hoàn tiền nếu HỦY)
     */
    fun updateOrderStatus(orderId: String, status: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        // 1. Cập nhật trạng thái Đơn hàng
        firestore.collection(Constants.COLLECTION_ORDERS)
            .document(orderId)
            .update(Constants.FIELD_ORDER_STATUS, status)
            .addOnSuccessListener {

                // 2. LOGIC HOÀN TIỀN: Nếu là Hủy Đơn, tìm và ép Giao Dịch thành FAILED
                if (status == Constants.ORDER_STATUS_CANCELLED) {
                    firestore.collection(Constants.COLLECTION_TRANSACTIONS)
                        .whereEqualTo(Constants.FIELD_TRANSACTION_ORDER_ID, orderId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            snapshot.documents.forEach { doc ->
                                doc.reference.update(
                                    Constants.FIELD_TRANSACTION_STATUS,
                                    Constants.TRANSACTION_STATUS_FAILED
                                )
                            }
                            result.value = Result.Success(Unit)
                        }
                        .addOnFailureListener { e ->
                            result.value =
                                Result.Error(Exception("Đã hủy đơn nhưng lỗi hoàn tiền: ${e.message}"))
                        }
                } else {
                    result.value = Result.Success(Unit)
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = ErrorHandler.getErrorMessage(exception as Exception)
                result.value = Result.Error(Exception(errorMessage))
            }
 
        return result
    }

    /**
     * Xóa đơn hàng
     */
    fun deleteOrder(orderId: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_ORDERS)
            .document(orderId)
            .delete()
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
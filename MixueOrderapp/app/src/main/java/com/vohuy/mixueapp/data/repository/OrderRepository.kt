package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result
import com.google.firebase.firestore.ListenerRegistration

/**
 * OrderRepository - Xử lý tất cả logic đơn hàng từ Firebase
 * DRY Principle: Tất cả Firestore Order operations ở đây
 */

class OrderRepository : BaseRepository() {

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
            .orderBy(
                Constants.FIELD_ORDER_CREATED_AT,
                com.google.firebase.firestore.Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    onResult(Result.Error(Exception(errorMessage)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
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
     * Lấy tất cả đơn hàng của user
     */
    fun getUserOrders(userId: String): LiveData<Result<List<Order>>> {
        val result = MutableLiveData<Result<List<Order>>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_ORDERS)
            .whereEqualTo(Constants.FIELD_ORDER_USER_ID, userId)
            .orderBy(Constants.FIELD_ORDER_CREATED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
            // NÂNG CẤP: Đổi .get() thành Lắng nghe Real-time
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = ErrorHandler.getErrorMessage(error)
                    result.value = Result.Error(Exception(errorMessage))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    result.value = Result.Success(orders)
                }
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
    fun updateOrderStatus(orderId: String, status: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        firestore.collection(Constants.COLLECTION_ORDERS)
            .document(orderId)
            .update(Constants.FIELD_ORDER_STATUS, status)
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

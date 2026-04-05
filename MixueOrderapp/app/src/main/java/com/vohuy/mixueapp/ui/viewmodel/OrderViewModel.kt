package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.data.repository.OrderRepository
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.Result

/**
 * OrderViewModel - Xử lý logic đơn hàng
 * Kế thừa BaseViewModel để quản lý isLoading, errorMessage, successMessage chung
 */
class OrderViewModel : BaseViewModel() {

    private val repository = OrderRepository()

    private val _userOrders = MutableLiveData<List<Order>>()
    val userOrders: LiveData<List<Order>> = _userOrders

    private val _selectedOrder = MutableLiveData<Order?>()
    val selectedOrder: LiveData<Order?> = _selectedOrder

    private val _createdOrderId = MutableLiveData<String>()
    val createdOrderId: LiveData<String> = _createdOrderId

    /**
     * Tạo đơn hàng mới
     */
    fun createOrder(userId: String, items: List<OrderItem>) {
        if (items.isEmpty()) {
            setError("Giỏ hàng trống")
            return
        }

        setLoading(true)
        val order = Order(
            userId = userId,
            items = items,
            status = Constants.ORDER_STATUS_PENDING
        )

        repository.createOrder(order).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _createdOrderId.value = result.data
                    setSuccess("Đơn hàng được tạo thành công!")
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tạo đơn hàng")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Tải danh sách đơn hàng của user
     */
    fun loadUserOrders(userId: String) {
        setLoading(true)
        repository.getUserOrders(userId).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _userOrders.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải đơn hàng")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Tải đơn hàng theo ID
     */
    fun loadOrderById(orderId: String) {
        setLoading(true)
        repository.getOrderById(orderId).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _selectedOrder.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải đơn hàng")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    fun updateOrderStatus(orderId: String, status: String) {
        setLoading(true)
        repository.updateOrderStatus(orderId, status).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    setSuccess("Cập nhật trạng thái thành công!")
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể cập nhật trạng thái")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Hủy đơn hàng
     */
    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, Constants.ORDER_STATUS_CANCELLED)
    }
}

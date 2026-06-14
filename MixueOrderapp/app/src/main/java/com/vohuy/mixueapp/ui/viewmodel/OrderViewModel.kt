package com.vohuy.mixueapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.data.model.Transaction
import com.vohuy.mixueapp.data.repository.OrderRepository
import com.vohuy.mixueapp.data.repository.PaymentRepository
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.Result

/**
 * OrderViewModel - Xử lý logic đơn hàng.
 * UI chỉ gọi ViewModel, ViewModel gọi Repository, không gọi Firestore trực tiếp.
 */
class OrderViewModel : BaseViewModel() {

    private val repository = OrderRepository()
    private val paymentRepository = PaymentRepository()

    private var ordersListener: ListenerRegistration? = null

    private val _userOrders = MutableLiveData<List<Order>>()
    val userOrders: LiveData<List<Order>> = _userOrders

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _selectedOrder = MutableLiveData<Order?>()
    val selectedOrder: LiveData<Order?> = _selectedOrder

    private val _createdOrderId = MutableLiveData<String>()
    val createdOrderId: LiveData<String> = _createdOrderId

    /**
     * Tạo đơn hàng mới.
     */
    fun createOrder(
        userId: String,
        items: List<OrderItem>,
        customerName: String = "",
        phoneNumber: String = "",
        address: String = "",
        paymentMethod: String = Constants.PAYMENT_METHOD_BANK_TRANSFER
    ) {
        // 1. KIỂM TRA CHỐNG SPAM (Dùng luôn danh sách đã tải trong ViewModel)
        val currentOrders = orders.value ?: emptyList()
        val pendingCount = currentOrders.count { it.status == Constants.ORDER_STATUS_PENDING }

        if (pendingCount >= 3) {
            setError("⛔ Bạn đang có quá 3 đơn hàng chờ duyệt. Vui lòng đợi quán xử lý trước!")
            return // Chặn luôn không cho chạy tiếp
        }
        if (items.isEmpty()) {
            setError("Giỏ hàng trống")
            return
        }

        val normalizedPaymentMethod = paymentMethod.uppercase()
        if (normalizedPaymentMethod !in Constants.VALID_PAYMENT_METHODS) {
            setError("Phương thức thanh toán không hợp lệ")
            return
        }

        setLoading(true)

        val calculatedTotal = items.sumOf { it.getTotalPrice() }

        val order = Order(
            userId = userId,
            customerName = customerName,
            phoneNumber = phoneNumber,
            address = address,
            paymentMethod = normalizedPaymentMethod,
            items = items,
            status = Constants.ORDER_STATUS_PENDING,
            totalPrice = calculatedTotal
        )

        repository.createOrder(order).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _createdOrderId.value = result.data
                    setSuccess("Đơn hàng được tạo thành công!")

                    createPendingTransaction(
                        userId = userId,
                        customerName = customerName,
                        orderId = result.data,
                        amount = calculatedTotal,
                        paymentMethod = normalizedPaymentMethod
                    )
                }

                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tạo đơn hàng")
                }

                is Result.Loading -> {
                    setLoading(true)
                }
            }
        }
    }

    /**
     * Tạo giao dịch chờ xử lý sau khi tạo đơn hàng.
     */
    private fun createPendingTransaction(
        userId: String,
        customerName: String,
        orderId: String,
        amount: Double,
        paymentMethod: String
    ) {
        val transaction = Transaction(
            userId = userId,
            customerName = customerName,
            orderId = orderId,
            amount = amount,
            paymentMethod = paymentMethod,
            status = Constants.TRANSACTION_STATUS_PENDING,
            description = "Thanh toán đơn hàng #$orderId"
        )

        paymentRepository.createTransaction(transaction).observeForever { transactionResult ->
            if (transactionResult is Result.Error) {
                Log.e(
                    "OrderViewModel",
                    "Không thể tạo giao dịch: ${transactionResult.exception.message}"
                )
            }
        }
    }

    /**
     * Tải và lắng nghe danh sách đơn hàng của user theo thời gian thực.
     */
    fun loadUserOrders(userId: String) {
        setLoading(true)

        ordersListener?.remove()

        ordersListener = repository.listenUserOrders(userId) { result ->
            when (result) {
                is Result.Success -> {
                    _userOrders.value = result.data
                    _orders.value = result.data
                    setLoading(false)
                }

                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải đơn hàng")
                }

                is Result.Loading -> {
                    setLoading(true)
                }
            }
        }
    }

    /**
     * Tải đơn hàng theo ID.
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

                is Result.Loading -> {
                    setLoading(true)
                }
            }
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng và đồng bộ trạng thái giao dịch liên quan.
     */
    fun updateOrderStatus(orderId: String, status: String) {
        val normalizedStatus = status.uppercase()

        if (normalizedStatus !in Constants.VALID_ORDER_STATUSES) {
            setError("Trạng thái đơn hàng không hợp lệ")
            return
        }

        setLoading(true)

        repository.updateOrderStatus(orderId, normalizedStatus).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    syncTransactionStatusAfterOrderStatusChanged(orderId, normalizedStatus)
                    setSuccess("Cập nhật trạng thái đơn hàng thành công")
                }

                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể cập nhật trạng thái đơn hàng")
                }

                is Result.Loading -> {
                    setLoading(true)
                }
            }
        }
    }

    /**
     * Đồng bộ trạng thái giao dịch dựa trên trạng thái đơn hàng.
     */
    private fun syncTransactionStatusAfterOrderStatusChanged(orderId: String, orderStatus: String) {
        val transactionStatus = when (orderStatus) {
            Constants.ORDER_STATUS_CANCELLED -> Constants.TRANSACTION_STATUS_FAILED
            Constants.ORDER_STATUS_CONFIRMED,
            Constants.ORDER_STATUS_DELIVERING -> Constants.TRANSACTION_STATUS_SUCCESS

            else -> Constants.TRANSACTION_STATUS_PENDING
        }

        paymentRepository
            .updateTransactionsStatusByOrderId(orderId, transactionStatus)
            .observeForever { result ->
                if (result is Result.Error) {
                    Log.e(
                        "OrderViewModel",
                        "Không thể đồng bộ giao dịch: ${result.exception.message}"
                    )
                }
            }
    }

    /**
     * Hủy đơn hàng.
     */
    fun cancelOrder(orderId: String) {
        setLoading(true)
        repository.updateOrderStatus(orderId, Constants.ORDER_STATUS_CANCELLED)
            .observeForever { result ->
                when (result) {
                    is Result.Success -> {
                        setSuccess("Đã hủy đơn và hoàn tiền thành công!")
                    }

                    is Result.Error -> {
                        setError(result.exception.message ?: "Không thể hủy đơn hàng")
                    }

                    is Result.Loading -> {
                        setLoading(true)
                    }
                }
            }
    }

    fun resetOrderState() {
        _createdOrderId.value = ""
        clearMessages()
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
    }
}
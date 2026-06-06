package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Transaction
import com.vohuy.mixueapp.data.repository.PaymentRepository
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.Result

/**
 * PaymentViewModel - Xử lý logic thanh toán và lịch sử giao dịch.
 */
class PaymentViewModel : BaseViewModel() {

    private val repository = PaymentRepository()
    
    // Đã thêm biến ăng-ten lắng nghe Real-time
    private var transactionListener: ListenerRegistration? = null

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _selectedTransaction = MutableLiveData<Transaction?>()
    val selectedTransaction: LiveData<Transaction?> = _selectedTransaction

    /**
     * Tạo giao dịch thanh toán.
     */
    fun createTransaction(
        userId: String,
        orderId: String,
        amount: Double,
        customerName: String = "",
        paymentMethod: String = Constants.PAYMENT_METHOD_CASH,
        status: String = Constants.TRANSACTION_STATUS_SUCCESS
    ) {
        val normalizedPaymentMethod = paymentMethod.uppercase()
        val normalizedStatus = status.uppercase()

        if (normalizedPaymentMethod !in Constants.VALID_PAYMENT_METHODS) {
            setError("Phương thức thanh toán không hợp lệ")
            return
        }

        if (normalizedStatus !in Constants.VALID_TRANSACTION_STATUSES) {
            setError("Trạng thái giao dịch không hợp lệ")
            return
        }

        setLoading(true)

        val transaction = Transaction(
            userId = userId,
            customerName = customerName,
            orderId = orderId,
            amount = amount,
            paymentMethod = normalizedPaymentMethod,
            status = normalizedStatus,
            description = "Thanh toán đơn hàng #$orderId"
        )

        repository.createTransaction(transaction).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    setSuccess("Giao dịch thanh toán thành công!")
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tạo giao dịch")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Lắng nghe danh sách giao dịch Real-time an toàn (Đã khử lỗi tràn RAM)
     */
    fun loadUserTransactions(userId: String) {
        setLoading(true)
        transactionListener?.remove() // Tắt ăng-ten cũ trước khi bật cái mới

        transactionListener = repository.listenUserTransactions(userId) { result ->
            when (result) {
                is Result.Success -> {
                    _transactions.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải giao dịch")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    /**
     * Tải giao dịch theo ID.
     */
    fun loadTransactionById(transactionId: String) {
        setLoading(true)

        repository.getTransactionById(transactionId).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _selectedTransaction.value = result.data
                    setLoading(false)
                }
                is Result.Error -> {
                    setError(result.exception.message ?: "Không thể tải giao dịch")
                }
                is Result.Loading -> setLoading(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        transactionListener?.remove() // Cực kỳ quan trọng: Tắt hoàn toàn khi thoát
    }
}
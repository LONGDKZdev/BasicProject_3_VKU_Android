package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.Transaction
import com.vohuy.mixueapp.data.repository.PaymentRepository
import com.vohuy.mixueapp.utils.Result

/**
 * PaymentViewModel - Xử lý logic thanh toán & lịch sử giao dịch
 */
class PaymentViewModel : BaseViewModel() {

    private val repository = PaymentRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _selectedTransaction = MutableLiveData<Transaction?>()
    val selectedTransaction: LiveData<Transaction?> = _selectedTransaction

    /**
     * Tạo giao dịch (khi thanh toán đơn hàng)
     */
    fun createTransaction(
        userId: String,
        orderId: String,
        amount: Double,
        paymentMethod: String = "CASH"
    ) {
        setLoading(true)

        val transaction = Transaction(
            userId = userId,
            orderId = orderId,
            amount = amount,
            paymentMethod = paymentMethod,
            status = "SUCCESS",
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
     * Tải danh sách giao dịch của user
     */
    fun loadUserTransactions(userId: String) {
        setLoading(true)
        repository.getUserTransactions(userId).observeForever { result ->
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
     * Tải giao dịch theo ID
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
}


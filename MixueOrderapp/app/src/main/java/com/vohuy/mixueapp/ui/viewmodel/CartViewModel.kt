package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.data.model.Voucher
import com.vohuy.mixueapp.data.repository.VoucherRepository
import com.vohuy.mixueapp.utils.Result

/**
 * CartViewModel - Xử lý logic giỏ hàng
 * Kế thừa BaseViewModel để quản lý isLoading, errorMessage, successMessage chung
 */
class CartViewModel : BaseViewModel() {

    private val voucherRepository = VoucherRepository()

    private val _cartItems = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<OrderItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    private val _subtotalPrice = MutableLiveData<Double>(0.0)
    val subtotalPrice: LiveData<Double> = _subtotalPrice

    private val _discountAmount = MutableLiveData<Double>(0.0)
    val discountAmount: LiveData<Double> = _discountAmount

    private val _appliedVoucher = MutableLiveData<Voucher?>()
    val appliedVoucher: LiveData<Voucher?> = _appliedVoucher

    /**
     * Thêm sản phẩm vào giỏ
     */
    fun addItem(
        product: Product,
        quantity: Int = 1,
        size: String = "",
        sugarLevel: String = "",
        iceLevel: String = "",
        toppings: List<String> = emptyList(),
        note: String = "",
        extraPrice: Double = 0.0
    ) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val itemId = buildItemId(product.id, size, sugarLevel, iceLevel, toppings, note)
        val existingItem = currentItems.find { it.id == itemId }

        if (existingItem != null) {
            existingItem.apply {
                this.quantity += quantity
            }
        } else {
            currentItems.add(
                OrderItem(
                    id = itemId,
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    price = product.price + extraPrice,
                    imageUrl = product.imageUrl,
                    size = size,
                    sugarLevel = sugarLevel,
                    iceLevel = iceLevel,
                    toppings = toppings,
                    note = note
                )
            )
        }

        _cartItems.value = ArrayList(currentItems)
        updateTotalPrice()
    }

    /**
     * Xóa sản phẩm khỏi giỏ
     */
// Trong CartViewModel.kt, sửa hàm removeItem:
    fun removeItem(itemId: String) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val newList = currentItems.filter { it.id != itemId }.toMutableList()
        _cartItems.value = newList
        updateTotalPrice()
    }

    /**
     * Xóa sản phẩm khỏi giỏ (alias cho Compose)
     */
    fun removeFromCart(itemId: String) {
        removeItem(itemId)
    }

    /**
     * Cập nhật số lượng sản phẩm
     */
    fun updateItemQuantity(itemId: String, quantity: Int) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val item = currentItems.find { it.id == itemId }
        if (item != null) {
            if (quantity <= 0) {
                removeItem(itemId)
            } else {
                item.quantity = quantity
                _cartItems.value = ArrayList(currentItems)
                updateTotalPrice()
            }
        }
    }

    /**
     * Xóa tất cả sản phẩm khỏi giỏ
     */
    fun clearCart() {
        _cartItems.value = mutableListOf()
        _subtotalPrice.value = 0.0
        _discountAmount.value = 0.0
        _appliedVoucher.value = null
        _totalPrice.value = 0.0
    }

    fun addOrderItems(items: List<OrderItem>) {
        val currentItems = _cartItems.value ?: mutableListOf()
        items.forEach { source ->
            val existing = currentItems.find { it.id == source.id }
            if (existing != null) {
                existing.quantity += source.quantity
            } else {
                currentItems.add(source.copy())
            }
        }
        _cartItems.value = ArrayList(currentItems)
        updateTotalPrice()
        setSuccess("Đã thêm lại món vào giỏ hàng")
    }

    fun applyVoucher(code: String) {
        val subtotal = _subtotalPrice.value ?: 0.0
        if (subtotal <= 0.0) {
            setError("Giỏ hàng đang trống")
            return
        }

        voucherRepository.findVoucherByCode(code).observeForever { result ->
            when (result) {
                is Result.Success -> {
                    val voucher = result.data
                    if (voucher == null) {
                        clearVoucher()
                        setError("Mã giảm giá không tồn tại hoặc đã hết hạn")
                        return@observeForever
                    }

                    val discount = voucher.calculateDiscount(subtotal)
                    if (discount <= 0.0) {
                        clearVoucher()
                        setError("Đơn hàng chưa đủ điều kiện áp dụng mã")
                    } else {
                        _appliedVoucher.value = voucher
                        _discountAmount.value = discount
                        updateTotalPrice()
                        setSuccess("Đã áp dụng mã ${voucher.code}")
                    }
                }
                is Result.Error -> setError(result.exception.message ?: "Không thể áp dụng mã")
                is Result.Loading -> setLoading(true)
            }
        }
    }

    fun clearVoucher() {
        _appliedVoucher.value = null
        _discountAmount.value = 0.0
        updateTotalPrice()
    }

    /**
     * Tính tổng tiền
     */
    private fun updateTotalPrice() {
        val subtotal = _cartItems.value?.sumOf { it.getTotalPrice() } ?: 0.0
        val voucher = _appliedVoucher.value
        val discount = voucher?.calculateDiscount(subtotal) ?: 0.0
        _subtotalPrice.value = subtotal
        _discountAmount.value = discount
        _totalPrice.value = (subtotal - discount).coerceAtLeast(0.0)
    }

    /**
     * Kiểm tra giỏ hàng có trống không
     */
    fun isCartEmpty(): Boolean {
        return _cartItems.value?.isEmpty() ?: true
    }

    private fun buildItemId(
        productId: String,
        size: String,
        sugarLevel: String,
        iceLevel: String,
        toppings: List<String>,
        note: String
    ): String {
        val optionKey = listOf(
            size,
            sugarLevel,
            iceLevel,
            toppings.sorted().joinToString("+"),
            note.trim()
        ).joinToString("|")
        return "$productId#$optionKey"
    }
}


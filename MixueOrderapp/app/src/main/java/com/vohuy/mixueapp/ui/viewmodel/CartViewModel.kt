package com.vohuy.mixueapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseViewModel
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.data.model.Product

/**
 * CartViewModel - Xử lý logic giỏ hàng
 * Kế thừa BaseViewModel để quản lý isLoading, errorMessage, successMessage chung
 */
class CartViewModel : BaseViewModel() {

    private val _cartItems = MutableLiveData<MutableList<OrderItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<OrderItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    /**
     * Thêm sản phẩm vào giỏ
     */
    fun addItem(product: Product, quantity: Int = 1) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val existingItem = currentItems.find { it.productId == product.id }

        if (existingItem != null) {
            // Nếu sản phẩm đã có trong giỏ, tăng số lượng
            existingItem.apply {
                this.quantity += quantity
            }
        } else {
            // Thêm sản phẩm mới vào giỏ
            currentItems.add(
                OrderItem(
                    id = product.id,
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    pricePerUnit = product.price,
                    productImage = product.imageUrl,
                )
            )
        }

        _cartItems.value = ArrayList(currentItems)
        updateTotalPrice()
    }

    /**
     * Xóa sản phẩm khỏi giỏ
     */
    fun removeItem(productId: String) {
        val currentItems = _cartItems.value ?: mutableListOf()
        currentItems.removeAll { it.productId == productId }
        _cartItems.value = ArrayList(currentItems)
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
    fun updateItemQuantity(productId: String, quantity: Int) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val item = currentItems.find { it.productId == productId }
        if (item != null) {
            if (quantity <= 0) {
                removeItem(productId)
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
        _totalPrice.value = 0.0
    }

    /**
     * Tính tổng tiền
     */
    private fun updateTotalPrice() {
        val total = _cartItems.value?.sumOf { it.getTotalPrice() } ?: 0.0
        _totalPrice.value = total
    }

    /**
     * Kiểm tra giỏ hàng có trống không
     */
    fun isCartEmpty(): Boolean {
        return _cartItems.value?.isEmpty() ?: true
    }
}


package com.vohuy.mixueapp.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vohuy.mixueapp.databinding.FragmentCartBinding
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.formatPrice
import com.vohuy.mixueapp.utils.showToast

/**
 * CartFragment - Màn hình giỏ hàng
 */
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        orderViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnCheckout.setOnClickListener {
            if (cartViewModel.isCartEmpty()) {
                showToast("Giỏ hàng trống")
            } else {
                // TODO: Create order with current user ID
                // val userId = getCurrentUserId()
                // orderViewModel.createOrder(userId, cartViewModel.cartItems.value ?: emptyList())
            }
        }

        binding.btnContinueShopping.setOnClickListener {
            // TODO: Navigate back to HomeFragment
        }

        binding.btnClearCart.setOnClickListener {
            cartViewModel.clearCart()
            showToast("Giỏ hàng đã được xóa")
        }
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            // TODO: Update cart items RecyclerView
            binding.tvItemCount.text = "Số lượng: ${items.size} sản phẩm"
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            binding.tvTotalPrice.text = "Tổng tiền: ${total.formatPrice()}"
        }

        orderViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                showToast(message)
                cartViewModel.clearCart()
                // TODO: Navigate to OrderHistoryFragment
            }
        }

        orderViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                showToast(error)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

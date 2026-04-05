package com.vohuy.mixueapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vohuy.mixueapp.databinding.FragmentProductDetailBinding
import com.vohuy.mixueapp.ui.viewmodel.ProductViewModel
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.utils.formatPrice
import com.vohuy.mixueapp.utils.showToast

/**
 * ProductDetailFragment - Màn hình chi tiết sản phẩm
 */
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel

    private var productId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("product_id")
        productId?.let {
            productViewModel.loadProductById(it)
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnAddToCart.setOnClickListener {
            productViewModel.selectedProduct.value?.let { product ->
                val quantity = binding.etQuantity.text.toString().toIntOrNull() ?: 1
                if (quantity > 0) {
                    cartViewModel.addItem(product, quantity)
                    showToast("Đã thêm vào giỏ hàng")
                } else {
                    showToast("Số lượng phải lớn hơn 0")
                }
            }
        }

        binding.btnBack.setOnClickListener {
            // TODO: Navigate back
        }
    }

    private fun observeViewModel() {
        productViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        productViewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            product?.let {
                // TODO: Load image with Glide
                binding.tvProductName.text = it.name
                binding.tvProductDescription.text = it.description
                binding.tvProductPrice.text = it.price.formatPrice()
            }
        }

        productViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
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

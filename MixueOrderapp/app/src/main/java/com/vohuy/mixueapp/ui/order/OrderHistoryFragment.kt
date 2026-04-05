package com.vohuy.mixueapp.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vohuy.mixueapp.databinding.FragmentOrderHistoryBinding
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.showToast

/**
 * OrderHistoryFragment - Màn hình lịch sử đơn hàng
 */
class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: OrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(OrderViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()

        // TODO: Get current user ID
        // viewModel.loadUserOrders(getCurrentUserId())
    }

    private fun setupUI() {
        // TODO: Setup RecyclerView for orders list
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.userOrders.observe(viewLifecycleOwner) { orders ->
            // TODO: Update RecyclerView with orders
            binding.tvOrderCount.text = "Số đơn hàng: ${orders.size}"
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
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

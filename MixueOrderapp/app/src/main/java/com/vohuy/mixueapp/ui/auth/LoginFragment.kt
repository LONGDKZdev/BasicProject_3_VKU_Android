package com.vohuy.mixueapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vohuy.mixueapp.databinding.FragmentLoginBinding
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.utils.isValidEmail
import com.vohuy.mixueapp.utils.isValidPassword
import com.vohuy.mixueapp.utils.showToast

/**
 * LoginFragment - Màn hình đăng nhập
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInput(email, password)) {
                viewModel.loginUser(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            // TODO: Navigate to RegisterFragment
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                showToast(error)
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                showToast(message)
                // TODO: Navigate to HomeFragment
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showToast("Email không được để trống")
                false
            }
            !email.isValidEmail() -> {
                showToast("Email không hợp lệ")
                false
            }
            password.isEmpty() -> {
                showToast("Mật khẩu không được để trống")
                false
            }
            !password.isValidPassword() -> {
                showToast("Mật khẩu phải có ít nhất 6 ký tự")
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

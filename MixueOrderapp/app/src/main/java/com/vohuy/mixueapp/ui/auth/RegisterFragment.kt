package com.vohuy.mixueapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vohuy.mixueapp.databinding.FragmentRegisterBinding
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.utils.isValidEmail
import com.vohuy.mixueapp.utils.isValidPassword
import com.vohuy.mixueapp.utils.isValidPhone
import com.vohuy.mixueapp.utils.showToast

/**
 * RegisterFragment - Màn hình đăng ký
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (validateInput(email, password, fullName, phone)) {
                viewModel.registerUser(email, password, fullName)
            }
        }

        binding.tvLogin.setOnClickListener {
            // TODO: Navigate back to LoginFragment
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                showToast(error)
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                showToast(message)
                // TODO: Navigate to LoginFragment
            }
        }
    }

    private fun validateInput(email: String, password: String, fullName: String, phone: String): Boolean {
        return when {
            fullName.isEmpty() -> {
                showToast("Họ tên không được để trống")
                false
            }
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
            phone.isEmpty() -> {
                showToast("Số điện thoại không được để trống")
                false
            }
            !phone.isValidPhone() -> {
                showToast("Số điện thoại không hợp lệ")
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

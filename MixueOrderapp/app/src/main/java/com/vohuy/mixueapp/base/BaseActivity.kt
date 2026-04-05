package com.vohuy.mixueapp.base

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * Abstract BaseActivity - Quản lý ViewBinding, Toast, Loading progressively
 * Tất cả Activity phải kế thừa lớp này để tự động có: ViewBinding, Toast, ProgressDialog
 * DRY Principle: Không cần viết lại logic ViewBinding, Toast ở từng Activity
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        setupUI()
        observeViewModel()
    }

    /**
     * Abstract method để inflate ViewBinding cho từng Activity
     * Mỗi Activity con sẽ implement để trả về binding của nó
     */
    abstract fun inflateBinding(inflater: android.view.LayoutInflater): VB

    /**
     * Setup UI elements (buttons, listeners, etc.)
     * Activity con override để setup các views của nó
     */
    abstract fun setupUI()

    /**
     * Observe ViewModel LiveData
     * Activity con override nếu cần observe
     */
    open fun observeViewModel() {}

    /**
     * Hiển thị ProgressDialog loading
     */
    fun showLoading(message: String = "Đang tải...") {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        progressDialog?.apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    /**
     * Ẩn ProgressDialog loading
     */
    fun hideLoading() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    /**
     * Hiển thị Toast notification
     */
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }
}


package com.vohuy.mixueapp.base

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Abstract BaseRepository - Quản lý Firebase instances chung
 * DRY Principle: Không cần khởi tạo Firebase ở mỗi Repository
 * Tất cả Repository phải kế thừa lớp này
 */
open class BaseRepository {

    protected val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    protected val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    protected val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    /**
     * Lấy ID của user hiện tại
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Check xem user đã đăng nhập chưa
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Đăng xuất user
     */
    fun logout() {
        auth.signOut()
    }
}


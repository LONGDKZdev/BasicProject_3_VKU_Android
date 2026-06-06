package com.vohuy.mixueapp.base

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * BaseRepository
 *
 * Chỉ quản lý Firebase Auth và Firestore.
 * Ảnh của dự án được lưu bằng Supabase Storage, không dùng Firebase Storage.
 */
open class BaseRepository {

    protected val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    protected val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Lấy ID của user hiện tại
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
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
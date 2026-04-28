package com.vohuy.mixueapp.data.remote.supabase

/**
 * Centralized path conventions for objects stored in Supabase Storage.
 */
object SupabasePaths {
    fun userAvatar(uid: String): String = "users/$uid/avatar.jpg"
    fun productMainImage(productId: String): String = "products/$productId/main.jpg"
}


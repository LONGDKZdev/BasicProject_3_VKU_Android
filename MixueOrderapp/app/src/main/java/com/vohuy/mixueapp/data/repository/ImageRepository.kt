package com.vohuy.mixueapp.data.repository

/**
 * ImageRepository defines how the app uploads and manages images.
 *
 * In this project, 100% images will be stored in Supabase Storage (public bucket).
 */
interface ImageRepository {
    data class ImageInfo(
        val publicUrl: String,
        val path: String,
    )

    suspend fun uploadUserAvatar(uid: String, bytes: ByteArray, contentType: String = "image/jpeg"): ImageInfo

    suspend fun uploadProductMainImage(productId: String, bytes: ByteArray, contentType: String = "image/jpeg"): ImageInfo

    suspend fun deleteByPath(path: String)
}


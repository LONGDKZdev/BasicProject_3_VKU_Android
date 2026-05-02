package com.vohuy.mixueapp.data.repository.impl

import com.vohuy.mixueapp.BuildConfig
import com.vohuy.mixueapp.data.remote.supabase.SupabaseClientProvider
import com.vohuy.mixueapp.data.repository.ImageRepository
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

/**
 * Supabase implementation for [ImageRepository].
 *
 * Bucket is public, so we return a public URL for direct access.
 */
class SupabaseImageRepository : ImageRepository {

    private val bucket: String
        get() = BuildConfig.SUPABASE_BUCKET

    override suspend fun uploadUserAvatar(
        uid: String,
        bytes: ByteArray,
        contentType: String,
    ): ImageRepository.ImageInfo {
        val path = "users/$uid/avatar.jpg"
        return upload(bytes = bytes, path = path, contentType = contentType)
    }

    override suspend fun uploadProductMainImage(
        productId: String,
        bytes: ByteArray,
        contentType: String,
    ): ImageRepository.ImageInfo {
        val path = "products/$productId/main.jpg"
        return upload(bytes = bytes, path = path, contentType = contentType)
    }

    override suspend fun deleteByPath(path: String) {
        // Optional in Phase 1; implemented for completeness.
        val storage = SupabaseClientProvider.client.storage
        storage.from(bucket).delete(path)
    }

    private suspend fun upload(
        bytes: ByteArray,
        path: String,
        contentType: String,
    ): ImageRepository.ImageInfo {
        require(bucket.isNotBlank()) { "Missing SUPABASE_BUCKET in BuildConfig/local.properties" }

        val storage = SupabaseClientProvider.client.storage
        storage.from(bucket).upload(
            path = path,
            data = bytes,
            upsert = true,
        )

        // Public URL format:
        // https://{project}.supabase.co/storage/v1/object/public/{bucket}/{path}
        // Users may accidentally provide the REST endpoint (..../rest/v1/) instead of the base URL.
        val baseUrl = BuildConfig.SUPABASE_URL
            .trimEnd('/')
            .removeSuffix("/rest/v1")
            .removeSuffix("/rest/v1/")

        val publicUrl = "$baseUrl/storage/v1/object/public/$bucket/$path"

        return ImageRepository.ImageInfo(
            publicUrl = publicUrl,
            path = path,
        )
    }
}


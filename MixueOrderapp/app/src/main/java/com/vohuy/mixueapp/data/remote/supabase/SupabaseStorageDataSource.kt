package com.vohuy.mixueapp.data.remote.supabase

import com.vohuy.mixueapp.BuildConfig
import io.github.jan.supabase.storage.storage

/**
 * Low-level access to Supabase Storage.
 *
 * Higher-level code should use ImageRepository instead of calling this directly.
 */
class SupabaseStorageDataSource {
    private val client = SupabaseClientProvider.client

    private val bucket: String
        get() = BuildConfig.SUPABASE_BUCKET

    fun bucket() = client.storage.from(bucket)
}


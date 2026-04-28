package com.vohuy.mixueapp.utils

import com.vohuy.mixueapp.BuildConfig

/**
 * Small helper to validate Supabase config is present at runtime.
 * Useful during early integration.
 */
object SupabaseConfigValidator {
    fun validateOrThrow() {
        require(BuildConfig.SUPABASE_URL.isNotBlank()) { "SUPABASE_URL is missing" }
        require(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) { "SUPABASE_ANON_KEY is missing" }
        require(BuildConfig.SUPABASE_BUCKET.isNotBlank()) { "SUPABASE_BUCKET is missing" }
    }
}


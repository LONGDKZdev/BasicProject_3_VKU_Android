package com.vohuy.mixueapp.data.remote.supabase

import com.vohuy.mixueapp.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Creates a singleton Supabase client.
 *
 * NOTE:
 * - We only use Supabase for Storage (public bucket) in this project.
 * - URL/key are injected from `local.properties` via BuildConfig fields.
 */
object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        require(BuildConfig.SUPABASE_URL.isNotBlank()) {
            "Missing SUPABASE_URL. Please set it in local.properties"
        }
        require(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "Missing SUPABASE_ANON_KEY. Please set it in local.properties"
        }

        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            httpEngine = OkHttp.create()
            install(Storage)
        }
    }
}


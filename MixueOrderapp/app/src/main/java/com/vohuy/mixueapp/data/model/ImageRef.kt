package com.vohuy.mixueapp.data.model

/**
 * Stores a reference to an image in Supabase Storage.
 *
 * This complements the existing `imageUrl` string fields.
 * We will start using this in later phases to make delete/replace easier.
 */
data class ImageRef(
    val url: String = "",
    val path: String = "",
)


package com.vohuy.mixueapp.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.IOException

/**
 * Utilities for working with [Uri] returned by the system picker.
 */

@Throws(IOException::class)
fun Uri.readBytes(context: Context): ByteArray {
    val resolver: ContentResolver = context.contentResolver
    resolver.openInputStream(this)?.use { input ->
        return input.readBytes()
    }
    throw IOException("Unable to open InputStream for uri=$this")
}


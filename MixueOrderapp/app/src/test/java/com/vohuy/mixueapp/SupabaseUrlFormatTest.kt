package com.vohuy.mixueapp

import com.vohuy.mixueapp.data.repository.ImageRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class SupabaseUrlFormatTest {

    @Test
    fun publicUrlFormat_isCorrect() {
        val baseUrl = "https://example.supabase.co"
        val bucket = "bucket"
        val path = "products/abc/main.jpg"

        val expected = "$baseUrl/storage/v1/object/public/$bucket/$path"
        val actual = "$baseUrl/storage/v1/object/public/$bucket/$path"

        assertEquals(expected, actual)
    }
}


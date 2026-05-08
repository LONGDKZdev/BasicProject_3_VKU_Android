package com.vohuy.mixueapp.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.vohuy.mixueapp.data.model.Product

/**
 * FirestoreSampleDataSeeder
 *
 * Utility for DEV/DEBUG only: upsert a small set of sample documents into Firestore.
 *
 * Why:
 * - Firestore has no "tables"; collections only appear after a successful write.
 * - If your UI shows "Không tìm thấy dữ liệu", seeding helps confirm reads are working.
 *
 * Notes:
 * - This class only writes to the `products` collection used by the app.
 * - Keep rules open only temporarily when testing.
 */
object FirestoreSampleDataSeeder {
    private const val TAG = "FirestoreSeeder"

    private val sampleProducts: List<Product> = listOf(
        Product(
            id = "seed_kem_dau",
            name = "Kem dâu",
            description = "Dữ liệu mẫu (seed) từ app",
            price = 25000.0,
            imageUrl = "https://picsum.photos/seed/kemdau/600/400",
            imagePath = "",
            category = Constants.CATEGORY_ICE_CREAM,
            available = true,
        ),
        Product(
            id = "seed_tra_sua_duong_den",
            name = "Trà sữa đường đen",
            description = "Dữ liệu mẫu (seed) từ app",
            price = 30000.0,
            imageUrl = "https://picsum.photos/seed/trasua/600/400",
            imagePath = "",
            category = Constants.CATEGORY_TEA,
            available = true,
        ),
        Product(
            id = "seed_nuoc_cam",
            name = "Nước cam",
            description = "Dữ liệu mẫu (seed) từ app",
            price = 20000.0,
            imageUrl = "https://picsum.photos/seed/nuoccam/600/400",
            imagePath = "",
            category = Constants.CATEGORY_DRINK,
            available = true,
        ),
    )

    /**
     * Upsert products with fixed IDs so you can run multiple times without duplicating.
     */
    fun seedProducts(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        onDone: ((success: Boolean, message: String) -> Unit)? = null,
    ) {
        Log.d(TAG, "Seeding ${sampleProducts.size} products...")
        val batch = firestore.batch()
        sampleProducts.forEach { p ->
            val ref = firestore.collection(Constants.COLLECTION_PRODUCTS).document(p.id)
            batch.set(ref, p)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Seed products OK")
                onDone?.invoke(true, "Seed products OK")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Seed products FAILED: ${e.message}", e)
                onDone?.invoke(false, "Seed products FAILED: ${e.message}")
            }
    }

    /**
     * Quick read verification: logs how many docs are found.
     */
    fun verifyProductsReadable(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        onDone: ((success: Boolean, message: String) -> Unit)? = null,
    ) {
        firestore.collection(Constants.COLLECTION_PRODUCTS)
            .limit(10)
            .get()
            .addOnSuccessListener { snap ->
                Log.d(TAG, "✅ Read products OK. size=${snap.size()}")
                onDone?.invoke(true, "Read products OK. size=${snap.size()}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Read products FAILED: ${e.message}", e)
                onDone?.invoke(false, "Read products FAILED: ${e.message}")
            }
    }
}


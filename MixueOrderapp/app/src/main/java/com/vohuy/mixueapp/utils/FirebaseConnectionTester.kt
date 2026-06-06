package com.vohuy.mixueapp.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * FirebaseConnectionTester
 *
 * Chỉ kiểm tra Firebase Auth và Firestore.
 * Không kiểm tra Firebase Storage vì ảnh của dự án dùng Supabase Storage.
 */
object FirebaseConnectionTester {

    private const val TAG = "FirebaseConnectionTest"

    /**
     * Test tất cả Firebase services được phép dùng trong dự án.
     */
    fun testAllConnections() {
        Log.d(TAG, "========== TESTING FIREBASE CONNECTIONS ==========")
        testFirestoreConnection()
        testAuthConnection()
        Log.d(TAG, "========== TEST COMPLETED ==========")
    }

    /**
     * Test Firestore connection.
     */
    private fun testFirestoreConnection() {
        val firestore = FirebaseFirestore.getInstance()

        try {
            val docId = "test_${System.currentTimeMillis()}"
            val testDocRef = firestore.collection("test_connection").document(docId)

            testDocRef
                .set(
                    mapOf(
                        "timestamp" to System.currentTimeMillis(),
                        "status" to "testing",
                        "message" to "Firebase Firestore connection test"
                    )
                )
                .addOnSuccessListener {
                    Log.d(TAG, "FIRESTORE: Connection SUCCESS")

                    testDocRef.delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "FIRESTORE: Test document deleted")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "FIRESTORE: Failed to delete test document - ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "FIRESTORE: Connection FAILED - ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "FIRESTORE: Exception - ${e.message}", e)
        }
    }

    /**
     * Test Firebase Auth connection.
     */
    private fun testAuthConnection() {
        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            Log.d(TAG, "AUTH: Instance initialized")

            if (currentUser != null) {
                Log.d(TAG, "AUTH: Current user = ${currentUser.email}")
            } else {
                Log.d(TAG, "AUTH: No user logged in")
            }
        } catch (e: Exception) {
            Log.e(TAG, "AUTH: Exception - ${e.message}", e)
        }
    }

    /**
     * Get connection status summary.
     */
    fun getConnectionStatus(): String {
        val sb = StringBuilder()
        sb.append("\n=== FIREBASE CONNECTION STATUS ===\n")

        try {
            FirebaseFirestore.getInstance()
            sb.append("Firestore: Initialized\n")
        } catch (e: Exception) {
            sb.append("Firestore: Failed - ${e.message}\n")
        }

        try {
            FirebaseAuth.getInstance()
            sb.append("Auth: Initialized\n")
        } catch (e: Exception) {
            sb.append("Auth: Failed - ${e.message}\n")
        }

        sb.append("Firebase Storage: Not used. Images use Supabase Storage.\n")
        sb.append("===================================\n")
        return sb.toString()
    }

    /**
     * Test specific Firestore collection.
     */
    fun testCollectionAccess(collectionName: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(collectionName)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "Collection '$collectionName': Readable (${snapshot.size()} documents)")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Collection '$collectionName': Not accessible - ${e.message}", e)
            }
    }

    /**
     * Test user authentication.
     */
    fun testUserAuthentication(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()

        Log.d(TAG, "Testing authentication with: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "AUTH: Login successful - ${authResult.user?.email}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "AUTH: Login failed - ${e.message}", e)
            }
    }
}
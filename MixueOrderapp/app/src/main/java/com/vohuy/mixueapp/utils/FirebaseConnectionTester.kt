package com.vohuy.mixueapp.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * FirebaseConnectionTester - Kiểm tra kết nối Firebase
 * Sử dụng để test tất cả Firebase services
 */
object FirebaseConnectionTester {

    private const val TAG = "FirebaseConnectionTest"

    /**
     * Test tất cả Firebase connections
     */
    fun testAllConnections() {
        Log.d(TAG, "========== TESTING FIREBASE CONNECTIONS ==========")
        testFirestoreConnection()
        testAuthConnection()
        testStorageConnection()
        Log.d(TAG, "========== TEST COMPLETED ==========")
    }

    /**
     * Test Firestore connection
     */
    private fun testFirestoreConnection() {
        val firestore = FirebaseFirestore.getInstance()
        
        try {
            firestore.collection("test_connection")
                .document("test_${System.currentTimeMillis()}")
                .set(mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "status" to "testing",
                    "message" to "Firebase connection test"
                ))
                .addOnSuccessListener {
                    Log.d(TAG, "✅ FIRESTORE: Connection SUCCESS")
                    // Xóa document test
                    firestore.collection("test_connection")
                        .document("test_${System.currentTimeMillis()}")
                        .delete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ FIRESTORE: Connection FAILED - ${e.message}")
                    Log.e(TAG, "Exception: ", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "❌ FIRESTORE: Exception - ${e.message}", e)
        }
    }

    /**
     * Test Firebase Auth connection
     */
    private fun testAuthConnection() {
        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (auth != null) {
                Log.d(TAG, "✅ AUTH: Instance initialized")
                if (currentUser != null) {
                    Log.d(TAG, "👤 AUTH: Current user = ${currentUser.email}")
                } else {
                    Log.d(TAG, "👤 AUTH: No user logged in (OK for testing)")
                }
            } else {
                Log.e(TAG, "❌ AUTH: Instance is NULL")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ AUTH: Exception - ${e.message}", e)
        }
    }

    /**
     * Test Firebase Storage connection
     */
    private fun testStorageConnection() {
        try {
            val storage = FirebaseStorage.getInstance()
            
            if (storage != null) {
                Log.d(TAG, "✅ STORAGE: Instance initialized")
                
                // Test write access
                val testRef = storage.reference.child("test_connection/test_${System.currentTimeMillis()}.txt")
                testRef.putBytes("Firebase Storage Test".toByteArray())
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ STORAGE: Upload test SUCCESS")
                        // Delete test file
                        testRef.delete()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ STORAGE: Upload test FAILED - ${e.message}")
                    }
            } else {
                Log.e(TAG, "❌ STORAGE: Instance is NULL")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ STORAGE: Exception - ${e.message}", e)
        }
    }

    /**
     * Get connection status summary
     */
    fun getConnectionStatus(): String {
        val sb = StringBuilder()
        sb.append("\n=== FIREBASE CONNECTION STATUS ===\n")
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            sb.append("✅ Firestore: Initialized\n")
        } catch (e: Exception) {
            sb.append("❌ Firestore: Failed - ${e.message}\n")
        }
        
        try {
            val auth = FirebaseAuth.getInstance()
            sb.append("✅ Auth: Initialized\n")
        } catch (e: Exception) {
            sb.append("❌ Auth: Failed - ${e.message}\n")
        }
        
        try {
            val storage = FirebaseStorage.getInstance()
            sb.append("✅ Storage: Initialized\n")
        } catch (e: Exception) {
            sb.append("❌ Storage: Failed - ${e.message}\n")
        }
        
        sb.append("===================================\n")
        return sb.toString()
    }

    /**
     * Test specific Firestore collection
     */
    fun testCollectionAccess(collectionName: String) {
        val firestore = FirebaseFirestore.getInstance()
        
        firestore.collection(collectionName)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "✅ Collection '$collectionName': Readable (${snapshot.size()} documents)")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Collection '$collectionName': Not accessible - ${e.message}")
            }
    }

    /**
     * Test user authentication
     */
    fun testUserAuthentication(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        
        Log.d(TAG, "Testing authentication with: $email")
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "✅ AUTH: Login successful - ${authResult.user?.email}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ AUTH: Login failed - ${e.message}")
            }
    }
}


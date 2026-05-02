package com.vohuy.mixueapp.utils

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * FirebaseHealthCheck
 *
 * Minimal, production-safe checks to confirm Firebase is configured correctly.
 *
 * Notes:
 * - This project uses Supabase for images, so we do NOT test Firebase Storage here.
 * - Firestore check writes a tiny doc under a dedicated collection and then deletes it.
 */
object FirebaseHealthCheck {
    private const val TAG = "FirebaseHealthCheck"

    /**
     * Call this once from debug builds / manual testing.
     */
    fun runAll() {
        logFirebaseApps()
        testAuthInstance()
        testFirestoreReadWrite()
    }

    private fun logFirebaseApps() {
        try {
            val apps = FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext)
            Log.d(TAG, "Firebase apps initialized: ${apps.map { it.name }}")
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseApp not initialized: ${e.message}", e)
        }
    }

    private fun testAuthInstance() {
        try {
            val auth = FirebaseAuth.getInstance()
            Log.d(TAG, "✅ Auth initialized. currentUser=${auth.currentUser?.uid ?: "(none)"}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Auth init failed: ${e.message}", e)
        }
    }

    private fun testFirestoreReadWrite() {
        val firestore = FirebaseFirestore.getInstance()
        val docId = "android_${System.currentTimeMillis()}"
        val docRef = firestore.collection("healthcheck").document(docId)

        docRef.set(
            mapOf(
                "timestamp" to System.currentTimeMillis(),
                "platform" to "android",
                "status" to "ok",
            )
        )
            .addOnSuccessListener {
                Log.d(TAG, "✅ Firestore write OK")
                docRef.get()
                    .addOnSuccessListener { snap ->
                        Log.d(TAG, "✅ Firestore read OK. exists=${snap.exists()}")
                        docRef.delete().addOnSuccessListener {
                            Log.d(TAG, "✅ Firestore delete OK")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Firestore read failed: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Firestore write failed: ${e.message}", e)
            }
    }
}


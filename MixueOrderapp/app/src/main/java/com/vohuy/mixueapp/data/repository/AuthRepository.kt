package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.User
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

/**
 * AuthRepository - Xử lý tất cả logic xác thực Firebase
 * DRY Principle: Tất cả Firebase Auth operations ở đây
 * Không code Firebase trực tiếp trong Activity/Fragment
 */
class AuthRepository : BaseRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Đăng ký người dùng mới
     */
    fun registerUser(email: String, password: String, fullName: String): LiveData<Result<User>> {
        val result = MutableLiveData<Result<User>>()
        result.value = Result.Loading()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: ""
                val user = User(
                    id = userId,
                    email = email,
                    fullName = fullName,
                    role = Constants.ROLE_USER,
                    createdAt = System.currentTimeMillis()
                )

                // ✅ No email verification - just save user to Firestore
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        result.value = Result.Success(user)
                        // DO NOT sign out - user can login immediately
                    }
                    .addOnFailureListener { exception ->
                        result.value = Result.Error(exception as Exception)
                    }
            }
            .addOnFailureListener { exception ->
                val errorMessage = if (exception is FirebaseAuthException) {
                    ErrorHandler.getAuthErrorMessage(exception.errorCode)
                } else {
                    ErrorHandler.getErrorMessage(exception as Exception)
                }
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Đăng nhập người dùng
     */
    fun loginUser(email: String, password: String): LiveData<Result<User>> {
        val result = MutableLiveData<Result<User>>()
        result.value = Result.Loading()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // ✅ No email verification check - user can login immediately
                val userId = authResult.user?.uid ?: ""
                fetchUserData(userId) { user ->
                    if (user != null) {
                        result.value = Result.Success(user)
                    } else {
                        result.value = Result.Error(Exception("Không thể lấy thông tin người dùng"))
                    }
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = if (exception is FirebaseAuthException) {
                    ErrorHandler.getAuthErrorMessage(exception.errorCode)
                } else {
                    ErrorHandler.getErrorMessage(exception as Exception)
                }
                result.value = Result.Error(Exception(errorMessage))
            }

        return result
    }

    /**
     * Lấy thông tin user hiện tại
     */
    fun getCurrentUser(): LiveData<User?> {
        val result = MutableLiveData<User?>()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            fetchUserData(userId) { user ->
                // Fallback: if Firestore user doc doesn't exist yet, still return minimal user.
                result.value = user ?: User(
                    id = userId,
                    email = auth.currentUser?.email ?: "",
                    fullName = "",
                    role = Constants.ROLE_USER,
                    createdAt = System.currentTimeMillis(),
                )
            }
        } else {
            result.value = null
        }
        return result
    }

    /**
     * Đăng xuất
     */
    fun logoutUser(): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        try {
            auth.signOut()
            result.value = Result.Success(Unit)
        } catch (exception: Exception) {
            result.value = Result.Error(exception)
        }
        return result
    }

    /**
     * Lấy dữ liệu user từ Firestore
     */
    private fun fetchUserData(userId: String, callback: (User?) -> Unit) {
        firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun changePassword(oldPass: String, newPass: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, oldPass)
            user.reauthenticate(credential).addOnSuccessListener {
                user.updatePassword(newPass).addOnSuccessListener {
                    result.value = Result.Success(Unit)
                }.addOnFailureListener { e -> result.value = Result.Error(e) }
            }.addOnFailureListener { e ->
                result.value = Result.Error(Exception("Mật khẩu hiện tại không đúng"))
            }
        } else {
            result.value = Result.Error(Exception("Chưa đăng nhập"))
        }
        return result
    }

    /**
     * Gửi email khôi phục mật khẩu
     */
    fun resetPassword(email: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        result.value = Result.Loading()

        // Sử dụng biến auth đã được khởi tạo sẵn ở BaseRepository
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                result.value = Result.Success(Unit)
            }
            .addOnFailureListener { e ->
                result.value = Result.Error(e)
            }
        return result
    }
}


package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

                authResult.user?.sendEmailVerification()

                // Lưu thông tin user vào Firestore
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        result.value = Result.Success(user)
                        auth.signOut()
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
                if (authResult.user?.isEmailVerified == true) {
                    val userId = authResult.user?.uid ?: ""
                    fetchUserData(userId) { user ->
                        if (user != null) {
                            result.value = Result.Success(user)
                        } else {
                            result.value = Result.Error(Exception("Không thể lấy thông tin người dùng"))
                        }
                    }
                } else {
                    auth.signOut() // Chưa verify thì đá ra ngoài
                    result.value = Result.Error(Exception("Vui lòng kiểm tra hộp thư và xác minh Email trước khi đăng nhập!"))
                }
            }
            .addOnFailureListener { exception ->
                // ... (Giữ nguyên phần xử lý lỗi của bạn)
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
}


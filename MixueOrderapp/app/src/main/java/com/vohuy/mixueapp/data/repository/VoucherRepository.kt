package com.vohuy.mixueapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vohuy.mixueapp.base.BaseRepository
import com.vohuy.mixueapp.data.model.Voucher
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.ErrorHandler
import com.vohuy.mixueapp.utils.Result

class VoucherRepository : BaseRepository() {

    fun findVoucherByCode(code: String): LiveData<Result<Voucher?>> {
        val result = MutableLiveData<Result<Voucher?>>()
        result.value = Result.Loading()

        val normalizedCode = code.trim().uppercase()
        if (normalizedCode.isBlank()) {
            result.value = Result.Success(null)
            return result
        }

        firestore.collection(Constants.COLLECTION_VOUCHERS)
            .whereEqualTo(Constants.FIELD_VOUCHER_CODE, normalizedCode)
            .whereEqualTo(Constants.FIELD_VOUCHER_ACTIVE, true)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val voucher = snapshot.documents.firstOrNull()?.toObject(Voucher::class.java)
                result.value = Result.Success(voucher)
            }
            .addOnFailureListener { exception ->
                result.value = Result.Error(Exception(ErrorHandler.getErrorMessage(exception as Exception)))
            }

        return result
    }
}


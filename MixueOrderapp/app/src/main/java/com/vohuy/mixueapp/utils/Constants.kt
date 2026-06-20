package com.vohuy.mixueapp.utils

/**
 * Constants - Chứa tất cả hằng số dùng chung trong ứng dụng.
 *
 * Dữ liệu nghiệp vụ lưu trong Firebase Firestore.
 * Ảnh lưu trong Supabase Storage và Firestore chỉ lưu imageUrl.
 */
object Constants {

    // ========== FIRESTORE COLLECTIONS ==========
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_TRANSACTIONS = "transactions"
    const val COLLECTION_PRODUCT_REVIEWS = "productReviews"
    const val COLLECTION_VOUCHERS = "vouchers"

    // ========== USER FIELDS ==========
    const val FIELD_USER_ID = "id"
    const val FIELD_USER_EMAIL = "email"
    const val FIELD_USER_FULL_NAME = "fullName"
    const val FIELD_USER_PHONE = "phoneNumber"
    const val FIELD_USER_ROLE = "role"
    const val FIELD_USER_CREATED_AT = "createdAt"

    // ========== PRODUCT FIELDS ==========
    const val FIELD_PRODUCT_ID = "id"
    const val FIELD_PRODUCT_NAME = "name"
    const val FIELD_PRODUCT_DESCRIPTION = "description"
    const val FIELD_PRODUCT_PRICE = "price"
    const val FIELD_PRODUCT_IMAGE_URL = "imageUrl"
    const val FIELD_PRODUCT_CATEGORY = "category"
    const val FIELD_PRODUCT_AVAILABLE = "available"
    const val FIELD_PRODUCT_RATING_AVERAGE = "ratingAverage"
    const val FIELD_PRODUCT_RATING_COUNT = "ratingCount"
    const val FIELD_PRODUCT_RATING_SUM = "ratingSum"
    const val FIELD_PRODUCT_RATING_UPDATED_AT = "ratingUpdatedAt"
    const val FIELD_PRODUCT_CREATED_AT = "createdAt"
    const val FIELD_PRODUCT_UPDATED_AT = "updatedAt"

    // ========== PRODUCT REVIEW FIELDS ==========
    const val FIELD_REVIEW_PRODUCT_ID = "productId"
    const val FIELD_REVIEW_USER_ID = "userId"
    const val FIELD_REVIEW_RATING = "rating"
    const val FIELD_REVIEW_UPDATED_AT = "updatedAt"

    // ========== VOUCHER FIELDS ==========
    const val FIELD_VOUCHER_CODE = "code"
    const val FIELD_VOUCHER_ACTIVE = "active"

    // ========== ORDER FIELDS ==========
    const val FIELD_ORDER_ID = "id"
    const val FIELD_ORDER_USER_ID = "userId"
    const val FIELD_ORDER_CUSTOMER_NAME = "customerName"
    const val FIELD_ORDER_ITEMS = "items"
    const val FIELD_ORDER_TOTAL_PRICE = "totalPrice"
    const val FIELD_ORDER_STATUS = "status"
    const val FIELD_ORDER_CREATED_AT = "createdAt"
    const val FIELD_ORDER_UPDATED_AT = "updatedAt"

    // ========== TRANSACTION FIELDS ==========
    const val FIELD_TRANSACTION_ID = "id"
    const val FIELD_TRANSACTION_USER_ID = "userId"
    const val FIELD_TRANSACTION_CUSTOMER_NAME = "customerName"
    const val FIELD_TRANSACTION_ORDER_ID = "orderId"
    const val FIELD_TRANSACTION_AMOUNT = "amount"
    const val FIELD_TRANSACTION_PAYMENT_METHOD = "paymentMethod"
    const val FIELD_TRANSACTION_STATUS = "status"
    const val FIELD_TRANSACTION_DESCRIPTION = "description"
    const val FIELD_TRANSACTION_CREATED_AT = "createdAt"

    // ========== USER ROLES ==========
    const val ROLE_USER = "USER"
    const val ROLE_ADMIN = "ADMIN"

    // ========== ORDER STATUS ==========
    const val ORDER_STATUS_PENDING = "PENDING"
    const val ORDER_STATUS_CONFIRMED = "CONFIRMED"
    const val ORDER_STATUS_DELIVERING = "DELIVERING"
    const val ORDER_STATUS_DONE = "DONE"
    const val ORDER_STATUS_CANCELLED = "CANCELLED"

    val VALID_ORDER_STATUSES = setOf(
        ORDER_STATUS_PENDING,
        ORDER_STATUS_CONFIRMED,
        ORDER_STATUS_DELIVERING,
        ORDER_STATUS_CANCELLED
    )

    // ========== PAYMENT METHOD ==========
    const val PAYMENT_METHOD_CASH = "CASH"
    const val PAYMENT_METHOD_BANK_TRANSFER = "BANK_TRANSFER"

    val VALID_PAYMENT_METHODS = setOf(
        PAYMENT_METHOD_CASH,
        PAYMENT_METHOD_BANK_TRANSFER
    )

    // ========== TRANSACTION STATUS ==========
    const val TRANSACTION_STATUS_PENDING = "PENDING"
    const val TRANSACTION_STATUS_SUCCESS = "SUCCESS"
    const val TRANSACTION_STATUS_FAILED = "FAILED"

    val VALID_TRANSACTION_STATUSES = setOf(
        TRANSACTION_STATUS_PENDING,
        TRANSACTION_STATUS_SUCCESS,
        TRANSACTION_STATUS_FAILED
    )

    // ========== PRODUCT CATEGORIES ==========
    const val CATEGORY_ICE_CREAM = "Kem"
    const val CATEGORY_TEA = "Trà sữa"
    const val CATEGORY_FRUIT_TEA = "Trà trái cây"
    const val CATEGORY_COFFEE = "Cà phê"
    const val CATEGORY_DRINK = "Nước"
    const val CATEGORY_OTHER = "Khác"

    val PRODUCT_CATEGORIES = listOf(
        CATEGORY_ICE_CREAM,
        CATEGORY_TEA,
        CATEGORY_FRUIT_TEA,
        CATEGORY_COFFEE,
        CATEGORY_DRINK,
        CATEGORY_OTHER
    )

    // ========== VALIDATION ==========
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_PHONE_LENGTH = 10
}

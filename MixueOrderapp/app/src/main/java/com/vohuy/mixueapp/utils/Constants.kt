package com.vohuy.mixueapp.utils

/**
 * Constants - Chứa tất cả hằng số của ứng dụng
 * DRY Principle: Khi cần thay đổi, chỉnh sửa ở một nơi
 */
object Constants {

    // ========== FIREBASE COLLECTIONS ==========
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"

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
    const val FIELD_PRODUCT_IMAGE_PATH = "imagePath"
    const val FIELD_PRODUCT_CATEGORY = "category"
    const val FIELD_PRODUCT_AVAILABLE = "available"

    // ========== ORDER FIELDS ==========
    const val FIELD_ORDER_ID = "id"
    const val FIELD_ORDER_USER_ID = "userId"
    const val FIELD_ORDER_ITEMS = "items"
    const val FIELD_ORDER_TOTAL_PRICE = "totalPrice"
    const val FIELD_ORDER_STATUS = "status"
    const val FIELD_ORDER_CREATED_AT = "createdAt"

    // ========== USER ROLES ==========
    const val ROLE_USER = "USER"
    const val ROLE_ADMIN = "ADMIN"

    // ========== ORDER STATUS ==========
    const val ORDER_STATUS_PENDING = "PENDING"
    const val ORDER_STATUS_CONFIRMED = "CONFIRMED"
    const val ORDER_STATUS_DELIVERING = "DELIVERING"
    const val ORDER_STATUS_DONE = "DONE"
    const val ORDER_STATUS_CANCELLED = "CANCELLED"

    // ========== PRODUCT CATEGORIES ==========
    const val CATEGORY_ICE_CREAM = "Kem"
    const val CATEGORY_TEA = "Trà Sữa"
    const val CATEGORY_DRINK = "Nước"

    // ========== VALIDATION ==========
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_PHONE_LENGTH = 10
}


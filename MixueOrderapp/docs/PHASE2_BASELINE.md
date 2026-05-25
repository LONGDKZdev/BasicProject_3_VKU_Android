# Phase 2 baseline (Supabase image wiring)

This file marks the **Phase 2 checkpoint** used as a stable reference point to avoid code conflicts.

## Scope completed in Phase 2

### Supabase Storage (public bucket)
- Bucket: `StorageImage_MixueAndroid` (public)
- Upload paths:
  - Product main image: `products/{productId}/main.jpg`
  - User avatar: `users/{uid}/avatar.jpg`
- Public URL generation:
  - Always returns: `https://<project>.supabase.co/storage/v1/object/public/<bucket>/<path>`
  - If config mistakenly uses REST URL (`.../rest/v1/`), it is automatically trimmed.

### Firestore wiring for product images
- Product now stores both:
  - `imageUrl` (public URL)
  - `imagePath` (Supabase object path)

## Files touched / added (Phase 2)
- `app/src/main/java/com/vohuy/mixueapp/data/repository/impl/SupabaseImageRepository.kt`
- `app/src/main/java/com/vohuy/mixueapp/data/model/Product.kt` (+ `imagePath`)
- `app/src/main/java/com/vohuy/mixueapp/utils/Constants.kt` (+ `FIELD_PRODUCT_IMAGE_PATH`)
- `app/src/main/java/com/vohuy/mixueapp/data/repository/ProductRepository.kt`
  - `createProductWithImage(...)`
  - `updateProductImage(...)`

## Not done yet (starts after this checkpoint)
- UI flow pick-image (gallery) → upload → create/update product
- Replace `observeForever` patterns (Phase 3)
- Remove Firebase Storage dependency + testers (Phase 4)
- Add user avatar upload wiring

## Ground rules from this point
1. **All images come from Supabase** (no Firebase Storage for app images).
2. Firestore stores image fields; UI reads only `imageUrl` for display.
3. Any refactor after this should keep API compatibility or be done in one PR.


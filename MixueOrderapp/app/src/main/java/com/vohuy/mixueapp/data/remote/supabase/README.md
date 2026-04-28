# Supabase module (planned)

This folder will contain Supabase client initialization and storage access.

## Requirements
- Public bucket
- 100% images stored in Supabase Storage

## Planned files
- `SupabaseClientProvider.kt`: creates a singleton Supabase client from `BuildConfig` values.
- `SupabaseStorageDataSource.kt`: low-level storage operations (upload/delete).

## Image path conventions
- Product main image: `products/{productId}/main.jpg`
- User avatar: `users/{uid}/avatar.jpg`

Firestore will store:
- `imageUrl` (required)
- `imagePath` (recommended)


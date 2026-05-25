# MixueOrderapp – Architecture (Draft)

## Goals
- **UI:** 100% Jetpack Compose + Navigation Compose.
- **Data:** Firebase is the source of truth for all business data.
  - Firebase Auth: identity (UID).
  - Firestore: users / products / orders.
- **Images:** 100% stored on **Supabase Storage** (public access). Firestore stores the image URLs.

## Current status (as of 2026-04-28)
### Already implemented
- Compose-first UI: `MainActivity` uses `setContent { NavGraph() }`.
- Navigation Compose: `ui/navigation/NavGraph.kt`.
- Firebase: `google-services.json`, `MixueApp` initializes Firebase, repositories for Auth/Product/Order.

### Not yet implemented
- Supabase client + storage upload/download/delete.
- A unified image layer (upload -> get public URL -> store in Firestore).
- Lifecycle-safe ViewModel state (currently uses `observeForever` in several ViewModels).

## Target package structure (recommended)
```
com.vohuy.mixueapp
  core/
    constants/
    result/
    error/
    extensions/
    config/
  data/
    model/
    repository/           (interfaces)
    repository/impl/
      firebase/
      storage/
    remote/
      firebase/
      supabase/
  domain/
    usecase/
  ui/
    navigation/
    screens/
    components/
    theme/
```

## Image storage contract (Supabase public bucket)
### Object paths
- Product image (single main image):
  - `products/{productId}/main.jpg`
- Product multiple images (optional):
  - `products/{productId}/{timestamp}.jpg`
- User avatar:
  - `users/{uid}/avatar.jpg`

### Firestore fields
- `imageUrl`: public URL from Supabase.
- `imagePath` (recommended): the exact object path in Supabase, used for replace/delete.

## Roadmap
### Phase 0 – Structure & docs (now)
- Add architecture documentation.
- Add Supabase module skeleton packages/files.

### Phase 1 – Supabase storage core
- Add Supabase dependencies.
- Implement Supabase client provider.
- Implement `ImageRepository` (upload/delete) that returns `{url, path}`.

### Phase 2 – Wiring into Firebase
- When creating/updating products/users: upload image to Supabase, write returned URL/path to Firestore.

### Phase 3 – MVVM state hardening
- Replace `observeForever` with `StateFlow`/`Flow` or lifecycle-aware observation.

### Phase 4 – Cleanup
- Remove Firebase Storage usage if not needed.
- Deprecate legacy ViewBinding-based base UI classes if unused.


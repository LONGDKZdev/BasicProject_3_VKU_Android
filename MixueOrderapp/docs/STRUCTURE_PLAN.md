# Structure Plan (approved direction)

This document is the actionable checklist for restructuring without changing business logic yet.

## What already exists
- Compose-first UI (MainActivity + NavGraph + screens)
- Firebase Auth + Firestore repositories
- Models: Product, Order, User, ...
- Utilities: Result, ErrorHandler, Constants

## Gaps
- Supabase Storage (public) not integrated
- Image management not standardized
- Some legacy UI base classes (ViewBinding + ProgressDialog)
- ViewModels use `observeForever`

## Phase 0 (now): Structure only
1. Add docs: `ARCHITECTURE.md`, this plan.
2. Add repository interfaces for future refactor:
   - `data/repository/ImageRepository.kt`
3. Add Supabase module skeleton folder (no runtime usage yet).

## Phase 1: Supabase Storage core
- Add dependencies
- Add BuildConfig keys (read from `local.properties`)
- Implement `ImageRepository` with Supabase

## Phase 2: Wiring
- Upload images to Supabase, store URL/path in Firestore

## Phase 3: MVVM hardening
- Replace observeForever with StateFlow or lifecycle-aware observation

## Phase 4: Cleanup & consistency
- Remove Firebase Storage dependency if unused
- Deprecate ViewBinding BaseActivity for Compose
- Manifest/theme alignment


# PROJECT SNAPSHOT — MixueOrderapp

> Mục tiêu của file này: **"snapshot" đầy đủ trạng thái dự án** (Android + web admin) để nếu mất đoạn chat, chỉ cần gửi lại file này là nắm được ngay dự án đang có gì, cấu trúc ra sao, chạy như thế nào, dùng thư viện nào, file nào chứa cấu hình/keys.

- Workspace root: `D:\StorageCode_Android\CodeTest\Basic_Project_3\MixueOrderapp`
- Snapshot created: **2026-05-06**

---

## 0) Tóm tắt nhanh

### 0.1. Thành phần hệ thống
- **Android app** (người dùng): đặt hàng/đọc dữ liệu.
- **Web admin**: hiện tại là **UI-only skeleton** (CRUD giả lập bằng mock/in-memory). Nếu muốn CRUD thật trên web, thay implementation trong `web-admin/src/services/`.
- **Backend dữ liệu (đã tích hợp trong Android app)**:
  - **Firebase**: Auth + Firestore + Storage.
  - **Supabase**: **Storage only** để upload ảnh và lấy URL public.

### 0.2. Trạng thái chạy hiện tại
- Web admin chạy được bằng HTTP server nội bộ (Python/PHP).
- Android app cấu hình Compose + Navigation, có các ViewModel/Repository và tiện ích Firebase healthcheck + seeder.

---

## 1) Cách chạy nhanh (Windows)

### 1.1. Chạy Web Admin (UI-only)
Chạy file:
- `web-admin\run-web-admin.bat`

File này sẽ:
1) Serve thư mục `web-admin/` qua `python -m http.server 5173` (nếu có Python), fallback sang `php -S localhost:5173` (nếu có PHP)
2) Tự mở browser tới: `http://localhost:5173/`

### 1.2. Chạy Android app
Mở project root `MixueOrderapp/` bằng Android Studio, run module `app`.

Kiểm tra build nhanh (không cần mở Android Studio):

```powershell
Set-Location "D:\StorageCode_Android\CodeTest\Basic_Project_3\MixueOrderapp"
./gradlew.bat :app:assembleDebug
```

---

## 2) Cấu trúc thư mục (high-level)

### 2.1. Root
Các file đáng chú ý:
- `build.gradle.kts` (Top-level plugins)
- `settings.gradle.kts` (repositories + include `:app`)
- `gradle/libs.versions.toml` (version catalog)
- `local.properties` (**chứa Supabase keys — không nên commit**)
- `database.sql` (hiện 0B)
- `firebase/` (rules)
- `web-admin/` (admin UI)

Nguồn danh sách file đầy đủ:
- `Danh_sach_file_auto.txt` (1507 dòng; auto-generated từ `scan_files.py`)

### 2.2. Android module: `app/`
- `app/build.gradle.kts`
- `app/google-services.json` (**Firebase config**)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/vohuy/mixueapp/` (Kotlin source)

### 2.3. Web admin: `web-admin/`
- `web-admin/index.html`
- `web-admin/assets/styles.css`
- `web-admin/src/main.js`
- `web-admin/src/services/*` (điểm tích hợp backend sau này)

---

## 3) Web Admin (UI-only skeleton)

### 3.1. Entry points
- HTML: `web-admin/index.html`
- JS entry: `web-admin/src/main.js`
- CSS: `web-admin/assets/styles.css`

### 3.2. Cách hoạt động
- Login hiện tại là **giả lập** (không gọi Firebase thật).
- Products/Orders CRUD hiện tại là **mock/in-memory**.
- Nút healthcheck/seed trong UI: **bị disable** và chỉ log `[UI-ONLY]`.

### 3.3. Nơi thay để tích hợp Firebase thật (sau này)
Theo `web-admin/src/services/README_INTEGRATION.md`:
- `web-admin/src/services/authService.js`
- `web-admin/src/services/productsService.js`
- `web-admin/src/services/ordersService.js`

Chỉ cần giữ nguyên signature để `web-admin/src/main.js` không phải đổi.

### 3.4. Chạy web admin
- Khuyến nghị chạy bằng `web-admin/run-web-admin.bat`.
- Port mặc định: `5173`.

---

## 4) Android App

### 4.1. AndroidManifest
File: `app/src/main/AndroidManifest.xml`
- Có permission: `android.permission.INTERNET`
- Application: `.MixueApp`
- Launcher activity: `.MainActivity`

### 4.2. Main activity
File: `app/src/main/java/com/vohuy/mixueapp/MainActivity.kt`
- Compose `setContent { NavGraph() }`
- Debug-only:
  - `FirebaseHealthCheck.runAll()`
  - `FirestoreSampleDataSeeder.seedProducts()`
  - `FirestoreSampleDataSeeder.verifyProductsReadable()`

### 4.3. Navigation
File: `app/src/main/java/com/vohuy/mixueapp/ui/navigation/NavGraph.kt`
- startDestination phụ thuộc `AuthViewModel.repositoryIsUserLoggedIn()`
- Routes có: Login, Register, Home, Cart, OrderHistory, ProductDetail, AdminAddProduct

---

## 5) Dependencies / Libraries (Android)

### 5.1. Gradle plugins (root)
File: `build.gradle.kts`
- `com.android.application` version `8.11.2`
- `org.jetbrains.kotlin.android` version `2.0.21`
- `com.google.gms.google-services` version `4.4.1`

### 5.2. Version catalog
File: `gradle/libs.versions.toml`
Một số versions chính:
- AGP: `8.11.2`
- Kotlin: `2.0.21`
- Material: `1.13.0`
- AppCompat: `1.7.1`

### 5.3. app/build.gradle.kts — các dependency đáng chú ý
File: `app/build.gradle.kts`

**Compose**
- Compose BOM: `androidx.compose:compose-bom:2024.10.01`
- Material3, UI tooling, navigation-compose `2.7.7`

**Firebase (Android)**
- Firebase BoM: `com.google.firebase:firebase-bom:33.1.0`
- `firebase-auth-ktx`
- `firebase-firestore-ktx`
- `firebase-storage-ktx`

**Supabase (Storage only)
- `io.github.jan-tennert.supabase:supabase-kt:2.6.1`
- `io.github.jan-tennert.supabase:storage-kt:2.6.1`
- `io.ktor:ktor-client-okhttp:2.3.12`

**Coroutines**
- `kotlinx-coroutines-android:1.8.0`
- `kotlinx-coroutines-core:1.8.0`

---

## 6) Keys / Config / Sensitive files (QUAN TRỌNG)

### 6.1. Firebase
- Android config: `app/google-services.json`
  - File này thường chứa `project_id`, `mobilesdk_app_id`, `api_key.current_key`, ...

**Giá trị hiện có trong dự án (từ `app/google-services.json`)**
- `project_id`: `mixue-order-app`
- `project_number`: `1025553077448`
- `package_name`: `com.vohuy.mixueapp`
- `api_key.current_key` (Web API key / dùng cho REST Identity Toolkit): `AIzaSyDJMGB9J3gkquwByz03IqzXODmnc3ulKWY`

**Lưu ý bảo mật**
- `api_key` (dạng `AIzaSy...`) là khóa client-side; có thể xuất hiện trong repo mobile/web.
- Tuy nhiên các khóa dạng server/service-role (Supabase service key, Firebase service account) **không được commit**.

### 6.2. Supabase keys
Theo `SUPABASE_SETUP.md`:
- Keys đặt trong `local.properties` (**NOT committed**):
  - `SUPABASE_URL=...`
  - `SUPABASE_ANON_KEY=...`
  - `SUPABASE_BUCKET=StorageImage_MixueAndroid`

**Lưu ý trạng thái hiện tại trong workspace của bạn**
- `local.properties` đang có đủ 3 biến Supabase; Gradle export ra `BuildConfig` qua `app/build.gradle.kts`.

Chúng được đưa vào `BuildConfig` qua `buildConfigField` trong `app/build.gradle.kts`:
- `BuildConfig.SUPABASE_URL`
- `BuildConfig.SUPABASE_ANON_KEY`
- `BuildConfig.SUPABASE_BUCKET`

### 6.3. Firebase rules
- Firestore rules: `firebase/firestore.rules`
- Storage rules: `firebase/storage.rules`

---

## 7) Danh sách file đầy đủ (không bỏ sót)

### 7.1. File nguồn
- `Danh_sach_file_auto.txt` (**1507 lines**) — danh sách đầy đủ file trong workspace tại thời điểm snapshot.

### 7.2. Cách tái tạo danh sách file (Windows)
Chạy từ root project:

```powershell
Set-Location "D:\StorageCode_Android\CodeTest\Basic_Project_3\MixueOrderapp"
cmd /c "echo.| python .\scan_files.py"
Copy-Item .\Danh_sach_file.txt .\Danh_sach_file_auto.txt -Force
```

Nếu không có Python, dùng PowerShell để export danh sách file (thay thế):

```powershell
Set-Location "D:\StorageCode_Android\CodeTest\Basic_Project_3\MixueOrderapp"
Get-ChildItem -Recurse -File | Select-Object FullName, Length | Out-File -Encoding utf8 .\Danh_sach_file_powershell.txt
```

---

## 8) Ghi chú / TODO (để không bị lạc)

- Web admin hiện **UI-only**; logic Firebase/Supabase hoạt động **đang nằm ở Android app**.
- Android hiện có healthcheck + seeder debug đang chạy trong `MainActivity` khi `BuildConfig.DEBUG = true`.

---

## 10) E2E: luồng "tạo sản phẩm + upload ảnh + lưu Firestore" (ĐÃ CÓ CODE)

Luồng này đã được implement trong Android:

1) UI: `app/src/main/java/com/vohuy/mixueapp/ui/screens/admin/AdminAddProductScreen.kt`
   - Chọn ảnh từ máy (`ActivityResultContracts.GetContent()`)
   - Gọi `ProductViewModel.createProductWithImage(...)`

2) ViewModel: `app/src/main/java/com/vohuy/mixueapp/ui/viewmodel/ProductViewModel.kt`
   - Gọi repository `ProductRepository.createProductWithImage(...)`

3) Repository: `app/src/main/java/com/vohuy/mixueapp/data/repository/ProductRepository.kt`
   - Tạo `productId` Firestore
   - Upload ảnh lên Supabase Storage (public bucket) qua `SupabaseImageRepository`
   - Lưu `imageUrl` + `imagePath` vào Firestore document trong collection `products`

4) Supabase storage client:
   - `app/src/main/java/com/vohuy/mixueapp/data/remote/supabase/SupabaseClientProvider.kt`
   - Keys lấy từ `BuildConfig.*` (được inject từ `local.properties`).


---

## 9) Appendix: Điểm cần kiểm chứng khi bàn giao

- `app/google-services.json` có đúng `applicationId = com.vohuy.mixueapp`.
- Firebase Auth providers đã bật đúng trong Firebase Console.
- Firestore có collections tối thiểu: `products`, `orders`, `users` (tuỳ design).
- Supabase bucket public + URL public build đúng format trong `SUPABASE_SETUP.md`.


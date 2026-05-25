# Firebase: Vì sao Console báo “chưa tạo Database” nhưng app vẫn đăng nhập/lưu thông tin?

> Mục tiêu: giải thích chính xác tình trạng hiện tại và hướng dẫn bạn kiểm tra dữ liệu đang nằm ở đâu (Auth / Firestore / Emulator / Local).

## 1) Hiện trạng của bạn (theo code dự án)

- App **đã tích hợp Firebase** (có `app/google-services.json`, dùng `com.google.gms.google-services`).
- App **đã dùng Firebase Authentication** để đăng ký/đăng nhập.
- App **có code dùng Cloud Firestore** để lưu/đọc thông tin user:
  - Ghi: `firestore.collection("users").document(userId).set(user)`
  - Đọc: `firestore.collection("users").document(uid).get()`

> Lưu ý: **Firestore** và **Realtime Database** là 2 sản phẩm khác nhau. Ảnh bạn gửi đang ở trang “Create database” của *Firestore/Realtime Database* (chưa tạo DB), nhưng điều đó **không ngăn Authentication hoạt động**.

## 2) Vì sao “chưa tạo DB” mà vẫn đăng nhập/lưu được?

### A. Firebase Authentication hoạt động *độc lập* với Database
Bạn có thể tạo tài khoản/đăng nhập và thấy user xuất hiện trong **Console → Authentication → Users** ngay cả khi **Firestore/Realtime Database chưa tạo**.

### B. Bạn có thể đang xem *nhầm Firebase project*
App kết nối đến Firebase project theo thông tin trong `app/google-services.json` (đặc biệt là `project_id`).

Nếu Console bạn đang mở **project khác**, bạn sẽ thấy trạng thái “chưa tạo database”, trong khi app lại đang ghi/đọc ở project thật.

### C. Bạn “thấy lưu thông tin” nhưng thực ra là dữ liệu từ Auth hoặc cache/local
Nhiều màn hình có thể hiển thị:
- Email/UID từ `FirebaseAuth.currentUser` (đây là *Auth*, không phải DB)
- Trạng thái đăng nhập/token được lưu local
- Dữ liệu demo/hardcode

### D. (Ít gặp nhưng có thể) App đang dùng Firestore Emulator
Nếu dự án có cấu hình `useEmulator(...)` thì dữ liệu sẽ nằm **trên máy của bạn** (emulator), không hiện lên Firebase Console.

## 3) Check nhanh: App đang dùng Firestore hay Realtime Database?

- **Firestore**: code có `FirebaseFirestore`, `.collection(...)`, `.document(...)`, `.set(...)`, `.get()`.
- **Realtime Database**: code có `FirebaseDatabase`, `.getReference(...)`.

Dự án hiện tại của bạn đang dùng **Firestore** (không phải Realtime Database).

## 4) Cách xác định app đang kết nối đúng Firebase project nào

1. Mở file: `app/google-services.json`
2. Tìm các trường:
   - `project_info.project_id`
   - `client[...].client_info.mobilesdk_app_id` (GMP App ID)
   - `client[...].client_info.android_client_info.package_name`
3. Trên Firebase Console:
   - Chọn **đúng project** có `project_id` giống trong file.
   - Vào **Project settings → Your apps**: kiểm tra đúng `package_name` và GMP App ID.

> Nếu project_id không khớp: bạn đang nhìn nhầm project trên Console.

## 5) Cách kiểm tra dữ liệu đang nằm ở đâu

### 5.1 Kiểm tra Auth (tài khoản)
- Console → **Authentication → Users**
- Nếu thấy email/uid ở đây: OK, tài khoản đang lưu trên Firebase Auth (cloud).

### 5.2 Kiểm tra Firestore (users/products/orders…)
- Console → **Build → Firestore Database → Data**
- Tìm collection `users` và document theo `uid`.

Nếu bạn chưa tạo Firestore database, Console sẽ hiện nút **Create database**.

### 5.3 Kiểm tra có dùng Emulator không
Trong codebase, search từ khóa:
- `useEmulator`
- `10.0.2.2` (host emulator Android)
- `localhost` (host)

Nếu có emulator, dữ liệu sẽ xuất hiện trong **Emulator UI**, không nằm trên Console.

### 5.4 Kiểm tra local storage
Search từ khóa:
- `Room`, `DataStore`, `SharedPreferences`

Nếu có, một phần dữ liệu có thể đang lưu local.

## 6) Cách “bật/tạo” Firestore Database đúng cách

Nếu ứng dụng của bạn muốn lưu **sản phẩm / đơn hàng / user profile** lên cloud database, bạn cần tạo Firestore:

1. Firebase Console → **Build → Firestore Database**
2. Nhấn **Create database**
3. Chọn:
   - Location (khu vực)
   - Mode:
     - **Production mode** (khuyên dùng)
     - Test mode (chỉ dùng tạm để thử, vì rules mở)

### Gợi ý rules tối thiểu cho collection `users`
- Cho phép user chỉ đọc/ghi document của chính mình (`request.auth.uid == userId`).

> Mình chưa thêm rules cụ thể vào đây vì rules phụ thuộc cấu trúc collections (users/products/orders/admin). Nếu bạn muốn, mình có thể viết bộ rules đầy đủ theo model trong dự án.

## 7) Checklist để “biết chắc” dữ liệu не lưu local

- [ ] Console → Authentication có user mới tạo
- [ ] `google-services.json` project_id khớp với project đang mở
- [ ] Console → Firestore Database đã được tạo
- [ ] Console → Firestore → Data nhìn thấy `users/{uid}` sau khi đăng ký
- [ ] Không cấu hình emulator, hoặc nếu có thì bạn biết rõ dữ liệu ở emulator UI

## 8) Ghi chú về Storage (ảnh sản phẩm)

Trong `build.gradle.kts` bạn đang có **Firebase Storage** và đồng thời dự án có **Supabase Storage**.
- Nếu bạn lưu ảnh lên Supabase: ảnh sẽ **không nằm trong Firebase Storage**.
- Nếu bạn lưu ảnh lên Firebase Storage: ảnh sẽ nằm ở Console → Storage.

Nên thống nhất 1 nơi để tránh nhầm.

---

## Lệnh/Thao tác kiểm tra nhanh trong dự án (tùy chọn)

Bạn có thể tìm nhanh các chỗ dùng Firebase bằng cách search trong IDE:
- `FirebaseAuth`
- `FirebaseFirestore`
- `FirebaseDatabase`
- `useEmulator`

Nếu bạn muốn mình xác nhận chính xác 100% dữ liệu đang được lưu ở đâu trong dự án của bạn, mình sẽ:
1) đọc `google-services.json`
2) rà soát các repository (Auth/Product/Order)
3) tìm cấu hình emulator / local storage
và kết luận bằng checklist cụ thể cho từng loại dữ liệu.


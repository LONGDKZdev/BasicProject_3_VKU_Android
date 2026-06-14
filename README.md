# HỆ THỐNG QUẢN LÝ VÀ ĐẶT ĐỒ UỐNG MIXUE (MOBILE APP & WEB ADMIN)

## 1. TỔNG QUAN DỰ ÁN
Dự án là một hệ thống phần mềm khép kín phục vụ mô hình kinh doanh F&B (Cửa hàng đồ uống Mixue). Hệ thống bao gồm hai phân hệ chính hoạt động song song và đồng bộ dữ liệu theo thời gian thực (Real-time):
- Phân hệ Khách hàng (Client Application): Ứng dụng di động trên nền tảng Android.
- Phân hệ Quản trị (Admin Portal): Trang quản trị trên nền tảng Web.

## 2. KIẾN TRÚC VÀ CÔNG NGHỆ SỬ DỤNG
Hệ thống được thiết kế theo mô hình Client-Serverless, tận dụng các dịch vụ Backend-as-a-Service (BaaS) để tối ưu hóa thời gian triển khai và tốc độ phản hồi.

### 2.1. Phân hệ Mobile App (Android)
- Ngôn ngữ: Kotlin.
- Kiến trúc phần mềm: MVVM (Model - View - ViewModel) kết hợp với Repository Pattern.
- Quản lý trạng thái (State Management): Sử dụng LiveData và StateFlow.
- Giao diện (UI): Xây dựng hoàn toàn bằng Jetpack Compose (Material Design 3).
- Điều hướng: Navigation Compose.
- Xử lý bất đồng bộ: Kotlin Coroutines.
- Tải và xử lý hình ảnh: Thư viện Coil.

### 2.2. Phân hệ Web Admin
- Ngôn ngữ: HTML5, CSS3, JavaScript (ES6+).
- Kiến trúc: Vanilla JavaScript, thao tác DOM trực tiếp kết hợp mô-đun hóa (ES Modules).
- Thư viện hỗ trợ: Chart.js (vẽ biểu đồ), SweetAlert2 (hiển thị hộp thoại tương tác).

### 2.3. Backend & Cơ sở dữ liệu
- Cơ sở dữ liệu chính: Firebase Firestore (NoSQL).
- Dịch vụ xác thực: Firebase Authentication (Email/Password).
- Lưu trữ tệp tĩnh: Supabase Storage.

## 3. CẤU TRÚC CƠ SỞ DỮ LIỆU (FIRESTORE SCHEMA)
Cơ sở dữ liệu được thiết kế tối giản, bao gồm 4 Collections chính:
1. `users`: `{ id, email, fullName, phoneNumber, address, role (USER/ADMIN), createdAt }`
2. `products`: `{ id, name, price, category, description, imageUrl, imagePath, available }`
3. `orders`: `{ id, userId, customerName, phoneNumber, address, items[], totalPrice, paymentMethod, status, createdAt }`
4. `transactions`: `{ id, orderId, userId, customerName, amount, paymentMethod, status, description, createdAt }`

## 4. CHỨC NĂNG CỐT LÕI
### 4.1. Đối với người dùng (App)
- Xác thực: Đăng ký/đăng nhập qua Firebase Auth, lưu phiên đăng nhập cục bộ.
- Quản lý Giỏ hàng & Thanh toán: Tự động gộp số lượng, hỗ trợ Tiền mặt/Chuyển khoản, validate thông tin chặt chẽ.
- Quản lý Đơn hàng: Theo dõi trạng thái đơn hàng (Chờ duyệt -> Đang giao -> Hoàn thành) realtime. Giới hạn chống Spam (tối đa 3 đơn PENDING).

### 4.2. Đối với Quản trị viên (Web)
- Dashboard Thống kê: Bảng điều khiển giám sát doanh thu tổng quan, tích hợp biểu đồ đường (Line Chart) cập nhật theo ngày.
- Quản lý Thực đơn (CRUD): Thêm, sửa, xóa món ăn. Upload ảnh trực tiếp lên Supabase.
- Giám sát Đơn hàng Realtime: Nhận thông báo (chuông & toast) khi có đơn mới. Xử lý quy trình đơn hàng và In hóa đơn.

## 5. HƯỚNG DẪN CÀI ĐẶT MÔI TRƯỜNG PHÁT TRIỂN

### 5.1. Khởi chạy Android App
1. Clone repository về máy trạm.
2. Mở dự án bằng Android Studio.
3. Đảm bảo tệp `google-services.json` đã được đặt vào thư mục `app/google-services.json`.
4. Bấm Sync Project with Gradle Files.
5. Build và khởi chạy trên Emulator hoặc thiết bị Android vật lý.

### 5.2. Khởi chạy Web Admin
1. Di chuyển vào thư mục `web-admin`.
2. Khởi tạo một Local Server (Ví dụ: Dùng extension "Live Server" trên VS Code hoặc chạy file `run-web-admin.bat`).
3. Truy cập địa chỉ localhost trên trình duyệt.

---

## 🔬 TÀI LIỆU KỸ THUẬT CHUYÊN SÂU (TECHNICAL DETAILS)
*Phần này mô tả chi tiết về cấu trúc mã nguồn, luồng dữ liệu và các quyết định kỹ thuật của dự án.*

### 1. Chi Tiết Kiến Trúc Mã Nguồn Android
Dự án được phân chia theo các package tiêu chuẩn nhằm đảm bảo tính tái sử dụng và dễ bảo trì:
- `core/`: Chứa các thành phần dùng chung (Constants, Result Wrapper, Error Handling).
- `data/`: Chứa `Model` (Data classes) và `Repository` (giao tiếp trực tiếp với Firebase/Supabase).
- `domain/`: Chứa các Use Case nghiệp vụ.
- `ui/`: Tầng giao diện chứa `navigation` (NavGraph), `screens` (CartScreen, HomeScreen...) và `components` (ToastMessageHandler, ItemCard...).

### 2. Chiến Lược Lưu Trữ Hình Ảnh (Supabase Storage Contract)
Tích hợp Supabase Public Bucket để tối ưu chi phí lưu trữ tệp tĩnh:
- **Đường dẫn chuẩn (Object Paths):** Ảnh sản phẩm được lưu theo chuẩn `products/{productId}/main.jpg`.
- **Cơ chế hoạt động:** Khi upload, Firestore lưu đồng thời `imageUrl` (Link Public render qua Coil) và `imagePath` (Đường dẫn gốc để Admin có thể xóa hoặc thay thế ảnh cũ).

### 3. Luồng Dữ Liệu Đồng Bộ (Data Flow)

#### 3.1. Luồng Phân quyền Web Admin (Role-Based Access)
- Sử dụng `authService.listen()` để theo dõi phiên (session).
- Khi có thay đổi, hệ thống truy vấn collection `users`. Giao diện (Tab Products, Orders, Payments) chỉ mở khóa nếu `role` là `ADMIN`.
- Các tài khoản mới sẽ nhận quyền `PENDING` (hoặc `USER`) và bị chặn ở màn hình chờ.

#### 3.2. Luồng Đồng bộ Đơn hàng Real-Time
1. Khách hàng tạo đơn trên App (Firestore `orders` collection).
2. Tự động tạo bản ghi đối soát trong `transactions` nếu thanh toán thành công.
3. Mạng WebSocket của Firebase (`onSnapshot`) đẩy tín hiệu về Web Admin.
4. Bảng điều khiển Admin tự động chèn đơn hàng mới lên đầu (áp dụng hàm sort `createdAt`), phát chuông cảnh báo và Toast hiển thị Tên khách hàng (Customer Name) mà không cần tải lại trang.

### 4. Điểm Nhấn Kỹ Thuật (Technical Highlights)
- **Tối ưu UI/UX:** Cử chỉ Pull-to-Refresh trên Mobile; hệ thống Toast Notification tự code trên Web thay cho hộp thoại mặc định.
- **Xử lý Bất đồng bộ:** Sử dụng `LaunchedEffect` trong Compose để chặn Race Condition khi tải dữ liệu người dùng.
- **Xóa Back Stack (Lịch sử trang):** Sử dụng `popUpTo(0)` để dọn sạch bộ nhớ và chặn việc ứng dụng lặp lại màn hình trung gian (ví dụ: giỏ hàng, chi tiết món) khi người dùng thao tác Lùi hoặc Đăng xuất.
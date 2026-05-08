# Mixue Web Admin (UI-only skeleton)

Mục tiêu của thư mục `web-admin/`: **chỉ giữ giao diện (HTML/CSS/JS)** để bạn tuỳ biến theo dự án.

- Không phụ thuộc Firebase/PHP
- Không CRUD thật (dùng mock/in-memory data)
- Khi cần tích hợp thật sau này: chỉ cần thay các file trong `src/services/`

## Chạy web admin (UI-only)
Bạn vẫn nên serve bằng HTTP (không mở file trực tiếp bằng `file://`).

### Cách nhanh (PowerShell) – Python (nếu có)
```powershell
cd D:\StorageCode_Android\CodeTest\Basic_Project_3\MixueOrderapp\web-admin
python -m http.server 5173
```

Mở: http://localhost:5173/

### Hoặc PHP (nếu có)
```powershell
cd D:\StorageCode_Android\CodeTest\Basic_Project_3\MixueOrderapp\web-admin
php -S localhost:5173
```

## Tích hợp backend/Firebase sau này
Xem hướng dẫn tại: `src/services/README_INTEGRATION.md`

Các service hiện tại:
- `src/services/authService.js`
- `src/services/productsService.js`
- `src/services/ordersService.js`

UI layer vẫn nằm trong:
- `index.html`
- `assets/styles.css`
- `src/main.js`


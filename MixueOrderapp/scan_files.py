import os

def get_size_format(b, factor=1024, suffix="B"):
    """
    Hàm chuyển đổi dung lượng từ byte sang KB, MB, GB...
    """
    for unit in ["", "KB", "MB", "GB", "TB", "PB"]:
        if b < factor:
            return f"{b:.2f}{unit}{suffix}"
        b /= factor
    return f"{b:.2f}PB{suffix}"

def scan_directory():
    # Lấy đường dẫn thư mục hiện tại (nơi đặt file script này)
    current_dir = os.getcwd()
    output_file = "Danh_sach_file.txt"
    
    # Mở file để ghi (encoding utf-8 để không lỗi font tiếng Việt hoặc ký tự lạ)
    with open(output_file, "w", encoding="utf-8") as f:
        print("Dang quet file, vui long doi...")
        
        # os.walk giúp quét đệ quy (quét hết các folder con)
        for root, dirs, files in os.walk(current_dir):
            for filename in files:
                # Bỏ qua chính file script này và file kết quả
                if filename == output_file or filename.endswith(".py"):
                    continue
                
                # Lấy đường dẫn đầy đủ
                filepath = os.path.join(root, filename)
                
                try:
                    # Lấy kích thước file
                    file_size = os.path.getsize(filepath)
                    formatted_size = get_size_format(file_size)
                    
                    # Tạo đường dẫn tương đối (ví dụ: mods/modgame/a.txt)
                    relative_path = os.path.relpath(filepath, current_dir)
                    
                    # Ghi vào file theo format bạn yêu cầu
                    # Thay thế dấu \ thành / nếu muốn giống format web/linux, hoặc để nguyên
                    line = f"{relative_path} (Size: {formatted_size})\n"
                    f.write(line)
                except Exception as e:
                    print(f"Khong the doc file: {filename} - Loi: {e}")

    print(f"Xong! Da tao file '{output_file}'")

if __name__ == "__main__":
    scan_directory()
    # Dòng dưới để giữ cửa sổ không tắt ngay nếu bạn chạy bằng double click
    input("Nhan Enter de thoat...")
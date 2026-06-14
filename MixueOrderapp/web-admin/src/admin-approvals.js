import { collection, getDocs, query, where, doc, updateDoc } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js"; // Đồng bộ biến COLLECTIONS hệ thống

// 1. Hàm tải danh sách các tài khoản đang chờ duyệt (Quyền: USER)
window.loadApprovals = async function() {
  const tbody = document.querySelector("#approvalsTable tbody");
  if (!tbody) return;
  tbody.innerHTML = "<tr><td colspan='4' style='text-align:center;'>Đang tải danh sách...</td></tr>";

  try {
    // Sử dụng COLLECTIONS.users để khớp chính xác cấu trúc dữ liệu của dự án
    const q = query(collection(db, COLLECTIONS.users), where("role", "==", "USER"));
    const snap = await getDocs(q);

    tbody.innerHTML = "";
    if (snap.empty) {
      tbody.innerHTML = "<tr><td colspan='4' class='muted' style='text-align:center;'>Không có tài khoản nào đang chờ duyệt</td></tr>";
      return;
    }

    snap.forEach(docSnap => {
      const user = docSnap.data();
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><strong>${user.fullName || "Chưa cập nhật"}</strong></td>
        <td>${user.email}</td>
        <td><span class="pill" style="background-color: #ff9800; color: white;">Đang chờ duyệt</span></td>
        <td>
          <button class="btn" style="padding: 6px 12px; font-size: 12px; background-color: #4CAF50; color: white; border: none; border-radius: 6px; cursor: pointer;" onclick="window.approveAdminFn('${docSnap.id}')">
            ✅ Cấp quyền Admin
          </button>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (error) {
    console.error("Lỗi tải danh sách:", error);
    // Đẩy lỗi trực tiếp ra giao diện để dễ theo dõi nếu lỗi rules bảo mật
    tbody.innerHTML = `<tr><td colspan='4' style='text-align:center; color: var(--primary); font-weight:bold;'>❌ Lỗi Firestore: ${error.message}</td></tr>`;
  }
};

// 2. Hàm khi Admin bấm nút "Cấp quyền"
window.approveAdminFn = async function(uid) {
  const result = await Swal.fire({
    title: 'Xác nhận cấp quyền?',
    text: "Tài khoản này sẽ có toàn quyền Quản trị (Thêm món, Duyệt đơn)!",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#4CAF50',
    cancelButtonColor: '#d33',
    confirmButtonText: 'Đồng ý Cấp',
    cancelButtonText: 'Hủy bỏ'
  });

  if (result.isConfirmed) {
    try {
      await updateDoc(doc(db, COLLECTIONS.users, uid), { role: "ADMIN" });
      await Swal.fire('Thành công!', 'Đã cấp quyền ADMIN thành công.', 'success');
      window.loadApprovals(); // Tải lại bảng ngay lập tức để làm sạch danh sách
    } catch (e) {
      Swal.fire('Lỗi!', e.message, 'error');
    }
  }
};

// 3. Lắng nghe sự kiện click độc lập và xử lý hiển thị giao diện
document.querySelectorAll(".admin-tab").forEach(tab => {
  tab.addEventListener("click", (e) => {
    // Lấy tên tab vừa được bấm
    const targetTab = e.currentTarget.getAttribute("data-tab");
    const tabApprovals = document.getElementById("tab-approvals");

    if (tabApprovals) {
      if (targetTab === "approvals") {
        // Bật hiển thị Tab Duyệt Admin
        tabApprovals.classList.remove("hidden");
        window.loadApprovals(); // Kích hoạt nạp dữ liệu
      } else {
        // Giấu Tab Duyệt Admin đi khi bấm sang mục khác
        tabApprovals.classList.add("hidden");
      }
    }
  });
});
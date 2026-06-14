import {
  listenOrders,
  listenPendingOrders,
  confirmOrder,
  rejectOrder,
  markPrepared,
  setOrderStatus
} from "./admin-orders.js";
import { showSuccess, showError } from "./toast.js";

/**
 * Initialize Orders Tab - hiển thị tất cả đơn hàng
 */
export function initOrdersTab() {
  const allOrdersTable = document.getElementById("ordersTable");
  if (!allOrdersTable) return;

  let isFirstLoad = true; // Biến kiểm tra lần tải đầu tiên
  let previousOrderCount = 0; // Biến lưu số lượng đơn hàng cũ

  // Load all orders (Lắng nghe realtime)
  listenOrders(100, (orders) => {

    // LOGIC CHUÔNG BÁO: Nếu không phải lần tải đầu và số đơn tăng lên
    if (!isFirstLoad && orders.length > previousOrderCount) {
      // 1. Phát âm thanh (Dùng 1 file âm thanh ting ting có sẵn trên mạng)
      const notificationSound = new Audio("https://actions.google.com/sounds/v1/alarms/digital_watch_alarm_long.ogg");
      notificationSound.play().catch(e => console.log("Trình duyệt chặn tự động phát âm thanh"));

      // 2. Bắn thông báo Toast lên màn hình
      showSuccess("🔔 BẠN CÓ ĐƠN HÀNG MỚI! Hãy kiểm tra ngay.", 5000);
    }

    // Cập nhật lại số lượng và trạng thái
    previousOrderCount = orders.length;
    isFirstLoad = false;

    // Render lại bảng
    renderAllOrdersTable(allOrdersTable, orders);
  });

  // Manual reload handler
  const btnReloadOrders = document.getElementById("btnReloadOrders");
  if (btnReloadOrders) {
    btnReloadOrders.onclick = () => {
      console.log("🔄 Reloading all orders...");
      listenOrders(100, (orders) => {
        renderAllOrdersTable(allOrdersTable, orders);
      });
    };
  }
}

/**
 * Render all orders table - với nút hủy & tên customer
 */
function renderAllOrdersTable(table, orders) {
  const tbody = table.querySelector("tbody");
  tbody.innerHTML = "";

  if (orders.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="muted text-center">Không có đơn hàng</td></tr>`;
    return;
  }

  // Sort by createdAt descending (newest first)
  const sortedOrders = [...orders].sort((a, b) => {
    const timeA = a.createdAt?.toDate?.() || new Date(a.createdAt);
    const timeB = b.createdAt?.toDate?.() || new Date(b.createdAt);
    return new Date(timeB) - new Date(timeA);
  });

  sortedOrders.forEach((order) => {
    const row = document.createElement("tr");
    const createdAt = order.createdAt
      ? new Date(order.createdAt.toDate?.() || order.createdAt).toLocaleString("vi-VN")
      : "-";
    const statusBadge = getStatusBadge(order.status || "pending");
    const customerName = order.customerName || order.userId || "-";

    const phone = order.phoneNumber ? `<div class="muted small" style="margin-top: 4px;">📞 ${order.phoneNumber}</div>` : "";
    const address = order.address ? `<div class="muted small" style="white-space: normal; max-width: 250px;">📍 ${order.address}</div>` : "";

    const payMethod = order.paymentMethod === 'BANK_TRANSFER'
        ? '<div style="color:#2196F3; font-size: 0.8rem; margin-top: 4px;">💳 Chuyển khoản</div>'
        : '<div style="color:#9C27B0; font-size: 0.8rem; margin-top: 4px;">💵 Tiền mặt</div>';

    const status = order.status || 'PENDING';
    let actionButtons = '';

    if (status === 'CANCELLED') {
        actionButtons = '';
    } else if (status === 'DONE') {
        actionButtons = `<button class="btn btn--small btn--info" onclick="window.printInvoiceFn('${order.id}')"> 📄 In </button>`;
    } else {
        if (status === 'PENDING' || status === 'pending') {
            actionButtons += `<button class="btn btn--small btn--success" onclick="window.confirmOrderFn('${order.id}')"> ✅ Xác Thực </button>`;
        } else if (status === 'CONFIRMED' || status === 'confirmed') {
            actionButtons += `<button class="btn btn--small btn--primary" onclick="window.deliverOrderFn('${order.id}')"> 🚚 Giao Hàng </button>`;
        } else if (status === 'DELIVERING' || status === 'delivering') {
            actionButtons += `<button class="btn btn--small btn--success" onclick="window.doneOrderFn('${order.id}')"> ✔️ Hoàn Thành </button>`;
        }
        actionButtons += ` <button class="btn btn--small btn--danger" onclick="window.rejectOrderFn('${order.id}')"> ❌ Hủy </button>`;
        actionButtons += ` <button class="btn btn--small btn--info" onclick="window.printInvoiceFn('${order.id}')"> 📄 In </button>`;
    }

    row.innerHTML = `
      <td><code>${order.id.substring(0,8)}</code></td>
      <td>
        <div class="font-bold">${customerName}</div>
        ${phone}
        ${address}
      </td>
      <td class="text-right font-bold">
        ${formatPrice(order.totalPrice || 0)}
        ${payMethod}
      </td>
      <td>${statusBadge}</td>
      <td>${createdAt}</td>
      <td>
        <div class="row row--compact">
          ${actionButtons}
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
}

/**
 * Get status badge HTML
 */
function getStatusBadge(status) {
  const badges = {
    PENDING: '<span class="badge badge--warning">⏳ Chờ</span>',
    CONFIRMED: '<span class="badge badge--info">✅ Xác Thực</span>',
    DELIVERING: '<span class="badge badge--primary">🚚 Đang Giao</span>',
    DONE: '<span class="badge badge--success">✔ Hoàn Thành</span>',
    CANCELLED: '<span class="badge badge--danger">✗ Hủy</span>',
    pending: '<span class="badge badge--warning">⏳ Chờ</span>',
    confirmed: '<span class="badge badge--info">✅ Xác Thực</span>',
    preparing: '<span class="badge badge--primary">🔨 Chuẩn Bị</span>',
    delivering: '<span class="badge badge--primary">🚚 Đang Giao</span>',
    done: '<span class="badge badge--success">✔ Hoàn Thành</span>',
    cancelled: '<span class="badge badge--danger">✗ Hủy</span>'
  };
  return badges[status] || `<span class="badge">${status}</span>`;
}

// ==========================================
// CÁC HÀM XỬ LÝ NÚT BẤM (Giao diện SweetAlert2)
// ==========================================
window.confirmOrderFn = async (id) => {
  const result = await Swal.fire({
    title: 'Xác thực đơn hàng?',
    text: "Bạn chuẩn bị duyệt đơn hàng này!",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#4CAF50',
    cancelButtonColor: '#d33',
    confirmButtonText: 'Đồng ý Duyệt',
    cancelButtonText: 'Hủy thao tác'
  });
  if (!result.isConfirmed) return;

  try {
    await confirmOrder(id);
    showSuccess("✅ Đã xác thực đơn hàng!");
    document.getElementById('btnReloadOrders')?.click();
    document.getElementById('btnReloadPayments')?.click();
  } catch (e) {
    showError("❌ Lỗi: " + e.message);
  }
};

window.deliverOrderFn = async (id) => {
  const result = await Swal.fire({
    title: 'Chuyển cho Shipper?',
    text: "Đơn hàng sẽ chuyển sang trạng thái Đang giao.",
    icon: 'info',
    showCancelButton: true,
    confirmButtonColor: '#2196F3',
    cancelButtonColor: '#d33',
    confirmButtonText: 'Bắt đầu giao',
    cancelButtonText: 'Hủy'
  });
  if (!result.isConfirmed) return;

  try {
    await markPrepared(id);
    showSuccess("🚚 Đã chuyển sang Đang Giao!");
    document.getElementById('btnReloadOrders')?.click();
  } catch (e) {
    showError("❌ Lỗi: " + e.message);
  }
};

window.doneOrderFn = async (id) => {
  const result = await Swal.fire({
    title: 'Hoàn thành đơn?',
    text: "Khách hàng đã nhận được đồ uống thành công?",
    icon: 'success',
    showCancelButton: true,
    confirmButtonColor: '#4CAF50',
    cancelButtonColor: '#d33',
    confirmButtonText: 'Đã hoàn thành',
    cancelButtonText: 'Chưa'
  });
  if (!result.isConfirmed) return;

  try {
    await setOrderStatus(id, 'DONE');
    showSuccess("✔️ Đã hoàn thành đơn hàng!");
    document.getElementById('btnReloadOrders')?.click();
    document.getElementById('btnReloadPayments')?.click();
  } catch (e) {
    showError("❌ Lỗi: " + e.message);
  }
};

window.rejectOrderFn = async (id) => {
  const { value: reason } = await Swal.fire({
    title: 'Hủy đơn hàng',
    input: 'text',
    inputLabel: 'Nhập lý do hủy đơn',
    inputPlaceholder: 'Ví dụ: Hết nguyên liệu, Khách đổi ý...',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#F44336',
    cancelButtonColor: '#9e9e9e',
    confirmButtonText: 'Xác nhận Hủy',
    cancelButtonText: 'Đóng',
    inputValidator: (value) => {
      if (!value) return 'Bạn cần nhập lý do để hủy!';
    }
  });
  if (!reason) return;

  try {
    await rejectOrder(id, reason);
    showSuccess("❌ Đã hủy đơn hàng!");
    document.getElementById('btnReloadOrders')?.click();
    document.getElementById('btnReloadPayments')?.click();
  } catch (e) {
    showError("❌ Lỗi: " + e.message);
  }
};
/**
 * Print invoice as PDF
 */
window.printInvoiceFn = async function (orderId) {
  try {
    const invoicePath = 'invoice.html?orderId=' + orderId;
    const printWindow = window.open(invoicePath, "printWindow");
    if (printWindow) {
      printWindow.addEventListener("load", () => {
        printWindow.print();
      });
    }
  } catch (error) {
    showError("❌ Lỗi in: " + error.message);
  }
};

function formatPrice(price) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(price);
}

export { confirmOrder, rejectOrder, markPrepared };
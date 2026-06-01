import {
  listenOrders,
  listenPendingOrders,
  confirmOrder,
  rejectOrder,
  markPrepared
} from "./admin-orders.js";
import { showSuccess, showError } from "./toast.js";

/**
 * Initialize Orders Tab - hiển thị tất cả đơn hàng
 */
export function initOrdersTab() {
   const allOrdersTable = document.getElementById("ordersTable");

   if (!allOrdersTable) return;

   // Load all orders (single listener for all orders)
   listenOrders(100, (orders) => {
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
 * Render pending orders table
 */
function renderPendingOrdersTable(table, orders) {
  const tbody = table.querySelector("tbody");
  tbody.innerHTML = "";

  if (orders.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="muted text-center">Không có đơn chờ xác thực</td></tr>`;
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
    const customerName = order.customerName || order.userId || "-";

    row.innerHTML = `
      <td><code>${order.id}</code></td>
      <td>${customerName}</td>
      <td class="text-right font-bold">${formatPrice(order.totalPrice || 0)}</td>
      <td>${createdAt}</td>
      <td>
        <div class="row row--compact">
          <button class="btn btn--small btn--success" onclick="window.confirmOrderFn('${order.id}')">
            ✅ Xác Thực
          </button>
          <button class="btn btn--small btn--danger" onclick="window.rejectOrderFn('${order.id}')">
            ❌ Từ Chối
          </button>
          <button class="btn btn--small btn--info" onclick="window.printInvoiceFn('${order.id}')">
            📄 In
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
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

    row.innerHTML = `
      <td><code>${order.id}</code></td>
      <td>${customerName}</td>
      <td>${order.userId || "-"}</td>
      <td class="text-right font-bold">${formatPrice(order.totalPrice || 0)}</td>
      <td>${statusBadge}</td>
      <td>${createdAt}</td>
      <td>
        <div class="row row--compact">
          <button class="btn btn--small btn--success" onclick="window.confirmOrderFn('${order.id}')">
            ✅ Xác Thực
          </button>
          <button class="btn btn--small btn--danger" onclick="window.rejectOrderFn('${order.id}')">
            ❌ Hủy
          </button>
          <button class="btn btn--small btn--info" onclick="window.printInvoiceFn('${order.id}')">
            📄 In
          </button>
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
    // Backward compatibility with old lowercase values
    pending: '<span class="badge badge--warning">⏳ Chờ</span>',
    confirmed: '<span class="badge badge--info">✅ Xác Thực</span>',
    preparing: '<span class="badge badge--primary">🔨 Chuẩn Bị</span>',
    delivering: '<span class="badge badge--primary">🚚 Đang Giao</span>',
    done: '<span class="badge badge--success">✔ Hoàn Thành</span>',
    cancelled: '<span class="badge badge--danger">✗ Hủy</span>'
  };
  return badges[status] || `<span class="badge">${status}</span>`;
}

/**
 * Confirm order (Global function)
 */
window.confirmOrderFn = async function (orderId) {
  if (!confirm("Xác nhận đơn hàng này?")) return;
  try {
    await confirmOrder(orderId);
    showSuccess("✅ Đơn hàng đã được xác nhận");
    initOrdersTab(); // Refresh UI
  } catch (error) {
    showError("❌ Lỗi: " + error.message);
  }
};

/**
 * Reject order (Global function)
 */
window.rejectOrderFn = async function (orderId) {
  const reason = prompt("Lý do từ chối (tuỳ chọn):", "");
  if (reason === null) return;

  try {
    await rejectOrder(orderId, reason);
    showSuccess("❌ Đơn hàng đã bị từ chối");
    initOrdersTab(); // Refresh UI
  } catch (error) {
    showError("❌ Lỗi: " + error.message);
  }
};

/**
 * Print invoice as PDF (Global function)
 */
window.printInvoiceFn = async function (orderId) {
  try {
    // Sửa lỗi địa chỉ: Sử dụng đường dẫn tương đối để tránh lỗi 404 khi chạy trong thư mục con
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

/**
 * Format price to VND
 */
function formatPrice(price) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(price);
}

// Export cho main.js
export { confirmOrder, rejectOrder, markPrepared };


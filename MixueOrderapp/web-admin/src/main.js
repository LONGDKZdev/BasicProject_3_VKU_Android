import { ROLES } from "./services/constants.js";
import { authService } from "./services/authService.js";
import { productsService } from "./services/productsService.js";
import { ordersService } from "./services/ordersService.js";
import * as paymentsService from "./services/paymentsService.js";
import { uploadProductImage } from "./supabase.js";
import { bootstrapAdminIfAllowed } from "./services/adminBootstrap.js";
import { seedAll } from "./services/seedService.js";
import { initOrdersTab } from "./orders-ui.js";
import { register, logout as authLogout, shouldAutoLogin, getSavedLoginEmail } from "./auth.js";
import { auth } from "./firebase.js";
import { showSuccess, showError, showInfo } from "./toast.js";

const $ = (id) => document.getElementById(id);

const els = {
  // header
  userInfo: $("userInfo"),
  btnLogout: $("btnLogout"),
  // login
  cardLogin: $("cardLogin"),
  authForm: $("authForm"),
  email: $("email"),
  password: $("password"),
  btnSubmitAuth: $("btnSubmitAuth"),
  fullName: $("fullName"),
  confirmPassword: $("confirmPassword"),
  registerFields: $("registerFields"),
  registerFieldsConfirm: $("registerFieldsConfirm"),
  btnSendVerify: $("btnSendVerify"),
  verifyHint: $("verifyHint"),
  tabModeLogin: $("tabModeLogin"),
  tabModeRegister: $("tabModeRegister"),
  // role
  cardRole: $("cardRole"),
  kvUid: $("kvUid"),
  kvEmail: $("kvEmail"),
  kvRole: $("kvRole"),
  // admin
  cardAdmin: $("cardAdmin"),
  cardNotAdmin: $("cardNotAdmin"),
   // tabs
   tabButtons: Array.from(document.querySelectorAll(".admin-tab")),
   tabProducts: $("tab-products"),
   tabOrders: $("tab-orders"),
   tabPayments: $("tab-payments"), // 🆕
  // products
  btnReloadProducts: $("btnReloadProducts"),
  productForm: $("productForm"),
  p_id: $("p_id"),
  p_name: $("p_name"),
  p_price: $("p_price"),
  p_category: $("p_category"),
  p_description: $("p_description"),
  p_available: $("p_available"),
  p_imageUrl: $("p_imageUrl"),
  p_imageFile: $("p_imageFile"),
  btnUploadImage: $("btnUploadImage"),
  uploadStatus: $("uploadStatus"),
  btnResetProduct: $("btnResetProduct"),
  productsTable: $("productsTable"),
  // orders
  btnReloadOrders: $("btnReloadOrders"),
  ordersTable: $("ordersTable"),
  // payments 🆕
  btnReloadPayments: $("btnReloadPayments"),
  paymentsTable: $("paymentsTable"),
};

// (ĐÃ XÓA: ADMIN_INVITE_CODE và các hàm isGatePassed, setGatePassed)

function setHidden(el, hidden) {
  if (!el) return;
  el.classList.toggle("hidden", hidden);
}

function logLine(s) {
  if (els.healthLog) {
    els.healthLog.textContent += `${s}\n`;
    els.healthLog.scrollTop = els.healthLog.scrollHeight;
  }
}

function clearLog() {
  if (els.healthLog) {
    els.healthLog.textContent = "";
  }
}

function setRolePill(role) {
  els.kvRole.textContent = role ?? "(missing)";
  els.kvRole.style.borderColor = role === ROLES.admin ? "rgba(71,209,108,.6)" : "rgba(110,168,254,.55)";
}

async function refreshProductsTable() {
  if (!els.productsTable) return;
  const tbody = els.productsTable.querySelector("tbody");
  if (!tbody) return;
  tbody.innerHTML = "";

  const items = await productsService.listProducts();
  for (const p of items) {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><code>${escapeHtml(p.id ?? "")}</code></td>
      <td>${escapeHtml(p.name ?? "")}</td>
      <td>${escapeHtml(String(p.price ?? ""))}</td>
      <td>${escapeHtml(p.category ?? "")}</td>
      <td>${escapeHtml(String(p.available ?? ""))}</td>
      <td>
        <div class="row">
          <button class="btn btn--ghost" data-act="edit" data-id="${escapeAttr(p.id ?? "")}">Edit</button>
          <button class="btn btn--ghost" data-act="del" data-id="${escapeAttr(p.id ?? "")}" style="border-color: rgba(255,107,107,.45)">Delete</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  }

  tbody.onclick = async (ev) => {
    const btn = ev.target.closest("button");
    if (!btn) return;
    const act = btn.getAttribute("data-act");
    const id = btn.getAttribute("data-id");
    if (!act || !id) return;

    if (act === "edit") {
      const current = items.find((x) => x.id === id);
      if (!current) return;
      els.p_id.value = current.id ?? "";
      els.p_name.value = current.name ?? "";
      els.p_price.value = String(current.price ?? "");
      els.p_category.value = current.category ?? "";
      els.p_description.value = current.description ?? "";
      els.p_available.value = String(Boolean(current.available));
      els.p_imageUrl.value = current.imageUrl ?? "";
      // Preserve imagePath so updating other fields won't wipe it.
      els.p_imageUrl.dataset.path = current.imagePath ?? "";
      window.scrollTo({ top: 0, behavior: "smooth" });
    }

    if (act === "del") {
      if (!confirm(`Delete product ${id}?`)) return;
      await productsService.deleteProduct(id);
      await refreshProductsTable();
    }
  };
}

// Orders table is now managed by initOrdersTab() in orders-ui.js with real-time listeners

let unsubPayments = null; // Real-time subscription for payments

function refreshPaymentsTable() {
  if (!els.paymentsTable) return;
  const tbody = els.paymentsTable.querySelector("tbody");
  if (!tbody) return;

  // Cancel old subscription if exists
  if (unsubPayments) {
    unsubPayments();
  }

  // Listen to all payments in real-time
  unsubPayments = paymentsService.listenTransactions((items) => {
    tbody.innerHTML = "";

// Chỉ cộng tiền những giao dịch có status là SUCCESS
    const totalRevenue = items
      .filter(payment => payment.status === 'SUCCESS')
      .reduce((sum, payment) => sum + (Number(payment.amount) || 0), 0);

    const statRevenueEl = document.getElementById("statRevenue");
    if (statRevenueEl) {
      statRevenueEl.textContent = formatPrice(totalRevenue);
    }

    if (items.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="muted text-center">Không có giao dịch nào</td></tr>`;
      return;
    }

    items.forEach((payment) => {
      const row = document.createElement("tr");
      const createdAt = payment.createdAt
        ? new Date(payment.createdAt.toDate?.() || payment.createdAt).toLocaleString("vi-VN")
        : "-";

      const statusBadge = getPaymentStatusBadge(payment.status || "SUCCESS");
      const methodBadge = getPaymentMethodBadge(payment.paymentMethod || "CASH");
      const customerName = payment.customerName || payment.userId?.substring(0, 8) || "-";
      row.innerHTML = `
        <td><code>${escapeHtml(payment.id?.substring(0, 8) || "")}</code></td>
        <td class="font-bold">${escapeHtml(customerName)}</td>
        <td><code>${escapeHtml(payment.orderId || "")}</code></td>
        <td class="text-right font-bold" style="color: var(--primary);">${formatPrice(payment.amount || 0)}</td>
        <td>${methodBadge}</td>
        <td>${statusBadge}</td>
        <td class="muted small">${createdAt}</td>
      `;
      tbody.appendChild(row);
    });
  });
}

function getPaymentStatusBadge(status) {
  const badges = {
    SUCCESS: '<span class="badge badge--success">✅ Thành Công</span>',
    PENDING: '<span class="badge badge--warning">⏳ Chờ</span>',
    FAILED: '<span class="badge badge--danger">❌ Thất Bại</span>'
  };
  return badges[status] || `<span class="badge">${status}</span>`;
}

function getPaymentMethodBadge(method) {
  const badges = {
    CASH: '<span class="badge" style="background-color: rgba(156, 39, 176, 0.2); color: #9C27B0;">💵 Tiền Mặt</span>',
    CARD: '<span class="badge" style="background-color: rgba(33, 150, 243, 0.2); color: #2196F3;">💳 Thẻ</span>',
    WALLET: '<span class="badge" style="background-color: rgba(76, 175, 80, 0.2); color: #4CAF50;">💰 Ví</span>'
  };
  return badges[method] || `<span class="badge">${method}</span>`;
}

function switchTab(tabName) {
  const adminTabs = document.querySelectorAll(".admin-tab");

  adminTabs.forEach(btn => {
    if (btn.getAttribute("data-tab") === tabName) {
      btn.classList.add("tab--active");
    } else {
      btn.classList.remove("tab--active");
    }
  });

  // Chuyển đổi nội dung bên dưới
  setHidden(els.tabProducts, tabName !== "products");
  setHidden(els.tabOrders, tabName !== "orders");
  setHidden(els.tabPayments, tabName !== "payments");
}

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
function escapeAttr(s) {
  return escapeHtml(s).replaceAll("`", "&#096;");
}
function formatPrice(price) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(price);
}
function cssEscape(s) {
  return CSS?.escape ? CSS.escape(String(s ?? "")) : String(s ?? "").replaceAll('"', "\\\"");
}

// ---------- UI handlers ----------
if (els.btnLogout) {
  els.btnLogout.onclick = async () => {
    await authService.logout();
  };
}

// --- LOGIC XỬ LÝ TAB & FORM MỚI ---
let isLoginMode = true;

if (els.tabModeLogin && els.tabModeRegister) {
  els.tabModeLogin.onclick = (ev) => {
    ev?.preventDefault?.();
    isLoginMode = true;
    els.tabModeLogin.classList.add("tab--active");
    els.tabModeRegister.classList.remove("tab--active");
    els.btnSubmitAuth.textContent = "Đăng nhập";
    setHidden(els.registerFields, true);
    setHidden(els.registerFieldsConfirm, true);
    if (els.verifyHint) els.verifyHint.textContent = "";
  };

  els.tabModeRegister.onclick = (ev) => {
    ev?.preventDefault?.();
    isLoginMode = false;
    els.tabModeRegister.classList.add("tab--active");
    els.tabModeLogin.classList.remove("tab--active");
    els.btnSubmitAuth.textContent = "Đăng ký";
    setHidden(els.registerFields, false);
    setHidden(els.registerFieldsConfirm, false);
    if (els.verifyHint) els.verifyHint.textContent = "";
  };
}

if (els.authForm) {
  els.authForm.onsubmit = async (ev) => {
    ev.preventDefault();
    const email = (els.email.value || "").trim();
    const password = els.password.value;

    if (!email || !password) return;

    if (!isLoginMode) {
      const fullName = (els.fullName.value || "").trim();
      const confirmPassword = els.confirmPassword.value;
      if (!fullName) { showError("❌ Vui lòng nhập họ và tên"); return; }
      if (password !== confirmPassword) { showError("❌ Mật khẩu không khớp!"); return; }

      try {
        els.btnSubmitAuth.disabled = true;
        await register(email, password, fullName);
        showSuccess("✅ Đăng ký thành công! Tài khoản admin đã được tạo.");
        els.email.value = "";
        els.password.value = "";
        els.fullName.value = "";
        els.confirmPassword.value = "";
        els.tabModeLogin.onclick(); // Chuyển về tab đăng nhập
      } catch (e) {
        showError("❌ Lỗi: " + (e?.message ?? String(e)));
      } finally {
        els.btnSubmitAuth.disabled = false;
      }
    } else {
      try {
        els.btnSubmitAuth.disabled = true;
        await authService.login(email, password);
      } catch (e) {
        showError("❌ Lỗi: " + (e?.message ?? String(e)));
      } finally {
        els.btnSubmitAuth.disabled = false;
      }
    }
  };
}

// Health/Seed section removed - use initOrdersTab for orders

if (els.tabButtons && els.tabButtons.length > 0) {
  els.tabButtons.forEach((b) => {
    b.onclick = () => switchTab(b.dataset.tab);
  });
}

if (els.btnReloadProducts) {
  els.btnReloadProducts.onclick = async () => {
    try {
      els.btnReloadProducts.disabled = true;
      await refreshProductsTable();
    } finally {
      els.btnReloadProducts.disabled = false;
    }
  };
}

if (els.btnReloadOrders) {
   els.btnReloadOrders.onclick = async () => {
     try {
       els.btnReloadOrders.disabled = true;
       console.log("🔄 Reloading orders...");
       initOrdersTab();
       showSuccess("✅ Đã tải lại danh sách đơn hàng");
     } finally {
       els.btnReloadOrders.disabled = false;
     }
   };
}

if (els.btnReloadPayments) {
  els.btnReloadPayments.onclick = async () => {
    try {
      els.btnReloadPayments.disabled = true;
      refreshPaymentsTable();
      showSuccess("🔄 Đã tải lại danh sách giao dịch");
    } finally {
      els.btnReloadPayments.disabled = false;
    }
  };
}

if (els.btnResetProduct) {
  els.btnResetProduct.onclick = () => {
    els.p_id.value = "";
    els.p_name.value = "";
    els.p_price.value = "";
    els.p_category.value = "";
    els.p_description.value = "";
    els.p_available.value = "true";
    els.p_imageUrl.value = "";
    els.p_imageUrl.dataset.path = "";
    if (els.p_imageFile) els.p_imageFile.value = "";
    if (els.uploadStatus) els.uploadStatus.textContent = "";
  };
}

if (els.btnUploadImage) {
  els.btnUploadImage.onclick = async () => {
     try {
       els.btnUploadImage.disabled = true;
       els.uploadStatus.textContent = "Đang upload...";

       const file = els.p_imageFile?.files?.[0];
       if (!file) throw new Error("Vui lòng chọn ảnh trước");

       // Use existing product id if provided; otherwise generate a temp id for storing.
       const productId = (els.p_id.value || "").trim() || (crypto?.randomUUID ? crypto.randomUUID() : String(Date.now()));
       if (!els.p_id.value.trim()) {
         // keep the generated id so next Create/Update writes the same document
         els.p_id.value = productId;
       }

       const { publicUrl, path } = await uploadProductImage({ file, productId });
       els.p_imageUrl.value = publicUrl;
       // keep for saving into Firestore
       els.p_imageUrl.dataset.path = path;
       els.uploadStatus.textContent = "✅ Upload OK";
       showSuccess("✅ Tải lên ảnh thành công");
     } catch (e) {
       els.uploadStatus.textContent = "❌ Upload lỗi";
       showError("❌ Lỗi tải lên: " + (e?.message ?? String(e)));
    } finally {
      els.btnUploadImage.disabled = false;
    }
  };
}

if (els.productForm) {
  els.productForm.onsubmit = async (ev) => {
    ev.preventDefault();
    const payload = {
      id: els.p_id.value.trim() || undefined,
      name: els.p_name.value.trim(),
      price: Number(els.p_price.value),
      category: els.p_category.value.trim(),
      description: els.p_description.value.trim(),
      available: els.p_available.value === "true",
      imageUrl: els.p_imageUrl.value.trim(),
      imagePath: (els.p_imageUrl.dataset.path || "").trim() || undefined,
    };
     try {
       await productsService.upsertProduct(payload);
       showSuccess("✅ Sản phẩm đã được lưu thành công");
       els.btnResetProduct.onclick();
       await refreshProductsTable();
     } catch (e) {
       showError("❌ Lỗi: " + (e?.message ?? String(e)));
    }
  };
}


// ---------- Auth state -> role gating ----------
authService.listen(async (user) => {
  clearLog();
  if (!user) {
    if (els.userInfo) els.userInfo.textContent = "Chưa đăng nhập";
    if (els.btnLogout) els.btnLogout.disabled = true;
    setHidden(els.cardLogin, false);
    setHidden(els.cardRole, true);
    setHidden(els.cardHealth, true);
    setHidden(els.cardAdmin, true);
    setHidden(els.cardNotAdmin, true);

    if (els.btnSendVerify) els.btnSendVerify.disabled = true;
    if (els.verifyHint) els.verifyHint.textContent = "";
    return;
  }

  if (els.btnLogout) els.btnLogout.disabled = false;
  if (els.userInfo) els.userInfo.textContent = `${user.email ?? "(no email)"}`;

  // DEV convenience: if this uid is allowlisted, promote role to ADMIN (merge).
  // This keeps the project smooth to demo without implementing invites yet.
  try {
    await bootstrapAdminIfAllowed({ uid: user.uid });
  } catch {
    // ignore bootstrap errors
  }

  const role = await authService.getUserRole(user.uid);

  // Hide/disable verify button to avoid confusion.
  if (els.btnSendVerify) els.btnSendVerify.disabled = true;
  if (els.verifyHint) els.verifyHint.textContent = "";

  setHidden(els.cardLogin, true);
  setHidden(els.cardRole, false);
  setHidden(els.cardHealth, true);

  // ✅ UI requirement: hide UID
  // Keep old behavior for later/reference.
  // els.kvUid.textContent = user.uid;
  if (els.kvUid) {
    els.kvUid.textContent = "";
    // Try to hide the whole UID row if markup supports it.
    const uidRow = els.kvUid.closest(".kv") || els.kvUid.closest(".row") || els.kvUid.parentElement;
    if (uidRow) uidRow.classList.add("hidden");
  }
  if (els.kvEmail) els.kvEmail.textContent = user.email ?? "";
  setRolePill(role);

  // Require ALL:
  // - Firestore role ADMIN (real security)
  // ✅ Requirement change: do NOT require email verification.
  // Keep old condition for later/reference.
  // if (user.emailVerified && role === ROLES.admin) {
   if (role === ROLES.admin) {
     setHidden(els.cardAdmin, false);
     setHidden(els.cardNotAdmin, true);
     setHidden(els.cardHealth, false);
     switchTab("products");
     await refreshProductsTable();
     initOrdersTab(); // 🔄 Use new orders UI
     refreshPaymentsTable(); // 🆕 Load payments

     // --- BỔ SUNG LOGIC ĐẾM SỐ LƯỢNG ĐƠN HÀNG (REALTIME) ---
          import("./services/ordersService.js").then(({ ordersService }) => {
            import("./admin-orders.js").then(({ listenOrders }) => {
               listenOrders(500, (orders) => {
                 const successCount = orders.filter(o => o.status === 'CONFIRMED' || o.status === 'DELIVERING' || o.status === 'DONE').length;
                 const cancelledCount = orders.filter(o => o.status === 'CANCELLED').length;

                 const statSuccessEl = document.getElementById("statSuccessOrders");
                 const statCancelEl = document.getElementById("statCancelledOrders");

                 if (statSuccessEl) statSuccessEl.textContent = successCount;
                 if (statCancelEl) statCancelEl.textContent = cancelledCount;
               });
            });
          });

  } else {
    setHidden(els.cardAdmin, true);
    setHidden(els.cardNotAdmin, false);
    setHidden(els.cardHealth, true);

    // Helpful hint: most blocks are from unverified email (ADMIN is auto-granted on web register)
    if (els.verifyHint) {
      if (role !== ROLES.admin) {
        els.verifyHint.textContent =
          "Bạn không có quyền ADMIN. Hãy kiểm tra Firestore: users/{uid}.role = ADMIN";
      } else {
        els.verifyHint.textContent = "";
      }
    }
  }
});

// ==================== AUTO-LOGIN with localStorage ====================

// Check if user should be auto-logged in
window.addEventListener("DOMContentLoaded", () => {
  const currentUser = auth.currentUser;

  // If already logged in via Firebase, no need to auto-login
  if (currentUser) {
    return;
  }

  // Check localStorage for saved login state
  if (shouldAutoLogin()) {
    const savedEmail = getSavedLoginEmail();
    if (savedEmail) {
      console.log("ℹ️ Thông tin đăng nhập được lưu. Hãy nhập mật khẩu để tiếp tục.");
      // Pre-fill email
      if (els.email) {
        els.email.value = savedEmail;
        els.email.focus();
      }
    }
  }
});

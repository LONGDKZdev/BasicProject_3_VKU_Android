import { ROLES } from "./services/constants.js";
import { authService } from "./services/authService.js";
import { productsService } from "./services/productsService.js";
import { ordersService } from "./services/ordersService.js";
import { uploadProductImage } from "./supabase.js";

const $ = (id) => document.getElementById(id);

const els = {
  // header
  userInfo: $("userInfo"),
  btnLogout: $("btnLogout"),
  // login
  cardLogin: $("cardLogin"),
  loginForm: $("loginForm"),
  email: $("email"),
  password: $("password"),
  btnSendVerify: $("btnSendVerify"),
  verifyHint: $("verifyHint"),
  btnToggleRegister: $("btnToggleRegister"),
  registerForm: $("registerForm"),
  adminInviteCode: $("adminInviteCode"),
  // role
  cardRole: $("cardRole"),
  kvUid: $("kvUid"),
  kvEmail: $("kvEmail"),
  kvRole: $("kvRole"),
  // health
  cardHealth: $("cardHealth"),
  btnRunHealth: $("btnRunHealth"),
  btnSeedProducts: $("btnSeedProducts"),
  btnClearLog: $("btnClearLog"),
  healthLog: $("healthLog"),
  // admin
  cardAdmin: $("cardAdmin"),
  cardNotAdmin: $("cardNotAdmin"),
  // tabs
  tabButtons: Array.from(document.querySelectorAll(".tab")),
  tabProducts: $("tab-products"),
  tabOrders: $("tab-orders"),
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
};

// --- Simple web-admin gate (local-only) ---
// This is NOT a replacement for Firestore security rules.
// It only prevents casual access to the admin UI.
const ADMIN_INVITE_CODE = "MIXUE-ADMIN-2026";
const ADMIN_GATE_KEY = "mixue_admin_gate_ok";

function isGatePassed() {
  return localStorage.getItem(ADMIN_GATE_KEY) === "1";
}

function setGatePassed(ok) {
  localStorage.setItem(ADMIN_GATE_KEY, ok ? "1" : "0");
}

function setHidden(el, hidden) {
  if (!el) return;
  el.classList.toggle("hidden", hidden);
}

function logLine(s) {
  els.healthLog.textContent += `${s}\n`;
  els.healthLog.scrollTop = els.healthLog.scrollHeight;
}

function clearLog() {
  els.healthLog.textContent = "";
}

function setRolePill(role) {
  els.kvRole.textContent = role ?? "(missing)";
  els.kvRole.style.borderColor = role === ROLES.admin ? "rgba(71,209,108,.6)" : "rgba(110,168,254,.55)";
}

async function refreshProductsTable() {
  const tbody = els.productsTable.querySelector("tbody");
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
      window.scrollTo({ top: 0, behavior: "smooth" });
    }

    if (act === "del") {
      if (!confirm(`Delete product ${id}?`)) return;
      await productsService.deleteProduct(id);
      await refreshProductsTable();
    }
  };
}

async function refreshOrdersTable() {
  const tbody = els.ordersTable.querySelector("tbody");
  tbody.innerHTML = "";

  const items = await ordersService.listOrders(50);
  for (const o of items) {
    const created = o.createdAt ? new Date(o.createdAt).toISOString() : "";
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><code>${escapeHtml(o.id ?? "")}</code></td>
      <td><code>${escapeHtml(o.userId ?? "")}</code></td>
      <td>${escapeHtml(String(o.totalPrice ?? ""))}</td>
      <td>
        <select data-act="status" data-id="${escapeAttr(o.id ?? "")}">
          ${["PENDING","CONFIRMED","DELIVERING","DONE","CANCELLED"].map((s) => `<option value="${s}" ${o.status===s?"selected":""}>${s}</option>`).join("")}
        </select>
      </td>
      <td>${escapeHtml(created)}</td>
      <td>
        <button class="btn btn--ghost" data-act="save" data-id="${escapeAttr(o.id ?? "")}">Save</button>
      </td>
    `;
    tbody.appendChild(tr);
  }

  tbody.onclick = async (ev) => {
    const btn = ev.target.closest("button");
    if (!btn) return;
    const act = btn.getAttribute("data-act");
    if (act !== "save") return;
    const id = btn.getAttribute("data-id");
    const sel = tbody.querySelector(`select[data-id="${cssEscape(id)}"]`);
    const status = sel?.value;
    if (!id || !status) return;
    await ordersService.setOrderStatus(id, status);
    alert("Updated order status");
  };
}

function switchTab(tabName) {
  for (const b of els.tabButtons) {
    b.classList.toggle("tab--active", b.dataset.tab === tabName);
  }
  setHidden(els.tabProducts, tabName !== "products");
  setHidden(els.tabOrders, tabName !== "orders");
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
function cssEscape(s) {
  return CSS?.escape ? CSS.escape(String(s ?? "")) : String(s ?? "").replaceAll('"', "\\\"");
}

// ---------- UI handlers ----------
els.btnLogout.onclick = async () => {
  await authService.logout();
};

els.loginForm.onsubmit = async (ev) => {
  ev.preventDefault();
  try {
    await authService.login(els.email.value.trim(), els.password.value);
  } catch (e) {
    alert(e?.message ?? String(e));
  }
};

els.btnSendVerify.onclick = async () => {
  try {
    els.btnSendVerify.disabled = true;
    await authService.sendVerificationEmail();
    alert("Đã gửi email xác minh. Hãy kiểm tra inbox/spam rồi đăng nhập lại.");
  } catch (e) {
    alert(e?.message ?? String(e));
  } finally {
    // will be re-enabled based on state
    els.btnSendVerify.disabled = false;
  }
};

els.btnToggleRegister.onclick = () => {
  const isHidden = els.registerForm.classList.contains("hidden");
  setHidden(els.registerForm, !isHidden);
  els.btnToggleRegister.textContent = isHidden ? "Ẩn" : "Nhập mã mời Admin";
};

els.registerForm.onsubmit = async (ev) => {
  ev.preventDefault();
  try {
    const code = (els.adminInviteCode.value || "").trim();
    if (code !== ADMIN_INVITE_CODE) throw new Error("Mã mời không đúng");
    setGatePassed(true);
    alert("✅ Xác thực admin OK. Trang sẽ tải lại để áp dụng quyền.");
    setHidden(els.registerForm, true);
    els.btnToggleRegister.textContent = "Nhập mã mời Admin";
    location.reload();
  } catch (e) {
    alert(e?.message ?? String(e));
  }
};

// Healthcheck/Seed are optional; not implemented in this minimal admin.
els.btnRunHealth.onclick = async () => {
  clearLog();
  logLine("Healthcheck: not implemented in web-admin yet.");
  logLine("Tip: Use Firebase Console or Android healthcheck logs.");
};
els.btnSeedProducts.onclick = async () => {
  clearLog();
  logLine("Seed: not implemented in web-admin yet.");
  logLine("Tip: Use Android debug seeder (FirestoreSampleDataSeeder) if needed.");
};
els.btnClearLog.onclick = clearLog;

els.tabButtons.forEach((b) => {
  b.onclick = () => switchTab(b.dataset.tab);
});

els.btnReloadProducts.onclick = async () => {
  try {
    els.btnReloadProducts.disabled = true;
    await refreshProductsTable();
  } finally {
    els.btnReloadProducts.disabled = false;
  }
};

els.btnReloadOrders.onclick = async () => {
  try {
    els.btnReloadOrders.disabled = true;
    await refreshOrdersTable();
  } finally {
    els.btnReloadOrders.disabled = false;
  }
};

els.btnResetProduct.onclick = () => {
  els.p_id.value = "";
  els.p_name.value = "";
  els.p_price.value = "";
  els.p_category.value = "";
  els.p_description.value = "";
  els.p_available.value = "true";
  els.p_imageUrl.value = "";
  if (els.p_imageFile) els.p_imageFile.value = "";
  if (els.uploadStatus) els.uploadStatus.textContent = "";
};

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

    const { publicUrl } = await uploadProductImage({ file, productId });
    els.p_imageUrl.value = publicUrl;
    els.uploadStatus.textContent = "✅ Upload OK";
  } catch (e) {
    els.uploadStatus.textContent = "❌ Upload lỗi";
    alert(e?.message ?? String(e));
  } finally {
    els.btnUploadImage.disabled = false;
  }
};

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
  };
  try {
    await productsService.upsertProduct(payload);
    els.btnResetProduct.onclick();
    await refreshProductsTable();
  } catch (e) {
    alert(e?.message ?? String(e));
  }
};

// ---------- Auth state -> role gating ----------
authService.listen(async (user) => {
  clearLog();
  if (!user) {
    els.userInfo.textContent = "Chưa đăng nhập";
    els.btnLogout.disabled = true;
    setHidden(els.cardLogin, false);
    setHidden(els.cardRole, true);
    setHidden(els.cardHealth, true);
    setHidden(els.cardAdmin, true);
    setHidden(els.cardNotAdmin, true);

    els.btnSendVerify.disabled = true;
    if (els.verifyHint) els.verifyHint.textContent = "";
    return;
  }

  els.btnLogout.disabled = false;
  els.userInfo.textContent = `${user.email ?? "(no email)"}`;

  const role = await authService.getUserRole(user.uid);

  // Email verification UI
  if (user.emailVerified) {
    els.btnSendVerify.disabled = true;
    if (els.verifyHint) els.verifyHint.textContent = "✅ Email đã xác minh";
  } else {
    els.btnSendVerify.disabled = false;
    if (els.verifyHint) {
      els.verifyHint.textContent = "⚠️ Email chưa xác minh. Hãy bấm 'Gửi email xác minh' rồi kiểm tra inbox/spam.";
    }
  }

  setHidden(els.cardLogin, true);
  setHidden(els.cardRole, false);
  setHidden(els.cardHealth, true);

  els.kvUid.textContent = user.uid;
  els.kvEmail.textContent = user.email ?? "";
  setRolePill(role);

  // Require ALL:
  // - Email verified (Firebase Auth)
  // - Firestore role ADMIN (real security)
  // - Local invite gate passed (extra simple protection)
  if (user.emailVerified && role === ROLES.admin && isGatePassed()) {
    setHidden(els.cardAdmin, false);
    setHidden(els.cardNotAdmin, true);
    setHidden(els.cardHealth, false);
    switchTab("products");
    await refreshProductsTable();
    await refreshOrdersTable();
  } else {
    setHidden(els.cardAdmin, true);
    setHidden(els.cardNotAdmin, false);
    setHidden(els.cardHealth, true);

    // Helpful hint for the most common reason
    if (els.verifyHint && role !== ROLES.admin) {
      els.verifyHint.textContent =
        (user.emailVerified ? "✅ Email đã xác minh. " : "⚠️ Email chưa xác minh. ") +
        "Tài khoản chưa có role ADMIN trong Firestore: users/{uid}.role = ADMIN";
    }
  }
});



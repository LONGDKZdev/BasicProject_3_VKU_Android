import { ROLES } from "./services/constants.js";
import { authService } from "./services/authService.js";
import { productsService } from "./services/productsService.js";
import { ordersService } from "./services/ordersService.js";
import { uploadProductImage } from "./supabase.js";
import { bootstrapAdminIfAllowed } from "./services/adminBootstrap.js";
import { seedAll } from "./services/seedService.js";

const $ = (id) => document.getElementById(id);

const els = {
  // header
  userInfo: $("userInfo"),
  btnLogout: $("btnLogout"),
  // login (ĐÃ CẬP NHẬT CÁC ID MỚI ĐỂ CHUYỂN TAB)
  cardLogin: $("cardLogin"),
  authForm: $("authForm"),
  email: $("email"),
  password: $("password"),
  btnSubmitAuth: $("btnSubmitAuth"),
  btnSendVerify: $("btnSendVerify"),
  verifyHint: $("verifyHint"),
  tabModeLogin: $("tabModeLogin"),
  tabModeRegister: $("tabModeRegister"),
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

// (ĐÃ XÓA: ADMIN_INVITE_CODE và các hàm isGatePassed, setGatePassed)

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

let unsubOrders = null; // Biến giữ kết nối Real-time để hủy khi cần

function refreshOrdersTable() {
  const tbody = els.ordersTable.querySelector("tbody");

  // Nếu đang có kết nối cũ thì hủy đi trước khi tạo mới
  if (unsubOrders) {
    unsubOrders();
  }

  // Khởi tạo luồng lắng nghe Real-time
  unsubOrders = ordersService.listenOrders(50, (items) => {
    tbody.innerHTML = ""; // Xóa bảng cũ

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
  });

  // Sự kiện bấm nút Save trạng thái
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

// --- LOGIC XỬ LÝ TAB & FORM MỚI ---
let isLoginMode = true;

if (els.tabModeLogin && els.tabModeRegister) {
  els.tabModeLogin.onclick = (ev) => {
    ev?.preventDefault?.();
    isLoginMode = true;
    els.tabModeLogin.classList.add("tab--active");
    els.tabModeRegister.classList.remove("tab--active");
    els.btnSubmitAuth.textContent = "Đăng nhập";
    if (els.verifyHint) els.verifyHint.textContent = "";
  };

  els.tabModeRegister.onclick = (ev) => {
    ev?.preventDefault?.();
    isLoginMode = false;
    els.tabModeRegister.classList.add("tab--active");
    els.tabModeLogin.classList.remove("tab--active");
    els.btnSubmitAuth.textContent = "Đăng ký";
    if (els.verifyHint) els.verifyHint.textContent = "";
  };
}

if (els.authForm) {
  els.authForm.onsubmit = async (ev) => {
    ev.preventDefault();
    const email = (els.email.value || "").trim();
    const password = els.password.value;
    if (!email || !password) return;
    try {
      // Prevent double-submit which can feel like the UI is frozen.
      els.btnSubmitAuth.disabled = true;
      if (isLoginMode) {
        await authService.login(email, password);
      } else {
        await authService.register(email, password);
        // ✅ Requirement change: do not require email verification.
        // Keep old message for later/reference.
        // alert(
        //   "Đăng ký thành công! Tài khoản admin đã được tạo, nhưng bạn cần XÁC MINH EMAIL trước khi vào Admin Console. Hãy kiểm tra Inbox/Spam."
        // );
        alert("Đăng ký thành công! Tài khoản admin đã được tạo.");
      }
    } catch (e) {
      alert(e?.message ?? String(e));
    } finally {
      els.btnSubmitAuth.disabled = false;
    }
  };
}

// Email verification disabled: button may be removed from HTML.
if (els.btnSendVerify) {
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
}

// Healthcheck/Seed are optional; not implemented in this minimal admin.
els.btnRunHealth.onclick = async () => {
  clearLog();
  logLine("Healthcheck: not implemented in web-admin yet.");
  logLine("Tip: Use Firebase Console or Android healthcheck logs.");
};
els.btnSeedProducts.onclick = async () => {
  clearLog();
  try {
    const u = authService.getAuthUser();
    if (!u) throw new Error("Chưa đăng nhập");
    logLine("[Seed] Running...");
    const res = await seedAll({ adminUid: u.uid });
    logLine(`[Seed] OK. products=${res.productsCount}, orderId=${res.orderId}`);
    await refreshProductsTable();
    await refreshOrdersTable();
  } catch (e) {
    logLine(`[Seed] FAILED: ${e?.message ?? String(e)}`);
    alert(e?.message ?? String(e));
  }
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
  els.p_imageUrl.dataset.path = "";
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

    const { publicUrl, path } = await uploadProductImage({ file, productId });
    els.p_imageUrl.value = publicUrl;
    // keep for saving into Firestore
    els.p_imageUrl.dataset.path = path;
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
    imagePath: (els.p_imageUrl.dataset.path || "").trim() || undefined,
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

    if (els.btnSendVerify) els.btnSendVerify.disabled = true;
    if (els.verifyHint) els.verifyHint.textContent = "";
    return;
  }

  els.btnLogout.disabled = false;
  els.userInfo.textContent = `${user.email ?? "(no email)"}`;

  // DEV convenience: if this uid is allowlisted, promote role to ADMIN (merge).
  // This keeps the project smooth to demo without implementing invites yet.
  try {
    await bootstrapAdminIfAllowed({ uid: user.uid });
  } catch {
    // ignore bootstrap errors
  }

  const role = await authService.getUserRole(user.uid);

  // ✅ Requirement change: email verification is not used.
  // Keep the old UI logic commented for later/reference.
  /*
  // Email verification UI
  if (user.emailVerified) {
    els.btnSendVerify.disabled = true;
    if (els.verifyHint) els.verifyHint.textContent = "Email đã xác minh.";
  } else {
    els.btnSendVerify.disabled = false;
    if (els.verifyHint) {
      els.verifyHint.textContent =
        "Bạn chưa xác minh email. Hãy bấm 'Gửi email xác minh' rồi kiểm tra Inbox/Spam. Sau khi xác minh, hãy đăng xuất/đăng nhập lại.";
    }
  }
  */
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
  els.kvEmail.textContent = user.email ?? "";
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
    await refreshOrdersTable();
  } else {
    setHidden(els.cardAdmin, true);
    setHidden(els.cardNotAdmin, false);
    setHidden(els.cardHealth, true);

    // Helpful hint: most blocks are from unverified email (ADMIN is auto-granted on web register)
    if (els.verifyHint) {
      // ✅ Requirement change: only role gating is relevant.
      // Keep old verification-related hints for later/reference.
      /*
      if (!user.emailVerified) {
        els.verifyHint.textContent =
          "Bạn bị chặn vì CHƯA XÁC MINH EMAIL. Hãy kiểm tra Inbox/Spam, bấm link xác minh, rồi đăng xuất/đăng nhập lại.";
      } else if (role !== ROLES.admin) {
        els.verifyHint.textContent =
          "Email đã xác minh, nhưng Firestore role chưa phải ADMIN. Hãy kiểm tra: users/{uid}.role = ADMIN";
      } else {
        // Edge-case: verified + role admin but still blocked due to stale client state
        els.verifyHint.textContent = "Nếu bạn vừa xác minh email, hãy thử đăng xuất/đăng nhập lại hoặc tải lại trang.";
      }
      */
      if (role !== ROLES.admin) {
        els.verifyHint.textContent =
          "Bạn không có quyền ADMIN. Hãy kiểm tra Firestore: users/{uid}.role = ADMIN";
      } else {
        els.verifyHint.textContent = "";
      }
    }
  }
});
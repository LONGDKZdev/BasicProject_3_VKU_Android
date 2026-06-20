import { collection, getDocs, addDoc, serverTimestamp } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js";
import { showSuccess, showError } from "./toast.js";
import { vouchersService } from "./services/vouchersService.js";

let products = [];
let localCart = [];
let appliedVoucher = null;
let discountAmount = 0;
const PRODUCT_CATEGORIES = ["Kem", "Trà sữa", "Trà trái cây", "Cà phê", "Nước", "Khác"];

const els = {
  grid: document.getElementById("posProductGrid"),
  search: document.getElementById("posSearch"),
  categoryButtons: Array.from(document.querySelectorAll("#posCategoryPills button")),
  cartList: document.getElementById("posCartList"),
  checkoutBtn: document.getElementById("posCheckoutBtn"),
  clearCartBtn: document.getElementById("posClearCartBtn"),
  reloadBtn: document.getElementById("posReloadBtn"),
  customerName: document.getElementById("posCustomerName"),
  orderNote: document.getElementById("posOrderNote"),
  voucherCode: document.getElementById("posVoucherCode"),
  applyVoucherBtn: document.getElementById("posApplyVoucherBtn"),
  clearVoucherBtn: document.getElementById("posClearVoucherBtn"),
  voucherStatus: document.getElementById("posVoucherStatus"),
  subtotal: document.getElementById("posSubtotal"),
  discount: document.getElementById("posDiscount"),
  total: document.getElementById("posTotal"),
  quickTotal: document.getElementById("posQuickTotal"),
  itemCount: document.getElementById("posItemCount"),
  productCount: document.getElementById("posProductCount"),
};

function formatPrice(price) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(Number(price || 0));
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function normalizeProductCategory(category) {
  const raw = String(category || "").trim();
  const found = PRODUCT_CATEGORIES.find((item) => item.toLocaleLowerCase("vi-VN") === raw.toLocaleLowerCase("vi-VN"));
  return found || raw || "Khác";
}

function getCartTotal() {
  return localCart.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0);
}

function getPayableTotal() {
  return Math.max(0, getCartTotal() - discountAmount);
}

function getCartCount() {
  return localCart.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
}

async function loadProducts() {
  try {
    if (els.grid) {
      els.grid.innerHTML = `<div class="empty-state"><span class="spinner-border spinner-border-sm"></span> Đang tải dữ liệu...</div>`;
    }

    const snap = await getDocs(collection(db, COLLECTIONS.products));
    products = snap.docs
      .map((d) => ({ id: d.id, ...d.data() }))
      .filter((p) => p.available !== false)
      .sort((a, b) => String(a.name || "").localeCompare(String(b.name || ""), "vi"));

    if (els.productCount) els.productCount.textContent = String(products.length);
    applyFilters();
  } catch (error) {
    console.error("Lỗi tải sản phẩm POS:", error);
    if (els.grid) {
      els.grid.innerHTML = `<div class="empty-state text-danger"><i class='bx bx-wifi-off'></i> Không thể tải dữ liệu sản phẩm</div>`;
    }
  }
}

function renderProducts(list) {
  if (!els.grid) return;
  els.grid.innerHTML = "";

  if (list.length === 0) {
    els.grid.innerHTML = `
      <div class="empty-state">
        <i class='bx bx-search-alt'></i>
        Không có sản phẩm phù hợp
      </div>
    `;
    return;
  }

  els.grid.innerHTML = list.map((p) => {
    const price = Number(p.price || 0);
    const imgUrl = p.imageUrl || "https://via.placeholder.com/400x300?text=Mixue";
    const category = normalizeProductCategory(p.category);

    return `
      <article class="product-card" onclick="window.addToCart('${escapeAttr(p.id)}')">
        <img src="${escapeAttr(imgUrl)}" alt="${escapeAttr(p.name || "Sản phẩm")}">
        <div class="product-body">
          <div class="product-name">${escapeHtml(p.name || "Sản phẩm")}</div>
          <div class="product-meta">
            <span class="category-badge">${escapeHtml(category)}</span>
            <span class="fw-bold text-danger">${formatPrice(price)}</span>
          </div>
        </div>
      </article>
    `;
  }).join("");
}

function escapeAttr(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}

let currentCategory = "all";
let searchQuery = "";

function applyFilters() {
  const filtered = products.filter((p) => {
    const name = String(p.name || "").toLocaleLowerCase("vi-VN");
    const description = String(p.description || "").toLocaleLowerCase("vi-VN");
    const matchSearch = !searchQuery || name.includes(searchQuery) || description.includes(searchQuery);
    const matchCat = currentCategory === "all" || normalizeProductCategory(p.category) === currentCategory;
    return matchSearch && matchCat;
  });
  renderProducts(filtered);
}

function setActiveCategory(button) {
  els.categoryButtons.forEach((b) => {
    b.classList.remove("btn-danger");
    b.classList.add("btn-outline-secondary");
  });
  button.classList.remove("btn-outline-secondary");
  button.classList.add("btn-danger");
}

els.search?.addEventListener("input", (e) => {
  searchQuery = e.target.value.trim().toLocaleLowerCase("vi-VN");
  applyFilters();
});

els.categoryButtons.forEach((btn) => {
  btn.addEventListener("click", (e) => {
    const target = e.target.closest("button");
    if (!target) return;
    setActiveCategory(target);
    currentCategory = target.getAttribute("data-cat") || "all";
    applyFilters();
  });
});

els.reloadBtn?.addEventListener("click", async () => {
  els.reloadBtn.disabled = true;
  await loadProducts();
  els.reloadBtn.disabled = false;
  showSuccess("Đã tải lại danh sách món");
});

els.clearCartBtn?.addEventListener("click", () => {
  if (localCart.length === 0) return;
  localCart = [];
  clearVoucher();
  updateCartUI();
});

els.applyVoucherBtn?.addEventListener("click", async () => {
  const code = els.voucherCode?.value.trim().toUpperCase() || "";
  if (!code) {
    setVoucherStatus("Vui lòng nhập mã giảm giá", true);
    return;
  }
  if (localCart.length === 0) {
    setVoucherStatus("Giỏ hàng đang trống", true);
    return;
  }

  try {
    els.applyVoucherBtn.disabled = true;
    const vouchers = await vouchersService.listVouchers();
    const voucher = vouchers.find((item) => String(item.code || "").toUpperCase() === code && item.active !== false);

    if (!voucher) {
      clearVoucher();
      setVoucherStatus("Mã giảm giá không tồn tại hoặc đã tạm tắt", true);
      return;
    }

    const subtotal = getCartTotal();
    const minOrderAmount = Number(voucher.minOrderAmount || 0);
    if (subtotal < minOrderAmount) {
      clearVoucher();
      setVoucherStatus(`Đơn tối thiểu để áp dụng mã là ${formatPrice(minOrderAmount)}`, true);
      return;
    }

    const percentDiscount = subtotal * Number(voucher.discountPercent || 0) / 100;
    const fixedDiscount = Number(voucher.discountAmount || 0);
    const calculatedDiscount = Math.min(subtotal, Math.max(0, percentDiscount + fixedDiscount));

    if (calculatedDiscount <= 0) {
      clearVoucher();
      setVoucherStatus("Mã này chưa có giá trị giảm hợp lệ", true);
      return;
    }

    appliedVoucher = voucher;
    discountAmount = calculatedDiscount;
    setVoucherStatus(`Đã áp dụng ${voucher.code}, giảm ${formatPrice(discountAmount)}`, false);
    els.clearVoucherBtn?.classList.remove("d-none");
    updateCartUI();
  } catch (error) {
    console.error("Lỗi áp dụng mã giảm giá POS:", error);
    setVoucherStatus("Không thể áp dụng mã giảm giá", true);
  } finally {
    els.applyVoucherBtn.disabled = false;
  }
});

els.clearVoucherBtn?.addEventListener("click", () => {
  clearVoucher();
  updateCartUI();
});

window.addToCart = function (id) {
  const prod = products.find((p) => p.id === id);
  if (!prod) return;

  const exist = localCart.find((item) => item.productId === id);
  if (exist) {
    exist.quantity += 1;
  } else {
    localCart.push({
      id,
      productId: id,
      productName: prod.name || "Sản phẩm",
      price: Number(prod.price || 0),
      quantity: 1,
      imageUrl: prod.imageUrl || "",
      size: "",
      sugarLevel: "",
      iceLevel: "",
      toppings: [],
      note: "",
    });
  }
  updateCartUI();
};

window.updateQty = function (id, delta) {
  const item = localCart.find((i) => i.productId === id);
  if (!item) return;

  item.quantity += delta;
  if (item.quantity <= 0) {
    localCart = localCart.filter((i) => i.productId !== id);
  }
  updateCartUI();
};

window.removeCartItem = function (id) {
  localCart = localCart.filter((i) => i.productId !== id);
  updateCartUI();
};

function updateCartUI() {
  const subtotal = getCartTotal();
  if (appliedVoucher) {
    const minOrderAmount = Number(appliedVoucher.minOrderAmount || 0);
    if (subtotal < minOrderAmount || subtotal <= 0) {
      clearVoucher();
    } else {
      const percentDiscount = subtotal * Number(appliedVoucher.discountPercent || 0) / 100;
      const fixedDiscount = Number(appliedVoucher.discountAmount || 0);
      discountAmount = Math.min(subtotal, Math.max(0, percentDiscount + fixedDiscount));
    }
  }

  const total = getPayableTotal();
  const count = getCartCount();

  if (els.total) els.total.innerText = formatPrice(total);
  if (els.subtotal) els.subtotal.innerText = formatPrice(subtotal);
  if (els.discount) els.discount.innerText = discountAmount > 0 ? `-${formatPrice(discountAmount)}` : formatPrice(0);
  if (els.quickTotal) els.quickTotal.innerText = formatPrice(total);
  if (els.itemCount) els.itemCount.textContent = String(count);
  if (els.checkoutBtn) els.checkoutBtn.disabled = localCart.length === 0;
  if (els.clearCartBtn) els.clearCartBtn.disabled = localCart.length === 0;

  if (!els.cartList) return;

  if (localCart.length === 0) {
    els.cartList.innerHTML = `
      <div class="empty-state">
        <i class='bx bx-cart'></i>
        Giỏ hàng trống
      </div>
    `;
    return;
  }

  els.cartList.innerHTML = localCart.map((item) => {
    const lineTotal = Number(item.price || 0) * Number(item.quantity || 0);
    return `
      <div class="cart-line">
        <div>
          <div class="fw-bold">${escapeHtml(item.productName)}</div>
          <div class="small text-muted">${formatPrice(item.price)} / món</div>
          <div class="fw-bold text-danger mt-1">${formatPrice(lineTotal)}</div>
        </div>
        <div>
          <div class="qty-box">
            <button class="btn btn-light btn-sm" onclick="window.updateQty('${escapeAttr(item.productId)}', -1)">-</button>
            <span class="fw-bold">${item.quantity}</span>
            <button class="btn btn-light btn-sm" onclick="window.updateQty('${escapeAttr(item.productId)}', 1)">+</button>
          </div>
          <button class="btn btn-sm btn-link text-danger mt-1 w-100" onclick="window.removeCartItem('${escapeAttr(item.productId)}')">Xóa</button>
        </div>
      </div>
    `;
  }).join("");
}

els.checkoutBtn?.addEventListener("click", async () => {
  const customerName = els.customerName?.value.trim() || "Khách Tại Quầy";
  const customerNote = els.orderNote?.value.trim() || "";
  const paymentMethod = document.querySelector("input[name='posPaymentMethod']:checked")?.value || "CASH";
  const btn = els.checkoutBtn;
  if (localCart.length === 0) return;

  const invoiceWindow = window.open("", "_blank");
  btn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Đang xử lý...`;
  btn.disabled = true;

  try {
    const subtotal = getCartTotal();
    const total = getPayableTotal();
    const items = localCart.map((item) => ({ ...item }));

    const orderRef = await addDoc(collection(db, COLLECTIONS.orders), {
      userId: "GUEST_POS",
      customerName,
      phoneNumber: "",
      address: "Mua tại quầy",
      paymentMethod,
      items,
      status: "DONE",
      subtotalPrice: subtotal,
      discountCode: appliedVoucher?.code || "",
      discountAmount,
      totalPrice: total,
      customerNote,
      createdAt: serverTimestamp(),
    });

    await addDoc(collection(db, COLLECTIONS.transactions), {
      orderId: orderRef.id,
      userId: "GUEST_POS",
      customerName,
      amount: total,
      paymentMethod,
      status: "SUCCESS",
      description: (paymentMethod === "BANK_TRANSFER" ? "Chuyển khoản tại quầy #" : "Thu tiền mặt tại quầy #") + orderRef.id,
      createdAt: serverTimestamp(),
    });

    showSuccess("✅ Đã thu tiền và lưu đơn thành công!");
    if (invoiceWindow) {
      invoiceWindow.location.href = `invoice.html?orderId=${orderRef.id}&print=1`;
    } else {
      showError("Trình duyệt đang chặn cửa sổ hóa đơn. Bạn có thể in lại trong tab Đơn Hàng.");
    }

    localCart = [];
    clearVoucher();
    if (els.customerName) els.customerName.value = "";
    if (els.orderNote) els.orderNote.value = "";
    updateCartUI();
  } catch (error) {
    console.error("Lỗi thanh toán POS:", error);
    if (invoiceWindow) invoiceWindow.close();
    showError("❌ Có lỗi xảy ra khi thanh toán!");
  } finally {
    btn.innerHTML = `<i class='bx bx-dollar-circle'></i> Lưu đơn & in hóa đơn`;
    updateCartUI();
  }
});

function setVoucherStatus(message, isError) {
  if (!els.voucherStatus) return;
  els.voucherStatus.textContent = message;
  els.voucherStatus.className = `small mt-2 ${isError ? "text-danger" : "text-success fw-bold"}`;
}

function clearVoucher() {
  appliedVoucher = null;
  discountAmount = 0;
  if (els.voucherCode) els.voucherCode.value = "";
  if (els.voucherStatus) {
    els.voucherStatus.textContent = "";
    els.voucherStatus.className = "small mt-2";
  }
  els.clearVoucherBtn?.classList.add("d-none");
}

updateCartUI();
loadProducts();

import { collection, getDocs, addDoc, serverTimestamp } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js";
import { showSuccess, showError } from "./toast.js";

let products = [];
let localCart = [];

function formatPrice(price) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price);
}

async function loadProducts() {
  try {
    const snap = await getDocs(collection(db, COLLECTIONS.products));
    products = snap.docs.map(d => ({ id: d.id, ...d.data() })).filter(p => p.available !== false);
    renderProducts(products);
  } catch (error) {
    document.getElementById("posProductGrid").innerHTML = `<div class="text-danger">Lỗi kết nối mạng!</div>`;
  }
}

function renderProducts(list) {
  const grid = document.getElementById("posProductGrid");
  grid.innerHTML = "";
  if (list.length === 0) return grid.innerHTML = `<div class="text-muted text-center mt-4">Trống!</div>`;

  list.forEach(p => {
    const price = Number(p.price || 0);
    const imgUrl = p.imageUrl || "https://via.placeholder.com/150";
    grid.innerHTML += `
      <div class="col-6 col-md-4 col-lg-3">
        <div class="card h-100 product-card shadow-sm" onclick="window.addToCart('${p.id}')">
          <img src="${imgUrl}" class="card-img-top" alt="${p.name}" style="height: 120px; object-fit: cover; border-radius: 12px 12px 0 0;">
          <div class="card-body p-2 text-center">
            <h6 class="card-title mb-1 fw-bold text-dark" style="font-size: 0.9rem;">${p.name}</h6>
            <span class="text-danger fw-bold small">${formatPrice(price)}</span>
          </div>
        </div>
      </div>
    `;
  });
}

// 🆕 Xử lý bộ lọc kết hợp (Từ khóa + Danh mục)
let currentCategory = "all";
let searchQuery = "";

function applyFilters() {
  const filtered = products.filter(p => {
    const matchSearch = p.name.toLowerCase().includes(searchQuery);
    const matchCat = currentCategory === "all" || p.category === currentCategory;
    return matchSearch && matchCat;
  });
  renderProducts(filtered);
}

document.getElementById("posSearch").addEventListener("input", (e) => {
  searchQuery = e.target.value.trim().toLowerCase();
  applyFilters();
});

document.querySelectorAll("#posCategoryPills button").forEach(btn => {
  btn.addEventListener("click", (e) => {
    // Đổi màu nút được chọn
    document.querySelectorAll("#posCategoryPills button").forEach(b => {
      b.classList.remove("btn-danger");
      b.classList.add("btn-outline-secondary");
    });
    e.target.classList.remove("btn-outline-secondary");
    e.target.classList.add("btn-danger");

    currentCategory = e.target.getAttribute("data-cat");
    applyFilters();
  });
});

window.addToCart = function(id) {
  const prod = products.find(p => p.id === id);
  if (!prod) return;
  const exist = localCart.find(item => item.productId === id);
  if (exist) exist.quantity += 1;
  else localCart.push({ productId: id, productName: prod.name, price: Number(prod.price || 0), quantity: 1, imageUrl: prod.imageUrl || "" });
  updateCartUI();
};

window.updateQty = function(id, delta) {
  const item = localCart.find(i => i.productId === id);
  if (item) {
    item.quantity += delta;
    if (item.quantity <= 0) localCart = localCart.filter(i => i.productId !== id);
  }
  updateCartUI();
};

function updateCartUI() {
  const list = document.getElementById("posCartList");
  if (localCart.length === 0) {
    list.innerHTML = `<div class="text-center text-muted mt-5">Giỏ hàng trống</div>`;
    document.getElementById("posTotal").innerText = formatPrice(0);
    checkValidation();
    return;
  }

  let total = 0;
  list.innerHTML = localCart.map(item => {
    total += item.price * item.quantity;
    return `
      <div class="d-flex justify-content-between align-items-center mb-2 bg-light p-2 rounded">
        <div style="flex: 1; padding-right: 10px;">
          <div class="fw-bold text-dark" style="font-size: 0.9rem;">${item.productName}</div>
          <div class="text-danger fw-bold" style="font-size: 0.8rem;">${formatPrice(item.price)}</div>
        </div>
        <div class="d-flex align-items-center gap-2 bg-white rounded shadow-sm border">
          <button class="btn btn-sm text-secondary fw-bold border-0" onclick="window.updateQty('${item.productId}', -1)">-</button>
          <span class="fw-bold px-1">${item.quantity}</span>
          <button class="btn btn-sm text-secondary fw-bold border-0" onclick="window.updateQty('${item.productId}', 1)">+</button>
        </div>
      </div>
    `;
  }).join("");
  document.getElementById("posTotal").innerText = formatPrice(total);
  checkValidation();
}

function checkValidation() {
  document.getElementById("posCheckoutBtn").disabled = localCart.length === 0;
}

document.getElementById("posCheckoutBtn").addEventListener("click", async () => {
  const customerName = document.getElementById("posCustomerName").value.trim() || "Khách Tại Quầy";
  const btn = document.getElementById("posCheckoutBtn");
  if (localCart.length === 0) return;

  btn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Đang xử lý...`;
  btn.disabled = true;

  try {
    const total = localCart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    // Lưu Đơn hàng: Đánh dấu là Tiền Mặt & Hoàn Thành
    const orderRef = await addDoc(collection(db, COLLECTIONS.orders), {
      userId: "GUEST_POS",
      customerName: customerName,
      phoneNumber: "",
      address: "Mua tại quầy",
      paymentMethod: "CASH",
      items: localCart,
      status: "DONE",
      totalPrice: total,
      createdAt: serverTimestamp()
    });

    // Lưu Giao dịch: Tiền mặt SUCCESS
    await addDoc(collection(db, COLLECTIONS.transactions), {
      orderId: orderRef.id,
      userId: "GUEST_POS",
      customerName: customerName,
      amount: total,
      paymentMethod: "CASH",
      status: "SUCCESS",
      description: "Thu tiền mặt tại quầy #" + orderRef.id,
      createdAt: serverTimestamp()
    });

    showSuccess("✅ Đã thu tiền thành công!");
    localCart = [];
    document.getElementById("posCustomerName").value = "";
    updateCartUI();
  } catch (error) {
    showError("❌ Có lỗi xảy ra khi thanh toán!");
  } finally {
    btn.innerHTML = `<i class='bx bx-dollar-circle'></i> Thu Tiền Mặt (Hoàn Thành)`;
    checkValidation();
  }
});

loadProducts();
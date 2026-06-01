# Web Admin Data Flow Diagram - Fixed Version

## Admin Login Flow (FIXED)

```
┌─────────────────────────────────────────────────────────────────┐
│ User clicks "Đăng nhập" in web-admin/index.html                │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ main.js: authService.listen() callback triggered               │
│ Checks: user exists + role === "ADMIN"                         │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ ✅ FIXED: switchTab("products")                                 │
│    - Changed selector: .nav-item → .tab                        │
│    - Now correctly finds all tab buttons with class="tab"      │
└──────────────────────┬──────────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   Products       Orders         Payments
   Tab (show)     Tab (hide)      Tab (hide)
        │
        ▼
┌─────────────────────────────────────────────────────────────────┐
│ main.js: await refreshProductsTable()                           │
│ Firestore: listenProducts() → Real-time listener                │
│ Display: Updates products table when data changes               │
└──────────────────┬──────────────────────────────────────────────┘
        │
        │ (PARALLEL)
        ▼
┌─────────────────────────────────────────────────────────────────┐
│ main.js: initOrdersTab()                                        │
│ ✅ FIXED: Only handles ALL orders (removed pending duplication)│
│                                                                 │
│ Flow:                                                           │
│ 1. Get ordersTable element from HTML ✓                         │
│ 2. Call ordersService.listenOrders(100, callback)              │
│ 3. Real-time listener connects to "orders" collection          │
│ 4. When data arrives: renderAllOrdersTable(data)               │
│ 5. Display with customerName (not userId)                      │
└──────────────────┬──────────────────────────────────────────────┘
        │
        │ (PARALLEL)
        ▼
┌─────────────────────────────────────────────────────────────────┐
│ main.js: refreshPaymentsTable()                                 │
│ Firestore: paymentsService.listenTransactions()                │
│ Display: Updates payments table with status/method badges      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Tab Button Interaction (FIXED)

**BEFORE** (Broken):
```javascript
❌ tabButtons: Array.from(document.querySelectorAll(".nav-item"))
   // Selector ".nav-item" doesn't match HTML buttons
   // Result: tabButtons array is empty []
   // Tab switch handlers never set up
   // Buttons appear but don't work
```

**AFTER** (Fixed):
```javascript
✅ tabButtons: Array.from(document.querySelectorAll(".tab"))
   // Correctly selects all buttons with class="tab"
   // HTML: <button class="tab" data-tab="orders">Orders</button>
   // Result: tabButtons array has 3 elements [products, orders, payments]
   
// Each button gets a click handler (line 310):
b.onclick = () => switchTab(b.dataset.tab)

// When user clicks "Orders" button:
// 1. Click event fires
// 2. switchTab("orders") called
// 3. All tabs hidden, orders tab shown
// 4. Real-time listener already active from initOrdersTab()
// 5. Order data displays immediately
```

---

## Real-Time Data Synchronization (FIXED)

### Android App → Firebase → Web Admin

```
┌─── Android App ───────────────────────────────────────────┐
│ User completes checkout                                   │
│ CartScreen.kt → OrderViewModel.createOrder()              │
│                                                           │
│ Data generated:                                           │
│ - customerId (UUID)                                       │
│ - customerName: "Nguyễn Văn A"  ← NEW FIELD             │
│ - items: [...]                                            │
│ - totalPrice: 250000                                      │
│ - status: "PENDING"                                       │
│ - createdAt: timestamp                                    │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
        ┌─────────────────────┐
        │ Firebase Firestore  │
        │                     │
        │ Collection:         │
        │ /orders/{orderID}   │
        │ - id: "abc123"      │
        │ - userId: "uid..."  │
        │ ✅ customerName:    │
        │    "Nguyễn Văn A"   │
        │ - items: [...]      │
        │ - totalPrice: 250k  │
        │ - status: PENDING   │
        └────────────┬────────┘
                     │
                     │ Real-time listener
                     │ (onSnapshot)
                     │
                     ▼
    ┌───────────── Web Admin ──────────────┐
    │ admin-orders.js                      │
    │ listenOrders(100) → onSnapshot()     │
    │                                      │
    │ Orders table updates automatically:  │
    │ ┌──────────────────────────────────┐ │
    │ │ ID    │ Khách hàng │ Total │Status│ │
    │ ├──────────────────────────────────┤ │
    │ │ abc123│Nguyễn Văn A│250.000│ ⏳  │ │ ← Shows customerName!
    │ │       │(not UUID!) │ VND   │Chờ  │ │
    │ └──────────────────────────────────┘ │
    └──────────────────────────────────────┘
```

### Also: Auto-Transaction Creation

```
When OrderViewModel.createOrder() succeeds:
    ↓
Automatically creates Transaction record:
    ↓
PaymentRepository.createTransaction()
    ↓
Firestore: /transactions/{txID}
    ├── userId: "uid..."
    ├── orderId: "abc123"
    ├── amount: 250000
    ├── paymentMethod: "CASH"
    ├── status: "SUCCESS"
    └── createdAt: timestamp
    ↓
Web Admin Payments tab listener
refreshPaymentsTable() → listenTransactions()
    ↓
Payments table updates with:
    ├── Transaction ID
    ├── Customer UID
    ├── Order ID
    ├── Method badge: 💵 Tiền Mặt
    ├── Status badge: ✅ Thành Công
    └── Timestamp
```

---

## Table Structure: Before vs After

### Orders Table (FIXED)

**BEFORE** (Confusing - two tables):
```
┌─ Tab: Orders ────────────────────────────────────────┐
│                                                      │
│ ⏳ Chờ Xác Thực                                      │
│ ┌──────────────────────────────────────────────┐    │
│ │ ID │ Khách hàng │ Total │ CreatedAt │ Actions│    │
│ ├──────────────────────────────────────────────┤    │
│ │    │            │       │           │        │    │ ← Pending only
│ └──────────────────────────────────────────────┘    │
│                                                      │
│ 📋 Tất Cả Đơn Hàng                                  │
│ ┌──────────────────────────────────────────────┐    │
│ │ ID │ Khách hàng │ UserId │ Total │ Status │ CreatedAt │ Actions│
│ ├──────────────────────────────────────────────┤    │
│ │    │            │        │       │        │           │        │  ← All orders
│ └──────────────────────────────────────────────┘    │
│                                                      │
│ Problems:                                            │
│ ✗ Duplicate data display                            │
│ ✗ Confusing for admin                               │
│ ✗ Takes up too much space                           │
└──────────────────────────────────────────────────────┘
```

**AFTER** (Clean - single source of truth):
```
┌─ Tab: Orders ────────────────────────────────────────┐
│                                                      │
│ 📋 Tất Cả Đơn Hàng                                  │
│ ┌──────────────────────────────────────────────┐    │
│ │ ID │Khách hàng│UserId│Total│Status│Created │ │
│ ├──────────────────────────────────────────────┤    │
│ │a11 │Nguyễn A  │uid..│250k │ ⏳ Chờ │12:30  │ │
│ ├──────────────────────────────────────────────┤    │
│ │a10 │Trần B    │uid..│150k │✅ Xác Thực│12:15 │ │    ✅ Shows customerName
│ ├──────────────────────────────────────────────┤    │    ✅ Single table
│ │ a9 │Lê C      │uid..│300k │🚚 Đang Giao│12:00 │ │  ✅ Clear status
│ └──────────────────────────────────────────────┘    │    ✅ Easy to read
│                                                      │
│ Benefits:                                            │
│ ✓ Single source of truth                            │
│ ✓ Shows customer names, not UIDs                    │
│ ✓ All statuses visible in one place                 │
│ ✓ Better use of screen space                        │
└──────────────────────────────────────────────────────┘
```

---

## File Changes Summary

### 1. web-admin/index.html
- **Removed**: Lines 172-189 (Pending Orders table section)
- **Kept**: All orders table with customerName column
- **Lines**: 180-187 (column headers including "Khách hàng")

### 2. web-admin/src/main.js
- **Fixed**: Line 39 - Tab selector `.nav-item` → `.tab`
- **Removed**: Lines 146-196 (old refreshOrdersTable function)
- **Updated**: Lines 325-336 (btnReloadOrders handler)
- **Added**: Toast message for reload confirmation
- **Result**: Clean, single-source-of-truth for orders UI

### 3. web-admin/src/orders-ui.js
- **Simplified**: initOrdersTab() function
- **Removed**: pendingOrdersTable handling
- **Kept**: Real-time listener via listenOrders()
- **Result**: Focus on all-orders with automatic updates

### 4. web-admin/src/firebase.js
- **Added**: `transactions: "transactions"` to COLLECTIONS
- **Result**: Consistent collection naming across app

---

## Testing Scenarios

### Scenario 1: Tab Navigation
```
1. Admin logs in ✓
2. "Products" tab shown automatically ✓
3. Click "Orders" button ✓
   → Orders tab appears ✓
   → All orders table visible ✓
4. Click "💳 Payments" button ✓
   → Payments tab appears ✓
   → Transactions table visible ✓
5. Click "Products" again ✓
   → Back to products view ✓
```

### Scenario 2: Order Creation Sync
```
1. Customer creates order on Android app ✓
2. Web admin Orders tab is open ✓
3. Within 1-2 seconds:
   → New order appears in table ✓
   → Shows customer name (not UID) ✓
   → Status shows "⏳ Chờ" ✓
   → Created time is accurate ✓
```

### Scenario 3: Real-Time Update
```
1. New order visible in table ✓
2. Admin changes status to "CONFIRMED" ✓
3. Table updates immediately:
   → Status badge changes to "✅ Xác Thực" ✓
   → List re-sorts if needed ✓
   → No page refresh required ✓
```

---

## Connection Status Indicators

### How to verify real-time listeners are working:

1. **Browser DevTools → Network → WS (WebSocket)**
   - Look for Firestore WebSocket connections
   - Should show persistent connections for cloud.firestore
   - Green status indicates active

2. **Browser Console Logs**
   - Watch for "🔄 Reloading orders..." message
   - No error messages about permissions
   - No "Unresolved reference" errors

3. **Visual Feedback**
   - Order appears within 2-3 seconds on web after Android creation
   - No manual reload button needed very often
   - Status changes reflected immediately

---

**Last Updated**: 2026-06-01  
**Architecture Version**: v2.0  
**Status**: ✅ All systems operational


import {
  collection,
  getDocs,
  limit,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
  doc,
  onSnapshot,
  where,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js";
import { ORDER_STATUS, TRANSACTION_STATUS, VALID_ORDER_STATUSES } from "./services/constants.js";

/**
 * Listen to all orders with real-time updates.
 */
export function listenOrders(max = 50, callback) {
  const q = query(
    collection(db, COLLECTIONS.orders),
    orderBy("createdAt", "desc"),
    limit(max)
  );

  return onSnapshot(q, (snap) => {
    const items = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
    callback(items);
  });
}

/**
 * Listen to pending orders.
 */
export function listenPendingOrders(callback) {
  const q = query(
    collection(db, COLLECTIONS.orders),
    orderBy("createdAt", "desc"),
    limit(100)
  );

  return onSnapshot(q, (snap) => {
    const items = snap.docs
      .map((d) => ({ id: d.id, ...d.data() }))
      .filter((order) => order.status === ORDER_STATUS.pending);

    callback(items);
  });
}

/**
 * Update order status.
 */
export async function setOrderStatus(orderId, status) {
  if (!orderId) throw new Error("orderId is required");

  const normalizedStatus = String(status || "").toUpperCase();
  if (!VALID_ORDER_STATUSES.includes(normalizedStatus)) {
    throw new Error(`Invalid order status: ${status}`);
  }

  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: normalizedStatus,
    updatedAt: serverTimestamp(),
  });
}

/**
 * Accept & confirm order.
 */
export async function confirmOrder(orderId) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: ORDER_STATUS.confirmed,
    confirmedAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });

  await updateTransactionsByOrderId(orderId, TRANSACTION_STATUS.success);
}

/**
 * Reject order.
 */
export async function rejectOrder(orderId, reason = "") {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: ORDER_STATUS.cancelled,
    rejectionReason: reason,
    updatedAt: serverTimestamp(),
  });

  await updateTransactionsByOrderId(orderId, TRANSACTION_STATUS.failed);
}

/**
 * Mark order as delivering.
 */
export async function markPrepared(orderId) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: ORDER_STATUS.delivering,
    preparedAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });

  await updateTransactionsByOrderId(orderId, TRANSACTION_STATUS.success);
}

/**
 * Update all transactions related to an order.
 */
async function updateTransactionsByOrderId(orderId, transactionStatus) {
  const q = query(
    collection(db, COLLECTIONS.transactions),
    where("orderId", "==", orderId)
  );

  const snap = await getDocs(q);

  const updates = snap.docs.map((d) =>
    updateDoc(doc(db, COLLECTIONS.transactions, d.id), {
      status: transactionStatus,
      updatedAt: serverTimestamp(),
    })
  );

  await Promise.all(updates);
}
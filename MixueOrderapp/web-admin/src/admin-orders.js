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
  where
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js";

/**
 * Listen to all orders with real-time updates
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
 * Listen to pending orders (Chờ xác thực) - Filter on client-side to avoid index
 */
export function listenPendingOrders(callback) {
  // Get all orders sorted by createdAt, then filter pending on client
  const q = query(
    collection(db, COLLECTIONS.orders),
    orderBy("createdAt", "desc"),
    limit(100) // Get more to ensure we get all pending
  );

  return onSnapshot(q, (snap) => {
    // Filter pending orders on client-side
    const items = snap.docs
      .map((d) => ({ id: d.id, ...d.data() }))
      .filter(order => order.status === "pending");
    callback(items);
  });
}

/**
 * Update order status
 */
export async function setOrderStatus(orderId, status) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status,
    updatedAt: serverTimestamp(),
  });
}

/**
 * Accept & confirm order (xác thực đơn hàng)
 */
export async function confirmOrder(orderId) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: "confirmed",
    confirmedAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });
}

/**
 * Reject order (từ chối đơn hàng)
 */
export async function rejectOrder(orderId, reason = "") {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: "cancelled",
    rejectionReason: reason,
    updatedAt: serverTimestamp(),
  });
}

/**
 * Mark order as prepared (đơn đã chuẩn bị)
 */
export async function markPrepared(orderId) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status: "preparing",
    preparedAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });
}


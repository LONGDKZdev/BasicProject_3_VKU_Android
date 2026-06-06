import {
  collection,
  getDocs,
  query,
  where,
  orderBy,
  limit,
  onSnapshot,
  doc,
  getDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "../firebase.js";

/**
 * Get all transactions for a user.
 */
export async function getUserTransactions(userId) {
  try {
    const q = query(
      collection(db, COLLECTIONS.transactions),
      where("userId", "==", userId),
      orderBy("createdAt", "desc"),
      limit(100)
    );

    const snapshot = await getDocs(q);
    return snapshot.docs.map((d) => ({ id: d.id, ...d.data() }));
  } catch (error) {
    console.error("Lỗi tải giao dịch:", error);
    return [];
  }
}

/**
 * Listen to all transactions in real-time.
 */
export function listenTransactions(callback) {
  const q = query(
    collection(db, COLLECTIONS.transactions),
    orderBy("createdAt", "desc"),
    limit(200)
  );

  return onSnapshot(q, (snap) => {
    const items = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
    callback(items);
  });
}

/**
 * Get transaction by ID.
 */
export async function getTransactionById(transactionId) {
  try {
    const snapshot = await getDoc(doc(db, COLLECTIONS.transactions, transactionId));

    if (snapshot.exists()) {
      return { id: snapshot.id, ...snapshot.data() };
    }

    return null;
  } catch (error) {
    console.error("Lỗi tải giao dịch:", error);
    return null;
  }
}

/**
 * Get transactions by order ID.
 */
export async function getTransactionsByOrderId(orderId) {
  try {
    const q = query(
      collection(db, COLLECTIONS.transactions),
      where("orderId", "==", orderId)
    );

    const snapshot = await getDocs(q);
    return snapshot.docs.map((d) => ({ id: d.id, ...d.data() }));
  } catch (error) {
    console.error("Lỗi tải giao dịch theo đơn hàng:", error);
    return [];
  }
}
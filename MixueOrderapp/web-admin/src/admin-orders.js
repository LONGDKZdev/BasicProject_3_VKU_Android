import {
  collection,
  getDocs,
  limit,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
  doc,
  onSnapshot
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js";

export function listenOrders(max = 50, callback) {
  const q = query(
    collection(db, COLLECTIONS.orders),
    orderBy("createdAt", "desc"),
    limit(max)
  );

  // onSnapshot sẽ tự động gọi lại callback mỗi khi có người đặt đơn
  return onSnapshot(q, (snap) => {
    const items = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
    callback(items);
  });
}

export async function setOrderStatus(orderId, status) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status,
    updatedAt: serverTimestamp(),
  });
}


import {
  collection,
  getDocs,
  limit,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
  doc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "./firebase.js";

export async function listOrders(max = 50) {
  const q = query(
    collection(db, COLLECTIONS.orders),
    orderBy("createdAt", "desc"),
    limit(max)
  );
  const snap = await getDocs(q);
  return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
}

export async function setOrderStatus(orderId, status) {
  await updateDoc(doc(db, COLLECTIONS.orders, orderId), {
    status,
    updatedAt: serverTimestamp(),
  });
}


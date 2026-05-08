import {
  collection,
  doc,
  getDocs,
  limit,
  orderBy,
  query,
  updateDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

import { db, COLLECTIONS } from "../firebase.js";

export const ordersService = {
  async listOrders(max = 50) {
    const q = query(
      collection(db, COLLECTIONS.orders),
      orderBy("createdAt", "desc"),
      limit(max)
    );
    const snap = await getDocs(q);
    return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
  },

  async setOrderStatus(orderId, status) {
    if (!orderId) throw new Error("OrderId is required");
    await updateDoc(doc(db, COLLECTIONS.orders, orderId), { status });
  },
};


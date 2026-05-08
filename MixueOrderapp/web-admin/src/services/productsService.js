import {
  collection,
  deleteDoc,
  doc,
  getDocs,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

import { db, COLLECTIONS } from "../firebase.js";

export const productsService = {
  async listProducts() {
    const q = query(collection(db, COLLECTIONS.products), orderBy("name"));
    const snap = await getDocs(q);
    return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
  },

  async upsertProduct(product) {
    const id = (product.id || "").trim() || (crypto?.randomUUID ? crypto.randomUUID() : String(Date.now()));
    const payload = {
      ...product,
      id,
      price: Number(product.price ?? 0),
      available: Boolean(product.available),
    };

    // Create if missing, update if exists (setDoc merge)
    await setDoc(
      doc(db, COLLECTIONS.products, id),
      {
        ...payload,
        updatedAt: serverTimestamp(),
        // createdAt only set if absent
        createdAt: payload.createdAt ?? serverTimestamp(),
      },
      { merge: true }
    );

    return id;
  },

  async deleteProduct(id) {
    await deleteDoc(doc(db, COLLECTIONS.products, id));
  },
};


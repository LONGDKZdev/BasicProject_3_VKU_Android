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
import { db, COLLECTIONS } from "./firebase.js";

export async function listProducts() {
  // orderBy requires an index if field missing sometimes; fallback if needed.
  const q = query(collection(db, COLLECTIONS.products), orderBy("name"));
  const snap = await getDocs(q);
  return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
}

export async function upsertProduct(product) {
  const id = (product.id || "").trim() || crypto.randomUUID();
  const payload = {
    ...product,
    id,
    price: Number(product.price ?? 0),
    available: Boolean(product.available),
    updatedAt: serverTimestamp(),
  };
  if (!payload.createdAt) payload.createdAt = serverTimestamp();

  await setDoc(doc(db, COLLECTIONS.products, id), payload, { merge: true });
  return id;
}

export async function deleteProduct(id) {
  await deleteDoc(doc(db, COLLECTIONS.products, id));
}


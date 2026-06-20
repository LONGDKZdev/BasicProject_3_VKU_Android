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

export const vouchersService = {
  async listVouchers() {
    const q = query(collection(db, COLLECTIONS.vouchers), orderBy("code"));
    const snap = await getDocs(q);
    return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
  },

  async upsertVoucher(voucher) {
    const code = String(voucher.code || "").trim().toUpperCase();
    if (!code) throw new Error("Vui lòng nhập mã voucher");

    const id = code;
    await setDoc(
      doc(db, COLLECTIONS.vouchers, id),
      {
        id,
        code,
        title: String(voucher.title || "").trim(),
        discountPercent: Number(voucher.discountPercent || 0),
        discountAmount: Number(voucher.discountAmount || 0),
        minOrderAmount: Number(voucher.minOrderAmount || 0),
        active: Boolean(voucher.active),
        updatedAt: serverTimestamp(),
        createdAt: voucher.createdAt ?? serverTimestamp(),
      },
      { merge: true }
    );
    return id;
  },

  async deleteVoucher(id) {
    await deleteDoc(doc(db, COLLECTIONS.vouchers, id));
  },
};


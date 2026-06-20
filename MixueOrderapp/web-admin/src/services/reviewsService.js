import {
  collection,
  doc,
  getDocs,
  orderBy,
  query,
  runTransaction,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

import { db, COLLECTIONS } from "../firebase.js";

export const reviewsService = {
  async listReviews() {
    const q = query(collection(db, COLLECTIONS.productReviews), orderBy("updatedAt", "desc"));
    const snap = await getDocs(q);
    return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
  },

  async deleteReview(review) {
    if (!review?.id || !review?.productId) throw new Error("Đánh giá không hợp lệ");

    const reviewRef = doc(db, COLLECTIONS.productReviews, review.id);
    const productRef = doc(db, COLLECTIONS.products, review.productId);

    await runTransaction(db, async (tx) => {
      const productSnap = await tx.get(productRef);
      const currentSum = Number(productSnap.data()?.ratingSum ?? 0);
      const currentCount = Number(productSnap.data()?.ratingCount ?? 0);
      const newCount = Math.max(0, currentCount - 1);
      const newSum = Math.max(0, currentSum - Number(review.rating ?? 0));
      const newAverage = newCount > 0 ? newSum / newCount : 0;

      tx.delete(reviewRef);
      tx.update(productRef, {
        ratingSum: newSum,
        ratingCount: newCount,
        ratingAverage: newAverage,
        ratingUpdatedAt: Date.now(),
      });
    });
  },
};

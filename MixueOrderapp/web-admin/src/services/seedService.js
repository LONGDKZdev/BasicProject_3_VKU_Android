import {
  collection,
  doc,
  serverTimestamp,
  setDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

import { db, COLLECTIONS } from "../firebase.js";

function vnd(n) {
  const x = Number(n);
  return Number.isFinite(x) ? x : 0;
}

/**
 * Create demo data for the school project.
 * Safe to run multiple times (fixed IDs + merge).
 */
export async function seedAll({ adminUid }) {
  if (!adminUid) throw new Error("adminUid is required");

  // ----- Products -----
  const products = [
    {
      id: "p_kem_vani",
      name: "Kem Vani",
      description: "Kem vani mát lạnh",
      category: "Kem",
      price: vnd(25000),
      available: true,
      imageUrl: "https://placehold.co/600x400/png",
      imagePath: "",
    },
    {
      id: "p_tra_sua_tc",
      name: "Trà sữa trân châu",
      description: "Trà sữa + trân châu",
      category: "Trà sữa",
      price: vnd(30000),
      available: true,
      imageUrl: "https://placehold.co/600x400/png",
      imagePath: "",
    },
    {
      id: "p_kem_oc_que",
      name: "Kem ốc quế",
      description: "Kem ốc quế Mixue",
      category: "Kem",
      price: vnd(10000),
      available: true,
      imageUrl:
        "https://vqupigbrkuucghnauwrb.supabase.co/storage/v1/object/public/StorageImage_MixueAndroid/products/ifoToN9pN1YZ28khZbew/main.jpg",
      imagePath: "products/ifoToN9pN1YZ28khZbew/main.jpg",
    },
  ];

  for (const p of products) {
    await setDoc(
      doc(db, COLLECTIONS.products, p.id),
      {
        ...p,
        createdBy: adminUid,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      },
      { merge: true }
    );
  }

  // ----- Orders (demo) -----
  // Firestore rules require: create order => signedIn && request.resource.data.userId == request.auth.uid
  // This means: whoever runs seedAll must be logged in as adminUid, so we can only create an order
  // with userId = adminUid here (simple demo order).

  const orderId = `demo_order_${Date.now()}`;
  await setDoc(
    doc(db, COLLECTIONS.orders, orderId),
    {
      userId: adminUid,
      status: "PENDING",
      totalPrice: products[0].price + products[2].price,
      items: [
        {
          productId: products[0].id,
          name: products[0].name,
          price: products[0].price,
          quantity: 1,
          imageUrl: products[0].imageUrl,
        },
        {
          productId: products[2].id,
          name: products[2].name,
          price: products[2].price,
          quantity: 1,
          imageUrl: products[2].imageUrl,
        },
      ],
      createdAt: serverTimestamp(),
      createdBy: adminUid,
    },
    { merge: true }
  );

  return { productsCount: products.length, orderId };
}


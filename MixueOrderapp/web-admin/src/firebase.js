// Firebase Web SDK (Modular v9+)
// Firebase chỉ dùng cho Auth và Firestore.
// Ảnh của dự án dùng Supabase Storage, không dùng Firebase Storage.

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

async function loadFirebaseConfig() {
  // No-PHP mode: reuse Android google-services.json as a static JSON file.
  // Khi chạy từ repository root, Android config nằm tại /app/google-services.json
  const res = await fetch("/app/google-services.json", { cache: "no-store" });

  if (!res.ok) {
    throw new Error(`Cannot load /app/google-services.json (HTTP ${res.status})`);
  }

  const gs = await res.json();

  const projectId = gs?.project_info?.project_id;
  const messagingSenderId = gs?.project_info?.project_number;
  const apiKey = gs?.client?.[0]?.api_key?.[0]?.current_key;

  if (!projectId || !apiKey) {
    throw new Error("google-services.json missing project_id/api_key");
  }

  return {
    apiKey,
    authDomain: `${projectId}.firebaseapp.com`,
    projectId,
    messagingSenderId,
    appId: "WEB_APP_ID_NOT_SET",
  };
}

export const firebaseConfig = await loadFirebaseConfig();

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);

export const COLLECTIONS = {
  users: "users",
  products: "products",
  orders: "orders",
  transactions: "transactions",
  healthcheck: "healthcheck",
};

export const ROLES = {
  admin: "ADMIN",
  user: "USER",
};
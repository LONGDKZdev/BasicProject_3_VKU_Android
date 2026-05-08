// Firebase Web SDK (Modular v9+)
// IMPORTANT: Replace firebaseConfig with your project's config:
// Firebase Console → Project settings → General → Your apps → Web app → Config

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { getStorage } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-storage.js";

async function loadFirebaseConfig() {
  // No-PHP mode: reuse Android google-services.json as a static JSON file.
  // This allows running via python http.server.
  // When served from repository root, the Android config is reachable at /app/google-services.json
  const res = await fetch("/app/google-services.json", { cache: "no-store" });
  if (!res.ok) {
    throw new Error(`Cannot load /app/google-services.json (HTTP ${res.status})`);
  }
  const gs = await res.json();

  const projectId = gs?.project_info?.project_id;
  const storageBucket = gs?.project_info?.storage_bucket;
  const messagingSenderId = gs?.project_info?.project_number;
  const apiKey = gs?.client?.[0]?.api_key?.[0]?.current_key;

  if (!projectId || !apiKey) {
    throw new Error("google-services.json missing project_id/api_key");
  }

  return {
    apiKey,
    authDomain: `${projectId}.firebaseapp.com`,
    projectId,
    storageBucket,
    messagingSenderId,
    // Web appId is optional for local dev; can be set later if you create a Web App in console.
    appId: "WEB_APP_ID_NOT_SET",
  };
}

export const firebaseConfig = await loadFirebaseConfig();

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

export const COLLECTIONS = {
  users: "users",
  products: "products",
  orders: "orders",
  healthcheck: "healthcheck",
};

export const ROLES = {
  admin: "ADMIN",
  user: "USER",
};


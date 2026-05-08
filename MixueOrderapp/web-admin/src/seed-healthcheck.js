import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  limit,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import {
  deleteObject,
  getDownloadURL,
  ref,
  uploadBytes,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-storage.js";
import { auth, db, storage, COLLECTIONS } from "./firebase.js";

function nowIso() {
  return new Date().toISOString();
}

export async function runHealthcheck(logger) {
  const user = auth.currentUser;
  if (!user) throw new Error("Not logged in");

  const log = (msg) => logger(`[${nowIso()}] ${msg}`);
  log("== HEALTHCHECK START ==");

  // ----- Firestore -----
  log("[Firestore] create doc...");
  const hcCol = collection(db, COLLECTIONS.healthcheck);
  const docRef = await addDoc(hcCol, {
    platform: "web-admin",
    uid: user.uid,
    status: "created",
    createdAt: serverTimestamp(),
  });
  log(`[Firestore] created: ${docRef.id}`);

  log("[Firestore] read back...");
  const snap1 = await getDoc(doc(db, COLLECTIONS.healthcheck, docRef.id));
  if (!snap1.exists()) throw new Error("Firestore read-back failed: doc missing");
  log(`[Firestore] read OK (status=${snap1.data().status})`);

  log("[Firestore] update...");
  await updateDoc(doc(db, COLLECTIONS.healthcheck, docRef.id), {
    status: "updated",
    updatedAt: serverTimestamp(),
  });
  log("[Firestore] update OK");

  log("[Firestore] query (platform=web-admin, limit 1)...");
  const q = query(
    collection(db, COLLECTIONS.healthcheck),
    where("platform", "==", "web-admin"),
    limit(1)
  );
  const qs = await getDocs(q);
  log(`[Firestore] query OK (found=${qs.size})`);

  log("[Firestore] delete...");
  await deleteDoc(doc(db, COLLECTIONS.healthcheck, docRef.id));
  log("[Firestore] delete OK");

  // ----- Storage -----
  log("[Storage] upload... (healthcheck/{uid}/web_admin_test.txt)");
  const content = `Mixue healthcheck OK\nuid=${user.uid}\ntime=${nowIso()}\n`;
  const bytes = new TextEncoder().encode(content);
  const fileRef = ref(storage, `healthcheck/${user.uid}/web_admin_test.txt`);
  await uploadBytes(fileRef, bytes, { contentType: "text/plain" });
  log("[Storage] upload OK");

  log("[Storage] getDownloadURL...");
  const url = await getDownloadURL(fileRef);
  log(`[Storage] url OK: ${url.substring(0, 60)}...`);

  log("[Storage] fetch content (optional verify)...");
  const fetched = await fetch(url);
  if (!fetched.ok) throw new Error(`Storage fetch failed: HTTP ${fetched.status}`);
  const text = await fetched.text();
  if (!text.includes("Mixue healthcheck OK")) {
    throw new Error("Storage fetch verify failed: unexpected content");
  }
  log("[Storage] fetch verify OK");

  log("[Storage] delete object...");
  await deleteObject(fileRef);
  log("[Storage] delete OK");

  log("== HEALTHCHECK PASS ==");
}

export async function seedProducts(logger) {
  const user = auth.currentUser;
  if (!user) throw new Error("Not logged in");

  const log = (msg) => logger(`[${nowIso()}] ${msg}`);

  const products = [
    {
      id: "p01",
      name: "Kem Vani",
      description: "Kem vani mát lạnh",
      price: 25000,
      category: "Kem",
      imageUrl: "https://placehold.co/600x400/png",
      available: true,
      createdAt: serverTimestamp(),
      createdBy: user.uid,
    },
    {
      id: "p02",
      name: "Trà sữa trân châu",
      description: "Trà sữa + trân châu",
      price: 30000,
      category: "Trà Sữa",
      imageUrl: "https://placehold.co/600x400/png",
      available: true,
      createdAt: serverTimestamp(),
      createdBy: user.uid,
    },
    {
      id: "p03",
      name: "Nước chanh",
      description: "Chanh tươi",
      price: 15000,
      category: "Nước",
      imageUrl: "https://placehold.co/600x400/png",
      available: true,
      createdAt: serverTimestamp(),
      createdBy: user.uid,
    },
  ];

  log("[Seed] upsert 3 products into Firestore collection 'products'...");
  for (const p of products) {
    await setDoc(doc(db, COLLECTIONS.products, p.id), p, { merge: true });
  }
  log("[Seed] DONE");
}


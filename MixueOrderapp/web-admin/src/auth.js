import {
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";
import {
  doc,
  getDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { auth, db, COLLECTIONS } from "./firebase.js";

export function listenAuth(callback) {
  return onAuthStateChanged(auth, callback);
}

export async function login(email, password) {
  const cred = await signInWithEmailAndPassword(auth, email, password);
  return cred.user;
}

export async function logout() {
  await signOut(auth);
}


export async function getUserRole(uid) {
  const snap = await getDoc(doc(db, COLLECTIONS.users, uid));
  if (!snap.exists()) return null;
  return snap.data()?.role ?? null;
}


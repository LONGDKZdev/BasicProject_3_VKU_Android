import {
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
  createUserWithEmailAndPassword,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";
import {
  doc,
  getDoc,
  setDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { auth, db, COLLECTIONS } from "./firebase.js";

/**
 * Listen to auth state changes
 */
export function listenAuth(callback) {
  return onAuthStateChanged(auth, callback);
}

/**
 * Login with email & password
 */
export async function login(email, password) {
  const cred = await signInWithEmailAndPassword(auth, email, password);
  // Save to localStorage for auto-login
  saveLoginState(email, true);
  return cred.user;
}

/**
 * Register new admin account
 */
export async function register(email, password, fullName) {
  const cred = await createUserWithEmailAndPassword(auth, email, password);
  const uid = cred.user.uid;


  await setDoc(doc(db, COLLECTIONS.users, uid), {
    id: uid,
    email: email,
    fullName: fullName,
    role: "USER",
    createdAt: new Date(),
  });

  saveLoginState(email, true);
  return cred.user;
}

/**
 * Logout
 */
export async function logout() {
  await signOut(auth);
  // Clear localStorage
  saveLoginState("", false);
}

/**
 * Get user role from Firestore
 */
export async function getUserRole(uid) {
  const snap = await getDoc(doc(db, COLLECTIONS.users, uid));
  if (!snap.exists()) return null;
  return snap.data()?.role ?? null;
}

/**
 * Save login state to localStorage for auto-login
 */
function saveLoginState(email, isLoggedIn) {
  if (isLoggedIn) {
    localStorage.setItem("mixue_admin_email", email);
    localStorage.setItem("mixue_admin_logged_in", "true");
  } else {
    localStorage.removeItem("mixue_admin_email");
    localStorage.removeItem("mixue_admin_logged_in");
  }
}

/**
 * Get saved login state
 */
export function getSavedLoginEmail() {
  return localStorage.getItem("mixue_admin_email") || null;
}

/**
 * Check if should auto-login
 */
export function shouldAutoLogin() {
  return localStorage.getItem("mixue_admin_logged_in") === "true";
}


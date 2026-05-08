import {
  onAuthStateChanged,
  sendEmailVerification,
  signInWithEmailAndPassword,
  signOut,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";

import {
  doc,
  getDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

import { auth, db, COLLECTIONS, ROLES } from "../firebase.js";

export const authService = {
  listen(callback) {
    // Keep UI layer contract: callback(user|null)
    return onAuthStateChanged(auth, (u) => {
      callback(
        u
          ? {
              uid: u.uid,
              email: u.email ?? "",
              emailVerified: Boolean(u.emailVerified),
            }
          : null
      );
    });
  },

  getAuthUser() {
    return auth.currentUser;
  },

  async login(email, _password) {
    const credential = await signInWithEmailAndPassword(auth, email, _password);
    const u = credential.user;
    return {
      uid: u.uid,
      email: u.email ?? email,
    };
  },

  async logout() {
    await signOut(auth);
  },

  async sendVerificationEmail() {
    const u = auth.currentUser;
    if (!u) throw new Error("Chưa đăng nhập");
    await sendEmailVerification(u);
  },

  async getUserRole(uid) {
    if (!uid) return ROLES.user;
    const snap = await getDoc(doc(db, COLLECTIONS.users, uid));
    const role = snap.exists() ? snap.data()?.role : null;
    return role === ROLES.admin ? ROLES.admin : ROLES.user;
  },
};


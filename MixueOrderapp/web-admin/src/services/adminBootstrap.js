import {
  doc,
  serverTimestamp,
  setDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

import { db, COLLECTIONS, ROLES } from "../firebase.js";

/**
 * DEV-ONLY: Bootstrap admin role by UID.
 *
 * Why:
 * - Firestore rules require users/{uid}.role == 'ADMIN' for product/order writes.
 * - For a small school project, we keep this simple so the system can run end-to-end.
 *
 * How to use:
 * 1) Login once to get your uid (UI shows it).
 * 2) Put the uid into ADMIN_UID_ALLOWLIST below.
 * 3) Reload + login again -> your users/{uid} doc will be upserted with role ADMIN.
 */

// TODO (later): replace this with invite codes / custom claims.
export const ADMIN_UID_ALLOWLIST = [
  "nJjzh0VY85aDZpw9Z1SIbLL0tKA2",
];

export async function bootstrapAdminIfAllowed(user) {
  if (!user?.uid) return false;
  if (!ADMIN_UID_ALLOWLIST.includes(user.uid)) return false;

  await setDoc(
    doc(db, COLLECTIONS.users, user.uid),
    {
      role: ROLES.admin,
      adminBootstrappedAt: serverTimestamp(),
    },
    { merge: true }
  );
  return true;
}


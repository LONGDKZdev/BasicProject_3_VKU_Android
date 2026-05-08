import { ROLES } from "../services/constants.js";

// UI-only skeleton session.
// You can later replace this with real authentication.
export const mockSession = {
  user: null,
  // simple in-memory role mapping by uid
  rolesByUid: {},

  login(email) {
    const uid = crypto?.randomUUID ? crypto.randomUUID() : String(Date.now());
    this.user = { uid, email };
    // default every login as ADMIN for UI testing
    this.rolesByUid[uid] = ROLES.admin;
    return this.user;
  },

  logout() {
    this.user = null;
  },

  getRole(uid) {
    return this.rolesByUid[uid] ?? null;
  },
};


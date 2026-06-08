import { doc, setDoc, serverTimestamp } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";
import { db, COLLECTIONS } from "../firebase.js";

/**
 * Hàm tạo dữ liệu mẫu (Seed Data)
 * Đã được vô hiệu hóa tạm thời để tránh ghi đè dữ liệu thật của hệ thống.
 */
export async function seedAll(adminUid) {
  console.log("Tính năng tạo dữ liệu mẫu (Seed) đã được tắt để bảo vệ dữ liệu thật.");
  return true;
}
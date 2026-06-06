// Các hằng số dùng chung cho Web Admin
export const ORDER_STATUS = {
  pending: "PENDING",
  confirmed: "CONFIRMED",
  delivering: "DELIVERING",
  cancelled: "CANCELLED",
  done: "DONE",
};

export const TRANSACTION_STATUS = {
  pending: "PENDING",
  success: "SUCCESS",
  failed: "FAILED",
};

export const VALID_ORDER_STATUSES = Object.values(ORDER_STATUS);

export const ROLES = {
  admin: "ADMIN",
  user: "USER",
};
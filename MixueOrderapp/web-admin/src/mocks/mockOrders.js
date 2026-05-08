export const mockOrders = [
  {
    id: "o001",
    userId: "u001",
    totalPrice: 55000,
    status: "PENDING",
    createdAt: Date.now() - 1000 * 60 * 60 * 3,
  },
  {
    id: "o002",
    userId: "u002",
    totalPrice: 30000,
    status: "CONFIRMED",
    createdAt: Date.now() - 1000 * 60 * 60 * 26,
  },
];


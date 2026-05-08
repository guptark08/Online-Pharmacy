export const APP_NAME = "PulseMeds Pharmacy";

export const DELIVERY_SLOTS = [
  "9AM-11AM",
  "10AM-12PM",
  "2PM-4PM",
  "4PM-6PM",
  "6PM-8PM",
];

export const PAYMENT_METHODS = [
  "UPI",
  "CARD",
  "COD",
  "NET_BANKING",
];

export const ROLE_OPTIONS = [
  "CUSTOMER",
  "ADMIN",
  "PHARMACIST",
  "DELIVERY_AGENT",
];

export const ORDER_STATUS_OPTIONS = [
  "PRESCRIPTION_PENDING",
  "PRESCRIPTION_APPROVED",
  "PRESCRIPTION_REJECTED",
  "PAYMENT_PENDING",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "ADMIN_CANCELLED",
  "REFUND_INITIATED",
  "REFUND_COMPLETED",
];

export const STATUS_THEMES = {
  APPROVED: "success",
  PRESCRIPTION_APPROVED: "success",
  PAID: "success",
  DELIVERED: "success",
  ACTIVE: "success",
  INACTIVE: "danger",
  PENDING: "warning",
  PRESCRIPTION_PENDING: "warning",
  PRESCRIPTION_REQUIRED: "warning",
  PAYMENT_PENDING: "warning",
  PACKED: "info",
  OUT_FOR_DELIVERY: "info",
  REJECTED: "danger",
  PRESCRIPTION_REJECTED: "danger",
  CUSTOMER_CANCELLED: "danger",
  ADMIN_CANCELLED: "danger",
  PAYMENT_FAILED: "danger",
  REFUND_INITIATED: "muted",
  REFUND_COMPLETED: "muted",
  LOW_STOCK: "warning",
};

export const ACCOUNT_NAV_LINKS = [
  { to: "/app/account/profile", label: "Profile" },
  { to: "/app/account/prescriptions", label: "Prescriptions" },
  { to: "/app/account/orders", label: "Order History" },
];

export const ADMIN_NAV_LINKS = [
  { to: "/app/admin/prescriptions", label: "Prescription Review" },
  { to: "/app/admin/dashboard", label: "Dashboard" },
  { to: "/app/admin/orders", label: "Orders" },
  { to: "/app/admin/users", label: "Users" },
  { to: "/app/admin/reports", label: "Reports" },
];

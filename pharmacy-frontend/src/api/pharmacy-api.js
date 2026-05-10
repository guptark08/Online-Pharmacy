import { apiBlobRequest, apiRequest } from "./client.js";

export function normalizeUser(user = {}) {
  if (!user) return null;

  return {
    id: user.id ?? user.userId ?? null,
    name: user.name ?? "",
    email: user.email ?? "",
    mobile: user.mobile ?? "",
    address: user.address ?? user.addrsss ?? "",
    role: user.role ?? "CUSTOMER",
    active: user.active ?? user.isActive ?? true,
  };
}

export async function login(credentials) {
  const session = await apiRequest("/api/auth/login", {
    method: "POST",
    body: credentials,
  });
  return loadUserFromSession(session);
}

export async function signup(profile) {
  const session = await apiRequest("/api/auth/signup", {
    method: "POST",
    body: profile,
  });
  return loadUserFromSession(session);
}

async function loadUserFromSession(session) {
  if (!session?.token) return { token: "", user: null };

  try {
    const profile = await apiRequest("/api/auth/me", { token: session.token });
    return { token: session.token, user: normalizeUser(profile) };
  } catch {
    return { token: session.token, user: normalizeUser(session) };
  }
}

export const getCategories = () => apiRequest("/api/catalog/categories");

export const getMedicines = (filters = {}) =>
  apiRequest("/api/catalog/medicines", {
    query: {
      name: filters.search || undefined,
      categoryId: filters.categoryId || undefined,
      requiresPrescription: filters.prescriptionOnly || undefined,
      page: filters.page ?? 0,
      size: filters.size ?? 12,
      sortBy: "name",
      sortDir: "asc",
    },
  });

export const getMedicine = (id) => apiRequest(`/api/catalog/medicines/${id}`);

export const getCart = (token) => apiRequest("/api/orders/cart", { token });

export const cartItemFromMedicine = (medicine, quantity = 1) => ({
  medicineId: medicine.id,
  medicineName: medicine.name,
  price: medicine.price,
  quantity,
  requiresPrescription: Boolean(medicine.requiresPrescription),
});

export const addCartItem = (token, item) =>
  apiRequest("/api/orders/cart/items", {
    method: "POST",
    token,
    body: item,
  });

export const updateCartItem = (token, itemId, quantity) =>
  apiRequest(`/api/orders/cart/items/${itemId}`, {
    method: "PUT",
    token,
    query: { quantity },
  });

export const removeCartItem = (token, itemId) =>
  apiRequest(`/api/orders/cart/items/${itemId}`, {
    method: "DELETE",
    token,
  });

export const clearCart = (token) =>
  apiRequest("/api/orders/cart", {
    method: "DELETE",
    token,
  });

export const normalizeAddress = (address = {}) => ({
  ...address,
  recipientName: address.recipientName ?? address.fullName ?? "",
  line1: address.line1 ?? address.addressLine1 ?? "",
  line2: address.line2 ?? address.addressLine2 ?? "",
  isDefault: address.isDefault ?? address.default ?? false,
});

export const addressRequest = (address = {}) => ({
  fullName: address.fullName ?? address.recipientName,
  mobile: address.mobile,
  addressLine1: address.addressLine1 ?? address.line1,
  addressLine2: address.addressLine2 ?? address.line2 ?? "",
  city: address.city,
  state: address.state,
  pincode: address.pincode,
  default: Boolean(address.isDefault ?? address.default),
});

export const getAddresses = async (token) => {
  const addresses = await apiRequest("/api/orders/addresses", { token });
  return Array.isArray(addresses) ? addresses.map(normalizeAddress) : [];
};

export const saveAddress = (token, address) =>
  apiRequest("/api/orders/addresses", {
    method: "POST",
    token,
    body: addressRequest(address),
  }).then(normalizeAddress);

export const getOrders = (token) => apiRequest("/api/orders", { token });

export const getOrder = (token, id) => apiRequest(`/api/orders/${id}`, { token });

export const startCheckout = (token, payload) =>
  apiRequest("/api/orders/checkout/start", {
    method: "POST",
    token,
    body: payload,
  });

export const payForOrder = (token, orderId, paymentMethod) =>
  apiRequest(`/api/orders/${orderId}/payment`, {
    method: "POST",
    token,
    body: {
      paymentMethod,
      transactionReference: `WEB-${Date.now()}`,
    },
  });

export const cancelOrder = (token, orderId) =>
  apiRequest(`/api/orders/${orderId}/cancel`, {
    method: "POST",
    token,
  });

export const getPrescriptions = (token) =>
  apiRequest("/api/catalog/prescriptions/my", { token });

export const uploadPrescription = (token, file) => {
  const formData = new FormData();
  formData.append("file", file);

  return apiRequest("/api/catalog/prescriptions/upload", {
    method: "POST",
    token,
    body: formData,
  });
};

export const getPrescriptionFile = (token, prescriptionId) =>
  apiBlobRequest(`/api/catalog/prescriptions/${prescriptionId}/file`, { token });

export const getAdminDashboard = (token) => apiRequest("/api/admin/dashboard", { token });
export const getAdminOrders = (token) => apiRequest("/api/admin/orders", { token });
export const getUsers = (token) => apiRequest("/api/admin/users", { token });
export const getSalesReport = (token) => apiRequest("/api/admin/reports/sales", { token });
export const getInventoryReport = (token) =>
  apiRequest("/api/admin/reports/inventory", { token });

export const updateAdminOrderStatus = (token, orderId, status) =>
  apiRequest(`/api/admin/orders/${orderId}/status`, {
    method: "PUT",
    token,
    body: { status },
  });

export const updateUser = (token, userId, data) =>
  apiRequest(`/api/admin/users/${userId}`, {
    method: "PUT",
    token,
    body: data,
  });

export const saveMedicine = (token, medicineId, medicine) =>
  apiRequest(medicineId ? `/api/catalog/medicines/${medicineId}` : "/api/catalog/medicines", {
    method: medicineId ? "PUT" : "POST",
    token,
    body: medicine,
  });

export const deleteMedicine = (token, medicineId) =>
  apiRequest(`/api/catalog/medicines/${medicineId}`, {
    method: "DELETE",
    token,
  });

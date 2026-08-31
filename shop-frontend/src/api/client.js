const API_BASE = import.meta.env.VITE_API_URL;
function getToken() {
  return localStorage.getItem("token");
}

async function request(path, { method = "GET", body, auth = true } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (auth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const message = await res.text().catch(() => res.statusText);
    throw new Error(message || `Request failed: ${res.status}`);
  }

  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  register: (data) => request("/auth/register", { method: "POST", body: data, auth: false }),
  login: (data) => request("/auth/login", { method: "POST", body: data, auth: false }),
  getItems: (page = 0, size = 12, q = "") => {
    const params = new URLSearchParams({ page, size });
    if (q) params.set("q", q);
    return request(`/items?${params.toString()}`);
  },
  createOrder: (data) => request("/orders", { method: "POST", body: data }),
  getMyOrders: () => request("/orders"),

  adminGetOrders: () => request("/orders/all"),
  adminUpdateOrderStatus: (id, status) => request(`/orders/${id}/status`, { method: "PATCH", body: { status } }),
  adminCreateItem: (data) => request("/items", { method: "POST", body: data }),
  adminUpdateItem: (id, data) => request(`/items/${id}`, { method: "PUT", body: data }),
  adminDeleteItem: (id) => request(`/items/${id}`, { method: "DELETE" }),
  adminGetUsers: () => request("/users"),
  getCart: () => request("/cart"),
  addCartItem: (variantId, quantity) => request("/cart/items", { method: "POST", body: { variantId, quantity } }),
  updateCartItem: (variantId, quantity) => request(`/cart/items/${variantId}`, { method: "PATCH", body: { quantity } }),
  removeCartItem: (variantId) => request(`/cart/items/${variantId}`, { method: "DELETE" }),
  clearCart: () => request("/cart", { method: "DELETE" }),
  checkout: () => request("/orders", { method: "POST" }),
  getCharacteristicTypes: () => request("/characteristics"),
  createCharacteristicType: (name) => request("/characteristics", { method: "POST", body: { name } }),
  deleteCharacteristicType: (id) => request(`/characteristics/${id}`, { method: "DELETE" }),
  addCharacteristicValue: (typeId, value) => request(`/characteristics/${typeId}/values`, { method: "POST", body: { value } }),
  deleteCharacteristicValue: (valueId) => request(`/characteristics/values/${valueId}`, { method: "DELETE" }),

  adminReplaceVariants: (itemId, variants) => request(`/items/${itemId}/variants`, { method: "PUT", body: { variants } }),
};
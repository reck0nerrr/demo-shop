const API_BASE = "http://localhost:8080/api";

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
  getItems: () => request("/items"),
  createOrder: (data) => request("/orders", { method: "POST", body: data }),
  getMyOrders: () => request("/orders"),

  adminGetOrders: () => request("/orders/all"),
  adminUpdateOrderStatus: (id, status) => request(`/orders/${id}/status`, { method: "PATCH", body: { status } }),
  adminCreateItem: (data) => request("/items", { method: "POST", body: data }),
  adminUpdateItem: (id, data) => request(`/items/${id}`, { method: "PUT", body: data }),
  adminDeleteItem: (id) => request(`/items/${id}`, { method: "DELETE" }),
  adminGetUsers: () => request("/users"),
};
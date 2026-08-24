import { useEffect, useState } from "react";
import { api } from "../api/client";

const TABS = ["items", "orders", "users"];
const STATUSES = ["PENDING", "PAID", "SHIPPED", "CANCELLED"];

export default function Admin() {
  const [tab, setTab] = useState("items");

  return (
    <div className="admin-page">
      <div className="tab-bar">
        {TABS.map((t) => (
          <button
            key={t}
            className={tab === t ? "tab tab-active" : "tab"}
            onClick={() => setTab(t)}
          >
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>
      {tab === "items" && <ItemsAdmin />}
      {tab === "orders" && <OrdersAdmin />}
      {tab === "users" && <UsersAdmin />}
    </div>
  );
}

function ItemsAdmin() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const emptyForm = { name: "", description: "", price: "", stockQuantity: "", imageUrl: "" };
  const [form, setForm] = useState(emptyForm);

  function load() {
    setLoading(true);
    api.getItems().then(setItems).catch(() => setError("Couldn't load items")).finally(() => setLoading(false));
  }

  useEffect(load, []);

  function startCreate() {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  }

  function startEdit(item) {
    setEditingId(item.id);
    setForm({
      name: item.name,
      description: item.description || "",
      price: item.price,
      stockQuantity: item.stockQuantity,
      imageUrl: item.imageUrl || "",
    });
    setShowForm(true);
  }

  async function submitForm(e) {
    e.preventDefault();
    setError(null);
    const payload = {
      name: form.name,
      description: form.description,
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
      imageUrl: form.imageUrl,
    };
    try {
      if (editingId) await api.adminUpdateItem(editingId, payload);
      else await api.adminCreateItem(payload);
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.message || "Save failed");
    }
  }

  async function remove(id) {
    if (!confirm("Delete this item?")) return;
    try {
      await api.adminDeleteItem(id);
      load();
    } catch (err) {
      setError(err.message || "Delete failed");
    }
  }

  if (loading) return <p className="muted">Loading items…</p>;

  return (
    <div>
      <div className="admin-toolbar">
        <h2>Items</h2>
        <button onClick={startCreate}>+ New item</button>
      </div>
      {error && <p className="error">{error}</p>}

      {showForm && (
        <form className="admin-form" onSubmit={submitForm}>
          <label>Name
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </label>
          <label>Description
            <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </label>
          <div className="form-row">
            <label>Price
              <input type="number" step="0.01" min="0" value={form.price}
                onChange={(e) => setForm({ ...form, price: e.target.value })} required />
            </label>
            <label>Stock
              <input type="number" min="0" value={form.stockQuantity}
                onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })} required />
            </label>
          </div>
          <label>Image URL
            <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
              placeholder="https://…" />
          </label>
          <div className="form-actions">
            <button type="submit">{editingId ? "Save changes" : "Create item"}</button>
            <button type="button" className="secondary" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </form>
      )}

      <table className="admin-table">
        <thead><tr><th></th><th>Name</th><th>Price</th><th>Stock</th><th></th></tr></thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              <td>
                {item.imageUrl
                  ? <img src={item.imageUrl} alt={item.name} className="admin-thumb" />
                  : <div className="admin-thumb placeholder">{item.name.charAt(0)}</div>}
              </td>
              <td>{item.name}</td>
              <td className="mono">${item.price.toFixed(2)}</td>
              <td className="mono">{item.stockQuantity}</td>
              <td className="row-actions">
                <button className="secondary" onClick={() => startEdit(item)}>Edit</button>
                <button className="danger" onClick={() => remove(item.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function OrdersAdmin() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  function load() {
    setLoading(true);
    api.adminGetOrders().then(setOrders).catch(() => setError("Couldn't load orders")).finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function changeStatus(id, status) {
    try {
      await api.adminUpdateOrderStatus(id, status);
      load();
    } catch (err) {
      setError(err.message || "Update failed");
    }
  }

  if (loading) return <p className="muted">Loading orders…</p>;

  return (
    <div>
      <h2>All orders</h2>
      {error && <p className="error">{error}</p>}
      <table className="admin-table">
        <thead><tr><th>Order</th><th>User</th><th>Items</th><th>Total</th><th>Status</th></tr></thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td className="mono">#{order.id}</td>
              <td className="mono">{order.userId}</td>
              <td>{order.items.map((i) => `${i.quantity}× ${i.itemName}`).join(", ")}</td>
              <td className="mono">${order.total.toFixed(2)}</td>
              <td>
                <select value={order.status} onChange={(e) => changeStatus(order.id, e.target.value)}>
                  {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function UsersAdmin() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.adminGetUsers().then(setUsers).catch(() => setError("Couldn't load users")).finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="muted">Loading users…</p>;

  return (
    <div>
      <h2>Users</h2>
      {error && <p className="error">{error}</p>}
      <table className="admin-table">
        <thead><tr><th>ID</th><th>Username</th><th>Email</th><th>Joined</th></tr></thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td className="mono">{u.id}</td>
              <td>{u.username}</td>
              <td>{u.email}</td>
              <td className="mono">{new Date(u.createdAt).toLocaleDateString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
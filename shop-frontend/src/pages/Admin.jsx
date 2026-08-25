import { useEffect, useState } from "react";
import { api } from "../api/client";
import Pager from "../components/Pager";

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
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState({
    content: [],
    totalPages: 1,
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const emptyForm = {
    name: "",
    description: "",
    price: "",
    stockQuantity: "",
    imageUrls: [""],
  };

  const [form, setForm] = useState(emptyForm);

  function load() {
    setLoading(true);
    setError(null);

    api
      .getItems(page, 10)
      .then(setPageData)
      .catch(() => setError("Couldn't load items"))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, [page]);

  function startCreate() {
    setEditingId(null);
    setForm({
      name: "",
      description: "",
      price: "",
      stockQuantity: "",
      imageUrls: [""],
    });
    setShowForm(true);
  }

  function startEdit(item) {
    setEditingId(item.id);

    setForm({
      name: item.name,
      description: item.description || "",
      price: item.price,
      stockQuantity: item.stockQuantity,
      imageUrls:
        item.imageUrls && item.imageUrls.length > 0
          ? [...item.imageUrls]
          : [""],
    });

    setShowForm(true);
  }

  function updateImageUrl(i, value) {
    const next = [...form.imageUrls];
    next[i] = value;

    setForm({
      ...form,
      imageUrls: next,
    });
  }

  function moveImage(i, dir) {
    const j = i + dir;

    if (j < 0 || j >= form.imageUrls.length) {
      return;
    }

    const next = [...form.imageUrls];

    [next[i], next[j]] = [next[j], next[i]];

    setForm({
      ...form,
      imageUrls: next,
    });
  }

  function removeImage(i) {
    const next = form.imageUrls.filter(
      (_, idx) => idx !== i
    );

    setForm({
      ...form,
      imageUrls: next.length > 0 ? next : [""],
    });
  }

  async function submitForm(e) {
    e.preventDefault();
    setError(null);

    const payload = {
      name: form.name,
      description: form.description,
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
      imageUrls: form.imageUrls
        .map((url) => url.trim())
        .filter(Boolean),
    };

    try {
      if (editingId) {
        await api.adminUpdateItem(editingId, payload);
      } else {
        await api.adminCreateItem(payload);
      }

      setShowForm(false);
      setEditingId(null);

      setForm({
        name: "",
        description: "",
        price: "",
        stockQuantity: "",
        imageUrls: [""],
      });

      load();
    } catch (err) {
      setError(err.message || "Save failed");
    }
  }

  async function remove(id) {
    if (!confirm("Delete this item?")) {
      return;
    }

    try {
      await api.adminDeleteItem(id);
      load();
    } catch (err) {
      setError(err.message || "Delete failed");
    }
  }

  if (loading) {
    return <p className="muted">Loading items…</p>;
  }

  return (
    <div>
      <div className="admin-toolbar">
        <h2>Items</h2>

        <button onClick={startCreate}>
          + New item
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      {showForm && (
        <form className="admin-form" onSubmit={submitForm}>
          <label>
            Name
            <input
              value={form.name}
              onChange={(e) =>
                setForm({
                  ...form,
                  name: e.target.value,
                })
              }
              required
            />
          </label>

          <label>
            Description
            <textarea
              value={form.description}
              onChange={(e) =>
                setForm({
                  ...form,
                  description: e.target.value,
                })
              }
            />
          </label>

          <div className="form-row">
            <label>
              Price
              <input
                type="number"
                step="0.01"
                min="0"
                value={form.price}
                onChange={(e) =>
                  setForm({
                    ...form,
                    price: e.target.value,
                  })
                }
                required
              />
            </label>

            <label>
              Stock
              <input
                type="number"
                min="0"
                value={form.stockQuantity}
                onChange={(e) =>
                  setForm({
                    ...form,
                    stockQuantity: e.target.value,
                  })
                }
                required
              />
            </label>
          </div>

          <label>Images</label>

          {form.imageUrls.map((url, i) => (
            <div className="image-url-row" key={i}>
              <input
                value={url}
                onChange={(e) =>
                  updateImageUrl(i, e.target.value)
                }
                placeholder="https://…"
              />

              <button
                type="button"
                className="secondary"
                disabled={i === 0}
                onClick={() => moveImage(i, -1)}
              >
                ↑
              </button>

              <button
                type="button"
                className="secondary"
                disabled={
                  i === form.imageUrls.length - 1
                }
                onClick={() => moveImage(i, 1)}
              >
                ↓
              </button>

              <button
                type="button"
                className="danger"
                onClick={() => removeImage(i)}
              >
                ×
              </button>
            </div>
          ))}

          <button
            type="button"
            className="secondary"
            onClick={() =>
              setForm({
                ...form,
                imageUrls: [
                  ...form.imageUrls,
                  "",
                ],
              })
            }
          >
            + Add image URL
          </button>

          <div className="form-actions">
            <button type="submit">
              {editingId
                ? "Save changes"
                : "Create item"}
            </button>

            <button
              type="button"
              className="secondary"
              onClick={() => {
                setShowForm(false);
                setEditingId(null);
              }}
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      <table className="admin-table">
        <thead>
          <tr>
            <th></th>
            <th>Name</th>
            <th>Price</th>
            <th>Stock</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          {pageData.content.map((item) => (
            <tr key={item.id}>
              <td>
                {item.imageUrls &&
                item.imageUrls.length > 0 ? (
                  <img
                    src={item.imageUrls[0]}
                    alt={item.name}
                    className="admin-thumb"
                  />
                ) : (
                  <div className="admin-thumb placeholder">
                    {item.name.charAt(0)}
                  </div>
                )}
              </td>

              <td>{item.name}</td>

              <td className="mono">
                ${Number(item.price).toFixed(2)}
              </td>

              <td className="mono">
                {item.stockQuantity}
              </td>

              <td className="row-actions">
                <button
                  className="secondary"
                  onClick={() => startEdit(item)}
                >
                  Edit
                </button>

                <button
                  className="danger"
                  onClick={() => remove(item.id)}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <Pager
        page={page}
        totalPages={pageData.totalPages}
        onChange={setPage}
      />
    </div>
  );
}

function OrdersAdmin() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  function load() {
    setLoading(true);
    setError(null);

    api
      .adminGetOrders()
      .then(setOrders)
      .catch(() => setError("Couldn't load orders"))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function changeStatus(id, status) {
    try {
      await api.adminUpdateOrderStatus(id, status);
      load();
    } catch (err) {
      setError(err.message || "Update failed");
    }
  }

  if (loading) {
    return <p className="muted">Loading orders…</p>;
  }

  return (
    <div>
      <h2>All orders</h2>

      {error && <p className="error">{error}</p>}

      <table className="admin-table">
        <thead>
          <tr>
            <th>Order</th>
            <th>User</th>
            <th>Items</th>
            <th>Total</th>
            <th>Status</th>
          </tr>
        </thead>

        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td className="mono">
                #{order.id}
              </td>

              <td className="mono">
                {order.userId}
              </td>

              <td>
                {order.items
                  .map(
                    (i) =>
                      `${i.quantity}× ${i.itemName}`
                  )
                  .join(", ")}
              </td>

              <td className="mono">
                ${Number(order.total).toFixed(2)}
              </td>

              <td>
                <select
                  value={order.status}
                  onChange={(e) =>
                    changeStatus(
                      order.id,
                      e.target.value
                    )
                  }
                >
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
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
    api
      .adminGetUsers()
      .then(setUsers)
      .catch(() =>
        setError("Couldn't load users")
      )
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <p className="muted">Loading users…</p>;
  }

  return (
    <div>
      <h2>Users</h2>

      {error && <p className="error">{error}</p>}

      <table className="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Joined</th>
          </tr>
        </thead>

        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td className="mono">
                {u.id}
              </td>

              <td>{u.username}</td>

              <td>{u.email}</td>

              <td className="mono">
                {new Date(
                  u.createdAt
                ).toLocaleDateString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

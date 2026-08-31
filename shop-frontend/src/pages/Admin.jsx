import { useEffect, useState } from "react";
import { api } from "../api/client";
import Pager from "../components/Pager";

const TABS = ["items", "characteristics", "orders", "users"];
const STATUSES = ["PENDING", "PAID", "SHIPPED", "CANCELLED"];

export default function Admin() {
  const [tab, setTab] = useState("items");

  return (
    <div className="admin-page">
      <div className="tab-bar">
        {TABS.map((t) => (
          <button key={t} className={tab === t ? "tab tab-active" : "tab"} onClick={() => setTab(t)}>
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>
      {tab === "items" && <ItemsAdmin />}
      {tab === "characteristics" && <CharacteristicsAdmin />}
      {tab === "orders" && <OrdersAdmin />}
      {tab === "users" && <UsersAdmin />}
    </div>
  );
}

function CharacteristicsAdmin() {
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newTypeName, setNewTypeName] = useState("");
  const [newValueInputs, setNewValueInputs] = useState({});

  function load() {
    setLoading(true);
    api.getCharacteristicTypes().then(setTypes).catch(() => setError("Couldn't load characteristics")).finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function addType(e) {
    e.preventDefault();
    if (!newTypeName.trim()) return;
    try {
      await api.createCharacteristicType(newTypeName.trim());
      setNewTypeName("");
      load();
    } catch (err) {
      setError(err.message || "Couldn't add type");
    }
  }

  async function removeType(id) {
    if (!confirm("Delete this characteristic type and all its values?")) return;
    try {
      await api.deleteCharacteristicType(id);
      load();
    } catch (err) {
      setError(err.message || "Couldn't delete — it may still be in use");
    }
  }

  async function addValue(typeId) {
    const value = (newValueInputs[typeId] || "").trim();
    if (!value) return;
    try {
      await api.addCharacteristicValue(typeId, value);
      setNewValueInputs({ ...newValueInputs, [typeId]: "" });
      load();
    } catch (err) {
      setError(err.message || "Couldn't add value");
    }
  }

  async function removeValue(valueId) {
    if (!confirm("Delete this value?")) return;
    try {
      await api.deleteCharacteristicValue(valueId);
      load();
    } catch (err) {
      setError(err.message || "Couldn't delete — it may still be in use");
    }
  }

  if (loading) return <p className="muted">Loading…</p>;

  return (
    <div>
      <h2>Characteristics</h2>
      {error && <p className="error">{error}</p>}

      <form className="admin-form" onSubmit={addType} style={{ maxWidth: 320 }}>
        <label>New type (e.g. Size, Color)
          <input value={newTypeName} onChange={(e) => setNewTypeName(e.target.value)} />
        </label>
        <button type="submit">+ Add type</button>
      </form>

      {types.map((type) => (
        <div className="characteristic-type-block" key={type.id}>
          <div className="admin-toolbar">
            <h3>{type.name}</h3>
            <button className="danger" onClick={() => removeType(type.id)}>Delete type</button>
          </div>
          <div className="value-chip-row">
            {type.values.map((v) => (
              <span className="value-chip" key={v.id}>
                {v.value}
                <button onClick={() => removeValue(v.id)}>×</button>
              </span>
            ))}
          </div>
          <div className="value-add-row">
            <input placeholder="New value…" value={newValueInputs[type.id] || ""}
              onChange={(e) => setNewValueInputs({ ...newValueInputs, [type.id]: e.target.value })} />
            <button className="secondary" onClick={() => addValue(type.id)}>+ Add value</button>
          </div>
        </div>
      ))}
    </div>
  );
}

function VariantsEditor({ item, allTypes, onClose, onSaved }) {
  const activeTypes = allTypes.filter((t) => item.characteristicTypes.includes(t.name));

  const [rows, setRows] = useState(() =>
    item.variants.map((v) => ({
      stockQuantity: v.stockQuantity,
      priceOverride: v.priceOverride ?? "",
      selections: activeTypes.reduce((acc, t) => ({ ...acc, [t.name]: v.characteristics[t.name] || "" }), {}),
    }))
  );
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function addRow() {
    setRows([...rows, {
      stockQuantity: 0,
      priceOverride: "",
      selections: activeTypes.reduce((acc, t) => ({ ...acc, [t.name]: "" }), {}),
    }]);
  }

  function updateRow(i, patch) {
    setRows(rows.map((r, idx) => (idx === i ? { ...r, ...patch } : r)));
  }

  function removeRow(i) {
    setRows(rows.filter((_, idx) => idx !== i));
  }

  async function save() {
    setError(null);
    const payloadVariants = [];
    for (const row of rows) {
      const valueIds = [];
      for (const t of activeTypes) {
        const selectedName = row.selections[t.name];
        if (!selectedName) {
          setError(`Missing "${t.name}" selection on a variant row`);
          return;
        }
        valueIds.push(t.values.find((v) => v.value === selectedName).id);
      }
      payloadVariants.push({
        stockQuantity: Number(row.stockQuantity),
        priceOverride: row.priceOverride === "" ? null : Number(row.priceOverride),
        characteristicValueIds: valueIds,
      });
    }

    setSaving(true);
    try {
      await api.adminReplaceVariants(item.id, payloadVariants);
      onSaved();
    } catch (err) {
      setError(err.message || "Couldn't save variants");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="variants-editor">
      <div className="admin-toolbar">
        <h3>Variants — {item.name}</h3>
        <button className="secondary" onClick={onClose}>Close</button>
      </div>
      {error && <p className="error">{error}</p>}
      {activeTypes.length === 0 && <p className="muted">No characteristic types enabled — this item uses a single stock line.</p>}

      {rows.map((row, i) => (
        <div className="variant-row" key={i}>
          {activeTypes.map((t) => (
            <select key={t.id} value={row.selections[t.name]}
              onChange={(e) => updateRow(i, { selections: { ...row.selections, [t.name]: e.target.value } })}>
              <option value="">{t.name}…</option>
              {t.values.map((v) => <option key={v.id} value={v.value}>{v.value}</option>)}
            </select>
          ))}
          <input type="number" min="0" placeholder="Stock" value={row.stockQuantity}
            onChange={(e) => updateRow(i, { stockQuantity: e.target.value })} />
          <input type="number" step="0.01" min="0" placeholder="Price override" value={row.priceOverride}
            onChange={(e) => updateRow(i, { priceOverride: e.target.value })} />
          <button className="danger" onClick={() => removeRow(i)}>×</button>
        </div>
      ))}

      <button className="secondary" onClick={addRow}>+ Add variant</button>
      <div className="form-actions">
        <button onClick={save} disabled={saving}>{saving ? "Saving…" : "Save variants"}</button>
      </div>
    </div>
  );
}

function ItemsAdmin() {
  const [pageData, setPageData] = useState({ content: [], totalPages: 1 });
  const [page, setPage] = useState(0);
  const [allTypes, setAllTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [variantsFor, setVariantsFor] = useState(null);
  const emptyForm = { name: "", description: "", price: "", imageUrls: [""], characteristicTypeIds: [] };
  const [form, setForm] = useState(emptyForm);

  function load() {
    setLoading(true);
    api.getItems(page, 10).then(setPageData).catch(() => setError("Couldn't load items")).finally(() => setLoading(false));
  }

  useEffect(load, [page]);
  useEffect(() => { api.getCharacteristicTypes().then(setAllTypes); }, []);

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
      imageUrls: item.imageUrls.length > 0 ? item.imageUrls : [""],
      characteristicTypeIds: allTypes.filter((t) => item.characteristicTypes.includes(t.name)).map((t) => t.id),
    });
    setShowForm(true);
  }

  function toggleType(id) {
    setForm((f) => ({
      ...f,
      characteristicTypeIds: f.characteristicTypeIds.includes(id)
        ? f.characteristicTypeIds.filter((x) => x !== id)
        : [...f.characteristicTypeIds, id],
    }));
  }

  function updateImageUrl(i, value) {
    const next = [...form.imageUrls];
    next[i] = value;
    setForm({ ...form, imageUrls: next });
  }

  function moveImage(i, dir) {
    const next = [...form.imageUrls];
    const j = i + dir;
    [next[i], next[j]] = [next[j], next[i]];
    setForm({ ...form, imageUrls: next });
  }

  function removeImage(i) {
    setForm({ ...form, imageUrls: form.imageUrls.filter((_, idx) => idx !== i) });
  }

  async function submitForm(e) {
    e.preventDefault();
    setError(null);
    const payload = {
      name: form.name,
      description: form.description,
      price: Number(form.price),
      imageUrls: form.imageUrls.map((u) => u.trim()).filter(Boolean),
      characteristicTypeIds: form.characteristicTypeIds,
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
          <label>Name<input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></label>
          <label>Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
          <label>Base price<input type="number" step="0.01" min="0" value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })} required /></label>

          <label>Images</label>
          {form.imageUrls.map((url, i) => (
            <div className="image-url-row" key={i}>
              <input value={url} onChange={(e) => updateImageUrl(i, e.target.value)} placeholder="https://…" />
              <button type="button" className="secondary" disabled={i === 0} onClick={() => moveImage(i, -1)}>↑</button>
              <button type="button" className="secondary" disabled={i === form.imageUrls.length - 1} onClick={() => moveImage(i, 1)}>↓</button>
              <button type="button" className="danger" onClick={() => removeImage(i)}>×</button>
            </div>
          ))}
          <button type="button" className="secondary" onClick={() => setForm({ ...form, imageUrls: [...form.imageUrls, ""] })}>
            + Add image URL
          </button>

          <label>Characteristic types
            <div className="checkbox-row">
              {allTypes.map((t) => (
                <label key={t.id} className="checkbox-inline">
                  <input type="checkbox" checked={form.characteristicTypeIds.includes(t.id)} onChange={() => toggleType(t.id)} />
                  {t.name}
                </label>
              ))}
            </div>
          </label>

          <div className="form-actions">
            <button type="submit">{editingId ? "Save changes" : "Create item"}</button>
            <button type="button" className="secondary" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </form>
      )}

      <table className="admin-table">
        <thead><tr><th></th><th>Name</th><th>Price</th><th>Stock</th><th>Options</th><th></th></tr></thead>
        <tbody>
          {pageData.content.map((item) => (
            <tr key={item.id}>
              <td>
                {item.imageUrls.length > 0
                  ? <img src={item.imageUrls[0]} alt={item.name} className="admin-thumb" />
                  : <div className="admin-thumb placeholder">{item.name.charAt(0)}</div>}
              </td>
              <td>{item.name}</td>
              <td className="mono">${item.price.toFixed(2)}</td>
              <td className="mono">{item.totalStock}</td>
              <td>{item.characteristicTypes.join(", ") || "—"}</td>
              <td className="row-actions">
                <button className="secondary" onClick={() => startEdit(item)}>Edit</button>
                <button className="secondary" onClick={() => setVariantsFor(item)}>Variants</button>
                <button className="danger" onClick={() => remove(item.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} totalPages={pageData.totalPages} onChange={setPage} />

      {variantsFor && (
        <VariantsEditor
          item={variantsFor}
          allTypes={allTypes}
          onClose={() => setVariantsFor(null)}
          onSaved={() => { setVariantsFor(null); load(); }}
        />
      )}
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
              <td>{order.items.map((i) => `${i.quantity}× ${i.itemName}${Object.keys(i.characteristics || {}).length ? ` (${Object.values(i.characteristics).join(", ")})` : ""}`).join(", ")}</td>
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
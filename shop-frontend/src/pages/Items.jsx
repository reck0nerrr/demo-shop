import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import ImageCarousel from "../components/ImageCarousel";
export default function Items() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [placing, setPlacing] = useState(false);
  const { user } = useAuth();
  const { lines, addItem, setQuantity, clear, total } = useCart();

  useEffect(() => {
    api
      .getItems()
      .then(setItems)
      .catch(() => setError("Couldn't load items"))
      .finally(() => setLoading(false));
  }, []);

  async function checkout() {
    if (!user) return;
    setPlacing(true);
    try {
      await api.createOrder({
        items: lines.map((l) => ({ itemId: l.item.id, quantity: l.quantity })),
      });
      clear();
    } catch (err) {
      setError(err.message || "Checkout failed");
    } finally {
      setPlacing(false);
    }
  }

  if (loading) return <p className="muted">Loading items…</p>;
  if (error) return <p className="error">{error}</p>;

  return (
    <div className="items-layout">
      <div className="item-grid">
        {items.map((item) => (
          <div className="item-card" key={item.id}>
            <ImageCarousel images={item.imageUrls} alt={item.name} />
            <h3>{item.name}</h3>
            <p className="muted">{item.description}</p>
            <div className="item-footer">
              <span className="price">${item.price.toFixed(2)}</span>
              <button onClick={() => addItem(item)} disabled={item.stockQuantity === 0}>
                {item.stockQuantity === 0 ? "Out of stock" : "Add"}
              </button>
            </div>
          </div>
        ))}
      </div>

      {lines.length > 0 && (
        <aside className="cart-panel">
          <h2>Cart</h2>
          {lines.map((l) => (
            <div className="cart-line" key={l.item.id}>
              <span>{l.item.name}</span>
              <input
                type="number"
                min="0"
                value={l.quantity}
                onChange={(e) => setQuantity(l.item.id, Number(e.target.value))}
              />
              <span>${(l.item.price * l.quantity).toFixed(2)}</span>
            </div>
          ))}
          <div className="cart-total">
            <strong>Total</strong>
            <strong>${total.toFixed(2)}</strong>
          </div>
          {user ? (
            <button onClick={checkout} disabled={placing} className="cta">
              {placing ? "Placing order…" : "Place order"}
            </button>
          ) : (
            <p className="muted">Log in to check out.</p>
          )}
        </aside>
      )}
    </div>
  );
}
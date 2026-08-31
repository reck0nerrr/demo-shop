import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { api } from "../api/client";

export default function Cart() {
  const { cart, loading, error, setQuantity, removeItem, clear, refresh } = useCart();
  const [checkingOut, setCheckingOut] = useState(false);
  const [checkoutError, setCheckoutError] = useState(null);
  const navigate = useNavigate();

  async function handleCheckout() {
    setCheckingOut(true);
    setCheckoutError(null);
    try {
      await api.checkout();
      await refresh();
      navigate("/orders");
    } catch (err) {
      setCheckoutError(err.message || "Checkout failed — please review your cart");
    } finally {
      setCheckingOut(false);
    }
  }

  if (loading) return <p className="muted">Loading cart…</p>;
  if (error) return <p className="error">{error}</p>;

  if (cart.items.length === 0) {
    return (
      <div className="cart-empty">
        <p className="muted">Your cart is empty.</p>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <h1>Your cart</h1>
      {checkoutError && <p className="error">{checkoutError}</p>}

      <div className="cart-page-list">
        {cart.items.map((line) => (
          <div className="cart-page-row" key={line.variantId}>
            <div className="cart-page-image">
              {line.imageUrl ? <img src={line.imageUrl} alt={line.itemName} /> : <div className="carousel-placeholder">{line.itemName.charAt(0)}</div>}
            </div>
            <div className="cart-page-info">
              <h3>{line.itemName}</h3>
              {Object.keys(line.characteristics || {}).length > 0 && (
                <span className="muted variant-label">
                  {Object.entries(line.characteristics).map(([k, v]) => `${k}: ${v}`).join(", ")}
                </span>
              )}
              <span className="price mono">${line.price.toFixed(2)}</span>
              <span className="muted">{line.availableStock} in stock</span>
            </div>
            <div className="cart-page-qty">
              <button className="secondary" onClick={() => setQuantity(line.variantId, line.quantity - 1)}>−</button>
              <input
                type="number" min="0" max={line.availableStock} value={line.quantity}
                onChange={(e) => setQuantity(line.variantId, Math.min(Math.max(0, Number(e.target.value)), line.availableStock))}
              />
              <button className="secondary" disabled={line.quantity >= line.availableStock} onClick={() => setQuantity(line.variantId, line.quantity + 1)}>+</button>
            </div>
            <div className="cart-page-subtotal mono">${line.subtotal.toFixed(2)}</div>
            <button className="danger" onClick={() => removeItem(line.variantId)}>Remove</button>
          </div>
        ))}
      </div>

      <div className="cart-page-footer">
        <button className="secondary" onClick={clear}>Clear cart</button>
        <div className="cart-page-total">
          <span>Total</span>
          <strong className="mono">${cart.total.toFixed(2)}</strong>
        </div>
        <button className="cta" onClick={handleCheckout} disabled={checkingOut}>
          {checkingOut ? "Placing order…" : "Checkout"}
        </button>
      </div>
    </div>
  );
}
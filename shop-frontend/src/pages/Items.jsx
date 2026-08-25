import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import ImageCarousel from "../components/ImageCarousel";
import Pager from "../components/Pager";

export default function Items() {
  const [searchInput, setSearchInput] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState({ content: [], totalPages: 1 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [placing, setPlacing] = useState(false);
  const { user } = useAuth();
  const { lines, addItem, setQuantity, clear, total } = useCart();

  // debounce: wait for typing to pause before firing a request
  useEffect(() => {
    const t = setTimeout(() => {
      setQuery(searchInput);
      setPage(0);
    }, 300);
    return () => clearTimeout(t);
  }, [searchInput]);

  useEffect(() => {
    setLoading(true);
    api
      .getItems(page, 12, query)
      .then(setPageData)
      .catch(() => setError("Couldn't load items"))
      .finally(() => setLoading(false));
  }, [page, query]);

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

  return (
    <div className="items-layout">
      <div>
        <input
          className="search-bar"
          type="text"
          placeholder="Search items…"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
        />

        {loading ? (
          <p className="muted">Loading items…</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : pageData.content.length === 0 ? (
          <p className="muted">
            {query ? `No items match "${query}".` : "No items yet."}
          </p>
        ) : (
          <>
            <div className="item-grid">
              {pageData.content.map((item) => (
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
            <Pager page={page} totalPages={pageData.totalPages} onChange={setPage} />
          </>
        )}
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
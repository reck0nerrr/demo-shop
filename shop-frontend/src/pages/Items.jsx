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
  const [addingId, setAddingId] = useState(null);
  const [cartError, setCartError] = useState(null);
  const { user } = useAuth();
  const { addItem } = useCart();

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

  async function handleAdd(item) {
    setCartError(null);
    setAddingId(item.id);
    try {
      await addItem(item.id, 1);
    } catch (err) {
      setCartError(err.message || "Couldn't add to cart");
    } finally {
      setAddingId(null);
    }
  }

  return (
    <div>
      <input
        className="search-bar"
        type="text"
        placeholder="Search items…"
        value={searchInput}
        onChange={(e) => setSearchInput(e.target.value)}
      />
      {cartError && <p className="error">{cartError}</p>}

      {loading ? (
        <p className="muted">Loading items…</p>
      ) : error ? (
        <p className="error">{error}</p>
      ) : pageData.content.length === 0 ? (
        <p className="muted">{query ? `No items match "${query}".` : "No items yet."}</p>
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
                  <button
                    onClick={() => handleAdd(item)}
                    disabled={!user || item.stockQuantity === 0 || addingId === item.id}
                  >
                    {!user
                      ? "Log in to buy"
                      : item.stockQuantity === 0
                      ? "Out of stock"
                      : addingId === item.id
                      ? "Adding…"
                      : "Add"}
                  </button>
                </div>
              </div>
            ))}
          </div>
          <Pager page={page} totalPages={pageData.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { api } from "../api/client";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cart, setCart] = useState({ items: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const refresh = useCallback(() => {
    if (!user) {
      setCart({ items: [], total: 0 });
      return Promise.resolve();
    }
    setLoading(true);
    return api
      .getCart()
      .then(setCart)
      .catch((err) => setError(err.message || "Couldn't load cart"))
      .finally(() => setLoading(false));
  }, [user]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function addItem(itemId, quantity = 1) {
    setError(null);
    try {
      setCart(await api.addCartItem(itemId, quantity));
    } catch (err) {
      setError(err.message || "Couldn't add item");
      throw err;
    }
  }

  async function setQuantity(itemId, quantity) {
    setError(null);
    try {
      setCart(await api.updateCartItem(itemId, quantity));
    } catch (err) {
      setError(err.message || "Couldn't update quantity");
      throw err;
    }
  }

  async function removeItem(itemId) {
    setError(null);
    try {
      setCart(await api.removeCartItem(itemId));
    } catch (err) {
      setError(err.message || "Couldn't remove item");
      throw err;
    }
  }

  async function clear() {
    setError(null);
    try {
      await api.clearCart();
      setCart({ items: [], total: 0 });
    } catch (err) {
      setError(err.message || "Couldn't clear cart");
      throw err;
    }
  }

  const itemCount = cart.items.reduce((sum, l) => sum + l.quantity, 0);

  return (
    <CartContext.Provider value={{ cart, loading, error, itemCount, addItem, setQuantity, removeItem, clear, refresh }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
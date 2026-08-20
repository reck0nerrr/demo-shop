import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Orders() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .getMyOrders()
      .then(setOrders)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="muted">Loading orders…</p>;
  if (orders.length === 0) return <p className="muted">No orders yet.</p>;

  return (
    <div className="orders-list">
      {orders.map((order) => (
        <div className="order-card" key={order.id}>
          <div className="order-header">
            <span>Order #{order.id}</span>
            <span className={`status status-${order.status.toLowerCase()}`}>
              {order.status}
            </span>
          </div>
          <li>
            {order.items.map((line) => (
              <li key={line.itemId}>
                <span>{line.quantity}× {line.itemName}</span>
                <span>${(line.price * line.quantity).toFixed(2)}</span>
              </li>
            ))}
          </li>
          <div className="order-total">Total: ${order.total.toFixed(2)}</div>
        </div>
      ))}
    </div>
  );
}
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const { lines } = useCart();
  const isAdmin = user?.role === "ADMIN";
  const itemCount = lines.reduce((sum, l) => sum + l.quantity, 0);

  return (
    <header className="navbar">
      <Link to={isAdmin ? "/admin" : "/"} className="brand">shop</Link>
      <nav>
        {isAdmin ? (
          <Link to="/admin">Admin</Link>
        ) : (
          <>
            <Link to="/">Browse</Link>
            {user && <Link to="/orders">Orders</Link>}
          </>
        )}
        {!isAdmin && itemCount > 0 && <span className="cart-badge">{itemCount} in cart</span>}
      </nav>
      <div className="auth-actions">
        {user ? (
          <>
            <span className="username">{user.username}</span>
            <button onClick={logout}>Log out</button>
          </>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register" className="cta">Sign up</Link>
          </>
        )}
      </div>
    </header>
  );
}
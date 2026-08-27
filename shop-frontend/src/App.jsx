import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { CartProvider } from "./context/CartContext";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Items from "./pages/Items";
import Orders from "./pages/Orders";
import Admin from "./pages/Admin";
import "./index.css";
import Cart from "./pages/Cart";

function RequireAuth({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

function RequireAdmin({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") return <Navigate to="/" replace />;
  return children;
}

// Admins have no business browsing the storefront — bounce them to their dashboard.
function BlockAdmin({ children }) {
  const { user } = useAuth();
  if (user?.role === "ADMIN") return <Navigate to="/admin" replace />;
  return children;
}

function AppRoutes() {
  return (
    <BrowserRouter>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<BlockAdmin><Items /></BlockAdmin>} />
          <Route path="/login" element={<BlockAdmin><Login /></BlockAdmin>} />
          <Route path="/register" element={<BlockAdmin><Register /></BlockAdmin>} />
          <Route
            path="/orders"
            element={
              <BlockAdmin>
                <RequireAuth>
                  <Orders />
                </RequireAuth>
              </BlockAdmin>
            }
          />
          <Route
            path="/admin"
            element={
              <RequireAdmin>
                <Admin />
              </RequireAdmin>
            }
          />
          <Route
            path="/cart"
            element={
              <BlockAdmin>
                <RequireAuth>
                  <Cart />
                </RequireAuth>
              </BlockAdmin>
            }
          />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <AppRoutes />
      </CartProvider>
    </AuthProvider>
  );
}
import { LogOut, Pill, ShoppingCart } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { getCart } from "../api/pharmacy-api.js";
import { Button } from "../components/ui/button.jsx";
import { useAuthStore } from "../store/auth-store.js";

export function AppLayout() {
  const navigate = useNavigate();
  const { token, user, logout } = useAuthStore();
  const cartQuery = useQuery({
    queryKey: ["cart", token],
    queryFn: () => getCart(token),
    enabled: Boolean(token) && user?.role !== "ADMIN",
  });
  const cartItems = cartQuery.data?.totalItems || cartQuery.data?.items?.length || 0;

  function handleLogout() {
    logout();
    navigate("/");
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to="/app" className="brand">
          <Pill size={22} />
          <span>PulseMeds</span>
        </NavLink>
        <nav className="main-nav">
          <NavLink to="/app/catalog">Catalog</NavLink>
          {user?.role !== "ADMIN" ? (
            <NavLink to="/app/cart">
              <ShoppingCart size={16} /> Cart {cartItems ? `(${cartItems})` : ""}
            </NavLink>
          ) : null}
          <NavLink to="/app/account/profile">Account</NavLink>
          {user?.role === "ADMIN" ? <NavLink to="/app/admin">Admin</NavLink> : null}
        </nav>
        <div className="user-actions">
          <span className="user-name">{user?.name || user?.email}</span>
          <Button variant="ghost" size="sm" onClick={handleLogout}>
            <LogOut size={16} /> Logout
          </Button>
        </div>
      </header>
      <main className="page-wrap">
        <Outlet />
      </main>
    </div>
  );
}

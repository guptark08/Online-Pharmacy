import { NavLink, Outlet } from "react-router-dom";
import { useAuthStore } from "../store/auth-store.js";

const links = [
  { to: "/app/account/profile", label: "Profile" },
  { to: "/app/account/prescriptions", label: "Prescriptions" },
  { to: "/app/account/orders", label: "Orders" },
  { to: "/app/account/checkout", label: "Checkout" },
];

export function AccountLayout() {
  const user = useAuthStore((state) => state.user);
  const visibleLinks = user?.role === "ADMIN" ? links.filter((link) => link.label === "Profile") : links;

  return (
    <div className="split-layout">
      <aside className="side-nav">
        <h2>Account</h2>
        {visibleLinks.map((link) => (
          <NavLink key={link.to} to={link.to}>
            {link.label}
          </NavLink>
        ))}
      </aside>
      <section className="split-content">
        <Outlet />
      </section>
    </div>
  );
}

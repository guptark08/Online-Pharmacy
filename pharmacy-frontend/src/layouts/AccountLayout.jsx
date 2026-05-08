import { NavLink, Outlet } from "react-router-dom";
import { ACCOUNT_NAV_LINKS } from "../utils/constants";

export default function AccountLayout() {
  return (
    <section className="dashboard-shell">
      <aside className="dashboard-shell__sidebar">
        <p className="eyebrow">Customer Workspace</p>
        <h2>Your medicine account</h2>
        <p>Upload prescriptions, manage profile, and track every order from one place.</p>

        <nav className="sidebar-nav">
          {ACCOUNT_NAV_LINKS.map((link) => (
            <NavLink key={link.to} to={link.to}>
              {link.label}
            </NavLink>
          ))}
          <NavLink to="/app/cart">Cart</NavLink>
          <NavLink to="/app/account/checkout">Checkout</NavLink>
        </nav>
      </aside>

      <div className="dashboard-shell__content">
        <Outlet />
      </div>
    </section>
  );
}

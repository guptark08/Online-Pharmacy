import { NavLink, Outlet } from "react-router-dom";
import { ADMIN_NAV_LINKS } from "../utils/constants";

export default function AdminLayout() {
  return (
    <section className="dashboard-shell">
      <aside className="dashboard-shell__sidebar">
        <p className="eyebrow">Admin Console</p>
        <h2>Operations center</h2>
        <p>Review prescriptions first, then manage orders, users, and reports.</p>

        <nav className="sidebar-nav">
          {ADMIN_NAV_LINKS.map((link) => (
            <NavLink key={link.to} to={link.to}>
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="dashboard-shell__content">
        <Outlet />
      </div>
    </section>
  );
}

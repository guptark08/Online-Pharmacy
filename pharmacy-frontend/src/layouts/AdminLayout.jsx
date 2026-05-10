import { NavLink, Outlet } from "react-router-dom";

const links = [
  { to: "/app/admin/dashboard", label: "Dashboard" },
  { to: "/app/admin/medicines", label: "Medicines" },
  { to: "/app/admin/orders", label: "Orders" },
  { to: "/app/admin/users", label: "Users" },
  { to: "/app/admin/reports", label: "Reports" },
];

export function AdminLayout() {
  return (
    <div className="split-layout">
      <aside className="side-nav">
        <h2>Admin</h2>
        {links.map((link) => (
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

import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import NotificationCenter from "../components/common/NotificationCenter";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { fetchAdminOrders } from "../store/slices/adminSlice";
import { setFilters } from "../store/slices/catalogSlice";
import { fetchCart, selectCartTotals } from "../store/slices/cartSlice";
import { logout } from "../store/slices/authSlice";
import { fetchOrders } from "../store/slices/orderSlice";
import {
  markAdminNotificationsRead,
  markUserNotificationsRead,
  selectAdminNotificationFeed,
  selectAdminUnreadCount,
  selectUserNotificationFeed,
  selectUserUnreadCount,
} from "../store/slices/notificationSlice";
import Button from "../components/common/Button";
import { selectWishlistCount } from "../store/slices/wishlistSlice";

export default function AppLayout() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated, isAdmin, user } = useAuth();
  const totals = useAppSelector(selectCartTotals);
  const userNotifications = useAppSelector(selectUserNotificationFeed);
  const adminNotifications = useAppSelector(selectAdminNotificationFeed);
  const userUnreadCount = useAppSelector(selectUserUnreadCount);
  const adminUnreadCount = useAppSelector(selectAdminUnreadCount);
  const wishlistCount = useAppSelector(selectWishlistCount);
  const [quickSearch, setQuickSearch] = useState("");

  useEffect(() => {
    if (isAuthenticated && !isAdmin) {
      dispatch(fetchCart());
    }
  }, [dispatch, isAuthenticated, isAdmin]);

  useEffect(() => {
    if (!isAuthenticated) {
      return undefined;
    }

    if (isAdmin) {
      dispatch(fetchAdminOrders());
      const adminPollTimer = window.setInterval(() => {
        dispatch(fetchAdminOrders());
      }, 20000);

      return () => {
        window.clearInterval(adminPollTimer);
      };
    }

    dispatch(fetchOrders());
    const customerPollTimer = window.setInterval(() => {
      dispatch(fetchOrders());
    }, 20000);

    return () => {
      window.clearInterval(customerPollTimer);
    };
  }, [dispatch, isAuthenticated, isAdmin]);

  const handleLogout = () => {
    dispatch(logout());
    navigate("/");
  };

  const handleQuickSearch = (event) => {
    event.preventDefault();
    dispatch(
      setFilters({
        search: quickSearch.trim(),
        categoryId: "",
        prescriptionOnly: false,
      }),
    );
    navigate("/app/catalog");
  };

  const handleMarkNotificationsRead = () => {
    if (isAdmin) {
      dispatch(markAdminNotificationsRead());
      return;
    }
    dispatch(markUserNotificationsRead());
  };

  const activeNotificationFeed = isAdmin ? adminNotifications : userNotifications;
  const activeUnreadCount = isAdmin ? adminUnreadCount : userUnreadCount;

  return (
    <div className="app-shell app-shell--commerce">
      <div className="app-shell__glow app-shell__glow--one" />
      <div className="app-shell__glow app-shell__glow--two" />

      <div className="site-topbar">
        <div className="site-topbar__inner">
          <p>Flat 20% OFF on selected medicines and free delivery on eligible orders.</p>
          <p>Need help? Call 98765 43210</p>
        </div>
      </div>

      <header className="site-header site-header--commerce">
        <div className="site-header__main">
          <Link className="brand" to="/app">
            <span className="brand__mark">
              <img src="/images/branding/pulsemeds-logo.svg" alt="PulseMeds" />
            </span>
            <div>
              <strong>PulseMeds</strong>
              <span>Your Health, Our Priority</span>
            </div>
          </Link>

          <form className="site-search site-search--hero" onSubmit={handleQuickSearch}>
            <input
              type="search"
              value={quickSearch}
              onChange={(event) => setQuickSearch(event.target.value)}
              placeholder="Search medicines, health products..."
              aria-label="Search medicine"
            />
            <Button type="submit">
              Search
            </Button>
          </form>

          <div className="site-header__actions">
            {isAuthenticated ? (
              <Link className="top-action" to={isAdmin ? "/app/admin/prescriptions" : "/app/account/profile"}>
                {isAdmin ? "Admin Console" : "My Account"}
              </Link>
            ) : (
              <Link className="top-action" to="/login">
                Sign In / Register
              </Link>
            )}

            {!isAdmin ? (
              <>
                <Link className="top-action top-action--cart" to="/app/wishlist">
                  Wishlist
                  <span className="top-action__count">{wishlistCount || 0}</span>
                </Link>
                <Link className="top-action top-action--cart" to={isAuthenticated ? "/app/cart" : "/login"}>
                  Cart
                  <span className="top-action__count">{totals.totalItems || 0}</span>
                </Link>
              </>
            ) : null}

            {isAuthenticated ? (
              <>
                <NotificationCenter
                  title={isAdmin ? "Admin Alerts" : "Order Updates"}
                  notifications={activeNotificationFeed}
                  unreadCount={activeUnreadCount}
                  onMarkAllRead={handleMarkNotificationsRead}
                  orderLinkBase={isAdmin ? "/app/admin/orders" : "/app/account/orders"}
                  linkToOrderDetail={!isAdmin}
                />
                <span className="profile-pill">{user?.name || user?.email}</span>
                <Button variant="ghost" onClick={handleLogout}>
                  Logout
                </Button>
              </>
            ) : (
              <Button variant="ghost" onClick={() => navigate("/signup")}>
                Create Account
              </Button>
            )}
          </div>
        </div>

        <nav className="site-nav site-nav--commerce">
          <NavLink to="/app">Home</NavLink>
          <NavLink to="/app/catalog">Medicines</NavLink>
          {!isAdmin ? <NavLink to="/app/wishlist">Wishlist</NavLink> : null}
          <NavLink to={isAuthenticated ? "/app/account/orders" : "/login"}>Track Orders</NavLink>
          <NavLink to={isAuthenticated ? "/app/account/profile" : "/login"}>My Account</NavLink>
          {isAdmin ? <NavLink to="/app/admin/prescriptions">Admin</NavLink> : null}
        </nav>
      </header>

      <main className="page-shell">
        <Outlet />
      </main>

      <footer className="site-footer">
        <p>Medicine ordering, prescription handling, and live order tracking in one place.</p>
        <p>Admin panel includes prescription review, user management, and order workflow updates.</p>
      </footer>
    </div>
  );
}

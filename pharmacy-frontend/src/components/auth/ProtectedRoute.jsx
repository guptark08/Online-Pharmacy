import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

export default function ProtectedRoute({ roles = [], children = null }) {
  const location = useLocation();
  const { isAuthenticated, role } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles.length > 0 && !roles.includes(role)) {
    return <Navigate to={role === "ADMIN" ? "/app/admin/dashboard" : "/app/account/profile"} replace />;
  }

  return children || <Outlet />;
}

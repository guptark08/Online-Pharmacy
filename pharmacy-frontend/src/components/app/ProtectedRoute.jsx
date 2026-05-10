import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "../../store/auth-store.js";

export function ProtectedRoute({ roles, children }) {
  const location = useLocation();
  const { token, user } = useAuthStore();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (roles?.length && !roles.includes(user?.role)) {
    return <Navigate to="/app" replace />;
  }

  return children;
}

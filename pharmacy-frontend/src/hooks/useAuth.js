import { useAppSelector } from "./useAppSelector";
import {
  selectAuthToken,
  selectCurrentUser,
  selectIsAdmin,
  selectIsAuthenticated,
} from "../store/slices/authSlice";

export function useAuth() {
  const token = useAppSelector(selectAuthToken);
  const user = useAppSelector(selectCurrentUser);
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const isAdmin = useAppSelector(selectIsAdmin);

  return {
    token,
    user,
    isAdmin,
    isAuthenticated,
    role: user?.role || "CUSTOMER",
  };
}

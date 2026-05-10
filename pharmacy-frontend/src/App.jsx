import { RouterProvider, createBrowserRouter } from "react-router-dom";
import { ProtectedRoute } from "./components/app/ProtectedRoute.jsx";
import { AccountLayout } from "./layouts/AccountLayout.jsx";
import { AdminLayout } from "./layouts/AdminLayout.jsx";
import { AppLayout } from "./layouts/AppLayout.jsx";
import {
  AdminDashboardPage,
  AdminMedicinesPage,
  AdminOrdersPage,
  AdminReportsPage,
  AdminUsersPage,
  CartPage,
  CatalogPage,
  CheckoutPage,
  HomePage,
  LandingPage,
  LoginPage,
  MedicineDetailPage,
  NotFoundPage,
  OrderDetailPage,
  OrdersPage,
  PrescriptionsPage,
  ProfilePage,
  SignupPage,
} from "./pages/index.jsx";

const router = createBrowserRouter([
  { path: "/", element: <LandingPage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/signup", element: <SignupPage /> },
  {
    path: "/app",
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <HomePage /> },
      { path: "catalog", element: <CatalogPage /> },
      { path: "catalog/:medicineId", element: <MedicineDetailPage /> },
      { path: "cart", element: <CartPage /> },
      {
        path: "account",
        element: <AccountLayout />,
        children: [
          { index: true, element: <ProfilePage /> },
          { path: "profile", element: <ProfilePage /> },
          { path: "prescriptions", element: <PrescriptionsPage /> },
          { path: "orders", element: <OrdersPage /> },
          { path: "orders/:orderId", element: <OrderDetailPage /> },
          { path: "checkout", element: <CheckoutPage /> },
        ],
      },
      {
        path: "admin",
        element: (
          <ProtectedRoute roles={["ADMIN"]}>
            <AdminLayout />
          </ProtectedRoute>
        ),
        children: [
          { index: true, element: <AdminDashboardPage /> },
          { path: "dashboard", element: <AdminDashboardPage /> },
          { path: "medicines", element: <AdminMedicinesPage /> },
          { path: "orders", element: <AdminOrdersPage /> },
          { path: "users", element: <AdminUsersPage /> },
          { path: "reports", element: <AdminReportsPage /> },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);

export default function App() {
  return <RouterProvider router={router} />;
}

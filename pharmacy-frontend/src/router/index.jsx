import { Navigate, createBrowserRouter } from "react-router-dom";
import ProtectedRoute from "../components/auth/ProtectedRoute";
import AccountLayout from "../layouts/AccountLayout";
import AdminLayout from "../layouts/AdminLayout";
import AppLayout from "../layouts/AppLayout";
import AdminDashboardPage from "../pages/AdminDashboardPage";
import AdminOrdersPage from "../pages/AdminOrdersPage";
import AdminPrescriptionReviewPage from "../pages/AdminPrescriptionReviewPage";
import AdminReportsPage from "../pages/AdminReportsPage";
import AdminUsersPage from "../pages/AdminUsersPage";
import CartPage from "../pages/CartPage";
import CategoryMedicinesPage from "../pages/CategoryMedicinesPage";
import CatalogPage from "../pages/CatalogPage";
import CheckoutPage from "../pages/CheckoutPage";
import HomePage from "../pages/HomePage";
import LandingPage from "../pages/LandingPage";
import LoginPage from "../pages/LoginPage";
import MedicineDetailPage from "../pages/MedicineDetailPage";
import NotFoundPage from "../pages/NotFoundPage";
import OrderDetailPage from "../pages/OrderDetailPage";
import OrdersPage from "../pages/OrdersPage";
import PrescriptionsPage from "../pages/PrescriptionsPage";
import ProfilePage from "../pages/ProfilePage";
import SignupPage from "../pages/SignupPage";
import WishlistPage from "../pages/WishlistPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <LandingPage />,
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/signup",
    element: <SignupPage />,
  },
  {
    path: "/app",
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: "catalog",
        element: <CatalogPage />,
      },
      {
        path: "catalog/category/:categoryId",
        element: <CategoryMedicinesPage />,
      },
      {
        path: "catalog/:medicineId",
        element: <MedicineDetailPage />,
      },
      {
        path: "cart",
        element: <CartPage />,
      },
      {
        path: "wishlist",
        element: <WishlistPage />,
      },
      {
        path: "account",
        element: <AccountLayout />,
        children: [
          {
            index: true,
            element: <Navigate to="profile" replace />,
          },
          {
            path: "profile",
            element: <ProfilePage />,
          },
          {
            path: "prescriptions",
            element: <PrescriptionsPage />,
          },
          {
            path: "orders",
            element: <OrdersPage />,
          },
          {
            path: "orders/:orderId",
            element: <OrderDetailPage />,
          },
          {
            path: "checkout",
            element: <CheckoutPage />,
          },
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
          {
            index: true,
            element: <Navigate to="prescriptions" replace />,
          },
          {
            path: "prescriptions",
            element: <AdminPrescriptionReviewPage />,
          },
          {
            path: "dashboard",
            element: <AdminDashboardPage />,
          },
          {
            path: "orders",
            element: <AdminOrdersPage />,
          },
          {
            path: "users",
            element: <AdminUsersPage />,
          },
          {
            path: "reports",
            element: <AdminReportsPage />,
          },
        ],
      },
      {
        path: "*",
        element: <NotFoundPage />,
      },
    ],
  },
  {
    path: "*",
    element: <Navigate to="/" replace />,
  },
]);

import { useEffect } from "react";
import Card from "../components/common/Card";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatCard from "../components/common/StatCard";
import StatusBadge from "../components/common/StatusBadge";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { fetchDashboard } from "../store/slices/adminSlice";
import { formatCurrency, formatDate } from "../utils/format";

export default function AdminDashboardPage() {
  const dispatch = useAppDispatch();
  const dashboard = useAppSelector((state) => state.admin.dashboard);
  const status = useAppSelector((state) => state.admin.dashboardStatus);
  const error = useAppSelector((state) => state.admin.error);

  useDocumentTitle("Admin Dashboard");

  useEffect(() => {
    dispatch(fetchDashboard());
  }, [dispatch]);

  if (status === "loading" && !dashboard) {
    return <LoadingState title="Loading dashboard" description="Collecting order, revenue, and inventory signals." />;
  }

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Admin Overview"
        title="Operations dashboard"
        description="Monitor orders, revenue, prescriptions, and inventory at a glance."
      />

      <ErrorAlert message={error} />

      <div className="grid grid--three">
        <StatCard label="Total Orders" value={dashboard?.totalOrders || 0} hint="All customer orders" />
        <StatCard label="Pending Prescriptions" value={dashboard?.pendingPrescriptions || 0} hint="Waiting for review" accent="mint" />
        <StatCard label="Revenue" value={formatCurrency(dashboard?.totalRevenue || 0)} hint="Paid orders total" accent="sunrise" />
      </div>

      <div className="grid grid--three">
        <StatCard label="Medicines" value={dashboard?.totalMedicines || 0} hint="Active catalog entries" />
        <StatCard label="Categories" value={dashboard?.totalCategories || 0} hint="Active catalog categories" accent="mint" />
        <StatCard label="Low Stock" value={dashboard?.lowStockMedicines || 0} hint="Below low-stock threshold" accent="sunrise" />
      </div>

      <Card title="Recent Orders" accent="glass">
        <div className="stack">
          {dashboard?.recentOrders?.map((order) => (
            <div key={order.id} className="summary-row summary-row--stretch">
              <div>
                <strong>Order #{order.id}</strong>
                <p>{order.userEmail}</p>
                <p>{formatDate(order.createdAt)}</p>
              </div>
              <div className="summary-row__stack">
                <StatusBadge status={order.status} />
                <strong>{formatCurrency(order.totalAmount)}</strong>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}

import { useEffect } from "react";
import Card from "../components/common/Card";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatCard from "../components/common/StatCard";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import {
  fetchInventoryReport,
  fetchSalesReport,
} from "../store/slices/adminSlice";
import { formatCurrency, humanizeEnum } from "../utils/format";

export default function AdminReportsPage() {
  const dispatch = useAppDispatch();
  const reports = useAppSelector((state) => state.admin.reports);
  const status = useAppSelector((state) => state.admin.reportsStatus);
  const error = useAppSelector((state) => state.admin.error);

  useDocumentTitle("Admin Reports");

  useEffect(() => {
    dispatch(fetchSalesReport());
    dispatch(fetchInventoryReport());
  }, [dispatch]);

  if (status === "loading" && !reports.sales) {
    return <LoadingState title="Loading reports" description="Preparing sales and inventory summaries." />;
  }

  const salesByStatus = reports.sales?.ordersByStatus || {};

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Reports"
        title="Sales and inventory summaries"
        description="Use this section to monitor order mix, revenue, and inventory health."
      />

      <ErrorAlert message={error} />

      <div className="grid grid--three">
        <StatCard label="Total Revenue" value={formatCurrency(reports.sales?.totalRevenue || 0)} hint="Paid order revenue" />
        <StatCard label="Total Orders" value={reports.sales?.totalOrders || 0} hint="All tracked orders" accent="mint" />
        <StatCard label="Inventory Status" value={reports.inventory?.reportType || "Available"} hint="Current stock report" accent="sunrise" />
      </div>

      <Card title="Orders by Status" accent="glass">
        <div className="grid grid--three">
          {Object.entries(salesByStatus).map(([statusKey, count]) => (
            <StatCard
              key={statusKey}
              label={humanizeEnum(statusKey)}
              value={count}
              hint="Orders in this state"
            />
          ))}
        </div>
      </Card>

      <Card title="Detailed Report JSON" accent="mint">
        <pre className="code-preview">{JSON.stringify(reports, null, 2)}</pre>
      </Card>
    </div>
  );
}

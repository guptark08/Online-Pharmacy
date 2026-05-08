import { useEffect } from "react";
import { Link } from "react-router-dom";
import Card from "../components/common/Card";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { fetchOrders } from "../store/slices/orderSlice";
import { formatCurrency, formatDate } from "../utils/format";

export default function OrdersPage() {
  const dispatch = useAppDispatch();
  const orders = useAppSelector((state) => state.orders.orders);
  const status = useAppSelector((state) => state.orders.ordersStatus);
  const error = useAppSelector((state) => state.orders.error);

  useDocumentTitle("Orders");

  useEffect(() => {
    dispatch(fetchOrders());
  }, [dispatch]);

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Order History"
        title="Track your medicine orders"
        description="Open any order for detailed progress, payment, and delivery updates."
      />

      <ErrorAlert message={error} />

      {status === "loading" && orders.length === 0 ? (
        <LoadingState title="Loading orders" description="Fetching your latest order updates." />
      ) : orders.length === 0 ? (
        <EmptyState title="No orders yet" description="Your completed checkouts will appear here." />
      ) : (
        <div className="stack">
          {orders.map((order) => (
            <Card key={order.id} accent="glass" title={`Order #${order.id}`}>
              <div className="summary-row">
                <StatusBadge status={order.status} />
                <strong>{formatCurrency(order.totalAmount)}</strong>
              </div>
              <p>Placed on {formatDate(order.createdAt)}</p>
              <p>Delivery slot: {order.deliverySlot || "Not selected"}</p>
              <Link className="text-button" to={`/app/account/orders/${order.id}`}>
                Track order
              </Link>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

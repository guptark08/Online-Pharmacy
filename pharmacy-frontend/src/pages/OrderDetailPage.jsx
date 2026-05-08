import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import FormField from "../components/forms/FormField";
import OrderTimeline from "../components/orders/OrderTimeline";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import {
  cancelOrder,
  fetchOrderById,
  fetchOrders,
  payForOrder,
} from "../store/slices/orderSlice";
import { PAYMENT_METHODS } from "../utils/constants";
import {
  formatCurrency,
  formatDate,
  humanizeEnum,
  isOrderCancelable,
  isOrderPayable,
} from "../utils/format";

export default function OrderDetailPage() {
  const { orderId } = useParams();
  const dispatch = useAppDispatch();
  const order = useAppSelector((state) => state.orders.activeOrder);
  const status = useAppSelector((state) => state.orders.activeOrderStatus);
  const paymentStatus = useAppSelector((state) => state.orders.paymentStatus);
  const error = useAppSelector((state) => state.orders.error);
  const lastPayment = useAppSelector((state) => state.orders.lastPayment);
  const [paymentForm, setPaymentForm] = useState({
    paymentMethod: "UPI",
    transactionReference: `TXN-${orderId}-${Date.now()}`,
  });

  useDocumentTitle(order ? `Order #${order.id}` : "Order Detail");

  useEffect(() => {
    dispatch(fetchOrderById(orderId));
  }, [dispatch, orderId]);

  const handlePaymentChange = (event) => {
    const { name, value } = event.target;
    setPaymentForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handlePayment = async (event) => {
    event.preventDefault();
    await dispatch(payForOrder({ orderId, ...paymentForm })).unwrap();
    await dispatch(fetchOrderById(orderId));
    dispatch(fetchOrders());
  };

  const handleCancel = async () => {
    await dispatch(cancelOrder(orderId)).unwrap();
    dispatch(fetchOrders());
  };

  if (status === "loading" || !order || String(order.id) !== String(orderId)) {
    if (status === "failed") {
      return (
        <div className="page-stack">
          <ErrorAlert message={error} />
          <Card title="Order unavailable" accent="glass">
            <p>The requested order could not be loaded. Try refreshing the order list and opening it again.</p>
          </Card>
        </div>
      );
    }

    return <LoadingState title="Loading order detail" description="Reviewing items, payment, and delivery progress." />;
  }

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Order Tracking"
        title={`Order #${order.id}`}
        description="Follow live status updates from prescription review to final delivery."
      />

      <ErrorAlert message={error} />

      <div className="grid grid--checkout">
        <div className="stack">
          <Card accent="glass" title="Order Snapshot">
            <div className="summary-row">
              <StatusBadge status={order.status} />
              <strong>{formatCurrency(order.totalAmount)}</strong>
            </div>
            <p>Created: {formatDate(order.createdAt)}</p>
            <p>Delivery slot: {order.deliverySlot || "Not selected"}</p>
            <p>Prescription ID: {order.prescriptionId || "Not required"}</p>
            <OrderTimeline order={order} />
          </Card>

          <Card accent="mint" title="Items">
            <div className="stack">
              {order.items?.map((item) => (
                <div key={item.id} className="summary-row">
                  <span>
                    {item.medicineName} x {item.quantity}
                  </span>
                  <strong>{formatCurrency(item.subtotal)}</strong>
                </div>
              ))}
            </div>
          </Card>

          {isOrderCancelable(order.status) ? (
            <Card accent="sunrise" title="Need to cancel?">
              <p>You can cancel this order while it is still active in the workflow.</p>
              <Button variant="ghost" onClick={handleCancel}>
                Cancel Order
              </Button>
            </Card>
          ) : null}
        </div>

        <div className="stack">
          {isOrderPayable(order.status) ? (
            <Card accent="glass" title="Payment">
              <form className="form-stack" onSubmit={handlePayment}>
                <FormField label="Payment Method">
                  <select
                    name="paymentMethod"
                    value={paymentForm.paymentMethod}
                    onChange={handlePaymentChange}
                  >
                    {PAYMENT_METHODS.map((method) => (
                      <option key={method} value={method}>
                        {humanizeEnum(method)}
                      </option>
                    ))}
                  </select>
                </FormField>

                <FormField label="Transaction Reference">
                  <input
                    name="transactionReference"
                    value={paymentForm.transactionReference}
                    onChange={handlePaymentChange}
                    required
                  />
                </FormField>

                <Button type="submit" disabled={paymentStatus === "loading"}>
                  {paymentStatus === "loading" ? "Processing payment..." : "Pay Now"}
                </Button>
              </form>
            </Card>
          ) : null}

          {lastPayment?.orderId === order.id ? (
            <Card accent="mint" title="Latest Payment Update">
              <p>Status: {lastPayment.status}</p>
              <p>Order state: {humanizeEnum(lastPayment.orderStatus)}</p>
              <p>{lastPayment.message}</p>
            </Card>
          ) : null}
        </div>
      </div>
    </div>
  );
}

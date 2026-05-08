import { useEffect, useMemo, useState } from "react";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { fetchAdminOrders, updateAdminOrderStatus } from "../store/slices/adminSlice";
import { formatCurrency, formatDate } from "../utils/format";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export default function AdminPrescriptionReviewPage() {
  const dispatch = useAppDispatch();
  const orders = useAppSelector((state) => state.admin.orders);
  const status = useAppSelector((state) => state.admin.ordersStatus);
  const error = useAppSelector((state) => state.admin.error);
  const token = useAppSelector((state) => state.auth.token);
  const [previewingPrescriptionId, setPreviewingPrescriptionId] = useState(null);
  const [previewError, setPreviewError] = useState("");
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [resolvedPrescriptionIds, setResolvedPrescriptionIds] = useState({});

  useDocumentTitle("Prescription Review");

  useEffect(() => {
    dispatch(fetchAdminOrders());
  }, [dispatch]);

  const reviewQueue = useMemo(
    () => orders.filter((order) => order.status === "PRESCRIPTION_PENDING"),
    [orders],
  );

  const resolvePrescriptionId = async (order) => {
    if (order.prescriptionId) {
      return order.prescriptionId;
    }

    if (resolvedPrescriptionIds[order.id]) {
      return resolvedPrescriptionIds[order.id];
    }

    const lookupPath = `/api/catalog/prescriptions/order/${order.id}`;
    const lookupEndpoint = API_BASE_URL ? `${API_BASE_URL}${lookupPath}` : lookupPath;
    const lookupResponse = await fetch(lookupEndpoint, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!lookupResponse.ok) {
      const details = await lookupResponse.text();
      throw new Error(details || "Prescription file is not linked to this order yet.");
    }

    const prescription = await lookupResponse.json();
    const resolvedId = prescription?.id;
    if (!resolvedId) {
      throw new Error("Prescription file is not linked to this order yet.");
    }

    setResolvedPrescriptionIds((current) => ({
      ...current,
      [order.id]: resolvedId,
    }));

    return resolvedId;
  };

  const handleViewPrescription = async (order) => {
    if (!token) {
      setPreviewError("You are not authenticated. Please sign in again.");
      return;
    }

    setPreviewError("");
    const knownPrescriptionId = order.prescriptionId || resolvedPrescriptionIds[order.id] || order.id;
    setPreviewingPrescriptionId(knownPrescriptionId);

    try {
      const prescriptionId = await resolvePrescriptionId(order);
      const path = `/api/catalog/prescriptions/${prescriptionId}/file`;
      const endpoint = API_BASE_URL ? `${API_BASE_URL}${path}` : path;
      const response = await fetch(endpoint, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const details = await response.text();
        throw new Error(details || "Unable to open prescription.");
      }

      const fileBlob = await response.blob();
      const fileUrl = window.URL.createObjectURL(fileBlob);
      const openedWindow = window.open(fileUrl, "_blank", "noopener,noreferrer");

      if (!openedWindow) {
        throw new Error("Pop-up blocked. Please allow pop-ups.");
      }

      window.setTimeout(() => {
        window.URL.revokeObjectURL(fileUrl);
      }, 60000);
    } catch (previewException) {
      setPreviewError(previewException.message || "Unable to open prescription.");
    } finally {
      setPreviewingPrescriptionId(null);
    }
  };

  const handleReview = async (orderId, nextStatus) => {
    setUpdatingOrderId(orderId);
    await dispatch(updateAdminOrderStatus({ orderId, status: nextStatus }));
    setUpdatingOrderId(null);
  };

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Prescription Review"
        title="Review uploaded prescriptions"
        description="Open each file and approve or reject before payment can continue."
      />

      <ErrorAlert message={error} />
      <ErrorAlert message={previewError} />

      {status === "loading" && reviewQueue.length === 0 ? (
        <LoadingState title="Loading prescription queue" description="Getting pending prescription orders." />
      ) : reviewQueue.length === 0 ? (
        <EmptyState title="No pending prescriptions" description="All prescription reviews are complete." />
      ) : (
        <div className="stack">
          {reviewQueue.map((order) => (
            <Card key={order.id} accent="glass" title={`Order #${order.id}`}>
              <div className="summary-row summary-row--stretch">
                <div>
                  <p><strong>User:</strong> {order.userEmail}</p>
                  <p><strong>Created:</strong> {formatDate(order.createdAt)}</p>
                  <p><strong>Total:</strong> {formatCurrency(order.totalAmount)}</p>
                  <p>
                    <strong>Prescription:</strong>{" "}
                    {(order.prescriptionId || resolvedPrescriptionIds[order.id])
                      ? (order.prescriptionFileName || `#${order.prescriptionId || resolvedPrescriptionIds[order.id]}`)
                      : "Not linked yet"}
                  </p>
                </div>

                <div className="summary-row__stack">
                  <StatusBadge status={order.status} />
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={() => handleViewPrescription(order)}
                    disabled={previewingPrescriptionId === (order.prescriptionId || resolvedPrescriptionIds[order.id] || order.id)}
                  >
                    {previewingPrescriptionId === (order.prescriptionId || resolvedPrescriptionIds[order.id] || order.id)
                      ? "Opening..."
                      : "View Prescription"}
                  </Button>
                  <Button
                    type="button"
                    onClick={() => handleReview(order.id, "PRESCRIPTION_APPROVED")}
                    disabled={updatingOrderId === order.id}
                  >
                    {updatingOrderId === order.id ? "Saving..." : "Approve"}
                  </Button>
                  <Button
                    type="button"
                    variant="danger"
                    onClick={() => handleReview(order.id, "PRESCRIPTION_REJECTED")}
                    disabled={updatingOrderId === order.id}
                  >
                    {updatingOrderId === order.id ? "Saving..." : "Reject"}
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

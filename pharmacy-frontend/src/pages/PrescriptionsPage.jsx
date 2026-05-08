import { useEffect, useState } from "react";
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
import {
  fetchPrescriptions,
  uploadPrescription,
} from "../store/slices/orderSlice";
import { formatDate } from "../utils/format";

export default function PrescriptionsPage() {
  const dispatch = useAppDispatch();
  const prescriptions = useAppSelector((state) => state.orders.prescriptions);
  const status = useAppSelector((state) => state.orders.prescriptionStatus);
  const error = useAppSelector((state) => state.orders.error);
  const [selectedFile, setSelectedFile] = useState(null);

  useDocumentTitle("Prescriptions");

  useEffect(() => {
    dispatch(fetchPrescriptions());
  }, [dispatch]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!selectedFile) {
      return;
    }

    await dispatch(uploadPrescription(selectedFile)).unwrap();
    setSelectedFile(null);
    event.target.reset();
  };

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Prescription Center"
        title="Upload and track prescription files"
        description="Use this section for all prescription documents linked to medicine orders."
      />

      <ErrorAlert message={error} />

        <Card title="Upload Prescription" accent="glass">
        <form className="form-stack" onSubmit={handleSubmit}>
          <input
            type="file"
            accept=".pdf,image/*"
            onChange={(event) => setSelectedFile(event.target.files?.[0] || null)}
            required
          />
          <Button type="submit" disabled={status === "loading"}>
            {status === "loading" ? "Uploading..." : "Upload File"}
          </Button>
        </form>
      </Card>

      {status === "loading" && prescriptions.length === 0 ? (
        <LoadingState title="Loading prescriptions" description="Checking upload and review statuses." />
      ) : prescriptions.length === 0 ? (
        <EmptyState
          title="No prescriptions uploaded"
          description="Upload your first prescription to order Rx medicines."
        />
      ) : (
        <div className="stack">
          {prescriptions.map((prescription) => (
            <Card key={prescription.id} title={`Prescription #${prescription.id}`} accent="mint">
              <div className="summary-row">
                <span>{prescription.fileName}</span>
                <StatusBadge status={prescription.status} />
              </div>
              <p>Uploaded: {formatDate(prescription.uploadedAt)}</p>
              <p>Review note: {prescription.reviewNote || "Awaiting pharmacist review."}</p>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

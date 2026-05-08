import { humanizeEnum } from "../../utils/format";

const STANDARD_STEPS = [
  "PAYMENT_PENDING",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
];

const PRESCRIPTION_STEPS = [
  "PRESCRIPTION_PENDING",
  "PRESCRIPTION_APPROVED",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
];

const BLOCKED_STATUSES = new Set([
  "PRESCRIPTION_REJECTED",
  "CUSTOMER_CANCELLED",
  "ADMIN_CANCELLED",
  "PAYMENT_FAILED",
]);

function getCurrentStepIndex(orderStatus, steps) {
  const index = steps.findIndex((step) => step === orderStatus);

  if (index >= 0) {
    return index;
  }

  if (orderStatus === "PAYMENT_PENDING" && steps.includes("PRESCRIPTION_APPROVED")) {
    return steps.findIndex((step) => step === "PRESCRIPTION_APPROVED");
  }

  return -1;
}

export default function OrderTimeline({ order }) {
  const hasPrescriptionFlow =
    Boolean(order?.prescriptionId) || String(order?.status || "").startsWith("PRESCRIPTION_");
  const steps = hasPrescriptionFlow ? PRESCRIPTION_STEPS : STANDARD_STEPS;
  const currentStepIndex = getCurrentStepIndex(order?.status, steps);
  const isBlocked = BLOCKED_STATUSES.has(order?.status);

  return (
    <section className="order-timeline">
      <strong>Order progress</strong>
      <div className="order-timeline__steps">
        {steps.map((step, index) => {
          let stateClass = "order-timeline__step--upcoming";
          if (index < currentStepIndex) {
            stateClass = "order-timeline__step--done";
          } else if (index === currentStepIndex) {
            stateClass = "order-timeline__step--active";
          }

          return (
            <div key={step} className={["order-timeline__step", stateClass].join(" ")}>
              <span className="order-timeline__dot" />
              <span>{humanizeEnum(step)}</span>
            </div>
          );
        })}
      </div>

      {isBlocked ? (
        <p className="order-timeline__blocked">
          Current state: <strong>{humanizeEnum(order.status)}</strong>. Please review this order update.
        </p>
      ) : null}
    </section>
  );
}

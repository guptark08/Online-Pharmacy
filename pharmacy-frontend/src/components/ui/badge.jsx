import { cn, humanize } from "../../lib/utils.js";

export function Badge({ children, tone = "default", className }) {
  return <span className={cn("badge", `badge-${tone}`, className)}>{children}</span>;
}

export function StatusBadge({ value }) {
  const dangerous = ["REJECTED", "CANCELLED", "FAILED", "INACTIVE"];
  const successful = ["APPROVED", "PAID", "DELIVERED", "ACTIVE"];
  const text = humanize(value);
  const upper = String(value || "").toUpperCase();

  let tone = "default";
  if (dangerous.some((word) => upper.includes(word))) tone = "danger";
  if (successful.some((word) => upper.includes(word))) tone = "success";
  if (upper.includes("PENDING") || upper.includes("LOW")) tone = "warning";

  return <Badge tone={tone}>{text}</Badge>;
}

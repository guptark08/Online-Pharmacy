import { STATUS_THEMES } from "../../utils/constants";
import { humanizeEnum } from "../../utils/format";

export default function StatusBadge({ status = "PENDING" }) {
  const theme = STATUS_THEMES[status] || "muted";

  return <span className={`status-badge status-badge--${theme}`}>{humanizeEnum(status)}</span>;
}

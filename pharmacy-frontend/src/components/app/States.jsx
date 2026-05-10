import { AlertCircle, Loader2 } from "lucide-react";

export function LoadingState({ label = "Loading" }) {
  return (
    <div className="state">
      <Loader2 className="spin" size={22} />
      <span>{label}</span>
    </div>
  );
}

export function ErrorState({ error }) {
  if (!error) return null;
  return (
    <div className="alert">
      <AlertCircle size={18} />
      <span>{error.message || error}</span>
    </div>
  );
}

export function EmptyState({ title = "Nothing here yet", text = "Try again later." }) {
  return (
    <div className="empty">
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}

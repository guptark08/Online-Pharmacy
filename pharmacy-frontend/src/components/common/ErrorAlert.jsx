export default function ErrorAlert({ message, actionLabel = "", onAction = null }) {
  if (!message) {
    return null;
  }

  return (
    <div className="error-alert" role="alert">
      <div>
        <strong>Something needs attention.</strong>
        <p>{message}</p>
      </div>
      {actionLabel && onAction ? (
        <button type="button" className="text-button" onClick={onAction}>
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}

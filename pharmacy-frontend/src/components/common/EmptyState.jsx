export default function EmptyState({
  title = "Nothing here yet",
  description = "Once data is available, it will appear here.",
  children,
}) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      <p>{description}</p>
      {children}
    </div>
  );
}

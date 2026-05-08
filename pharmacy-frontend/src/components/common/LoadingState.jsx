export default function LoadingState({
  title = "Loading content",
  description = "Please wait while we fetch the latest pharmacy data.",
}) {
  return (
    <div className="loading-state">
      <div className="loading-state__pulse" />
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

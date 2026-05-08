import Card from "./Card";

export default function StatCard({ label, value, hint = "", accent = "glass" }) {
  return (
    <Card accent={accent} className="stat-card">
      <p className="stat-card__label">{label}</p>
      <h3 className="stat-card__value">{value}</h3>
      {hint ? <p className="stat-card__hint">{hint}</p> : null}
    </Card>
  );
}

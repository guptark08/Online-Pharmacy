export default function Card({
  title = "",
  eyebrow = "",
  accent = "glass",
  action = null,
  className = "",
  children,
}) {
  return (
    <section className={["card", `card--${accent}`, className].filter(Boolean).join(" ")}>
      {(title || eyebrow || action) && (
        <header className="card__header">
          <div>
            {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
            {title ? <h3 className="card__title">{title}</h3> : null}
          </div>
          {action}
        </header>
      )}
      {children}
    </section>
  );
}

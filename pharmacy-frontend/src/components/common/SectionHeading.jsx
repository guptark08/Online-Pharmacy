export default function SectionHeading({
  eyebrow = "",
  title,
  description = "",
  action = null,
}) {
  return (
    <div className="section-heading">
      <div>
        {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
        <h2>{title}</h2>
        {description ? <p className="section-heading__description">{description}</p> : null}
      </div>
      {action}
    </div>
  );
}

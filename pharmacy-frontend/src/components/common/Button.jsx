export default function Button({
  children,
  variant = "primary",
  size = "md",
  type = "button",
  block = false,
  className = "",
  ...props
}) {
  const classes = [
    "button",
    `button--${variant}`,
    `button--${size}`,
    block ? "button--block" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <button type={type} className={classes} {...props}>
      {children}
    </button>
  );
}

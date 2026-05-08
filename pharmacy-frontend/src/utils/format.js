export function formatCurrency(value) {
  const amount = Number(value || 0);

  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(amount);
}

export function formatDate(value) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function humanizeEnum(value) {
  if (!value) {
    return "Unknown";
  }

  return String(value)
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function isOrderPayable(status) {
  return ["PAYMENT_PENDING", "PRESCRIPTION_APPROVED"].includes(status);
}

export function isOrderCancelable(status) {
  return !["DELIVERED", "CUSTOMER_CANCELLED", "ADMIN_CANCELLED"].includes(status);
}

export function getStockLabel(stock) {
  if (stock <= 0) {
    return "Out of stock";
  }

  if (stock < 10) {
    return "Low stock";
  }

  return "In stock";
}

export function resolveMedicineImage(medicine) {
  const rawImage =
    medicine?.imageUrl ||
    medicine?.image_url ||
    medicine?.image ||
    "";

  if (!rawImage) {
    return "/medicine-images/default.svg";
  }

  // Backend seeds .png paths. We provide matching public SVG assets.
  if (rawImage.startsWith("/images/medicines/")) {
    return rawImage
      .replace("/images/medicines/", "/medicine-images/")
      .replace(/\.png$/i, ".svg");
  }

  return rawImage;
}

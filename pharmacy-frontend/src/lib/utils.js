import { clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

export function asArray(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content ?? [];
}

export function formatCurrency(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
  }).format(Number(value || 0));
}

export function formatDate(value) {
  if (!value) return "Not available";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function humanize(value) {
  if (!value) return "Unknown";

  return String(value)
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

export function medicineImage(medicine) {
  const image = medicine?.imageUrl || medicine?.image_url || medicine?.image || "";

  if (!image) return defaultMedicineImage();
  if (image.startsWith("/images/medicines/")) {
    return image.replace("/images/medicines/", "/medicine-images/").replace(/\.png$/i, ".svg");
  }

  return image;
}

export function defaultMedicineImage() {
  return "/medicine-images/default.svg";
}

export function useDefaultMedicineImage(event) {
  if (event.currentTarget.src.endsWith(defaultMedicineImage())) return;
  event.currentTarget.src = defaultMedicineImage();
}

import { Link } from "react-router-dom";
import { getStockLabel, formatCurrency, resolveMedicineImage } from "../../utils/format";
import Button from "../common/Button";
import Card from "../common/Card";
import StatusBadge from "../common/StatusBadge";

export default function MedicineCard({ medicine, onAdd, onWishlistToggle, wishlisted }) {
  const imageSrc = resolveMedicineImage(medicine);

  return (
    <Card accent="glass" className="medicine-card">
      <div className="medicine-card__media">
        <img
          src={imageSrc}
          alt={medicine.name}
          loading="lazy"
          onError={(event) => {
            event.currentTarget.src = "/medicine-images/default.svg";
          }}
        />
      </div>
      <div className="medicine-card__body">
        <div className="medicine-card__meta">
          <p className="eyebrow">{medicine.category?.name || "Healthcare"}</p>
          {medicine.requiresPrescription ? <StatusBadge status="PRESCRIPTION_REQUIRED" /> : null}
        </div>

        <h3>{medicine.name}</h3>
        <p>{medicine.description}</p>

        <div className="medicine-card__facts">
          <span>{medicine.manufacturer}</span>
          <span>{getStockLabel(medicine.stock)}</span>
        </div>

        <div className="medicine-card__footer">
          <strong>{formatCurrency(medicine.price)}</strong>
          <div className="medicine-card__actions">
            <Link className="text-button" to={`/app/catalog/${medicine.id}`}>
              View Details
            </Link>
            {onWishlistToggle ? (
              <Button type="button" variant="ghost" onClick={() => onWishlistToggle(medicine)}>
                {wishlisted ? "Wishlisted" : "Wishlist"}
              </Button>
            ) : null}
            <Button onClick={() => onAdd(medicine)}>Add to Cart</Button>
          </div>
        </div>
      </div>
    </Card>
  );
}

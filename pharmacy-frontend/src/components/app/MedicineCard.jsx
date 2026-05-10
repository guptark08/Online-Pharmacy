import { Heart, ShoppingCart } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "../ui/button.jsx";
import { Card, CardContent } from "../ui/card.jsx";
import { Badge } from "../ui/badge.jsx";
import { formatCurrency, medicineImage, useDefaultMedicineImage } from "../../lib/utils.js";

export function MedicineCard({ medicine, onAddToCart }) {
  return (
    <Card className="medicine-card">
      <Link to={`/app/catalog/${medicine.id}`} className="medicine-image-link">
        <img src={medicineImage(medicine)} alt={medicine.name} onError={useDefaultMedicineImage} />
      </Link>
      <CardContent>
        <div className="medicine-topline">
          <Badge tone={medicine.stock > 0 ? "success" : "danger"}>
            {medicine.stock > 0 ? "In stock" : "Out of stock"}
          </Badge>
          {medicine.requiresPrescription ? <Badge tone="warning">Rx</Badge> : null}
        </div>
        <Link to={`/app/catalog/${medicine.id}`} className="medicine-title">
          {medicine.name}
        </Link>
        <p className="muted">{medicine.manufacturer || medicine.categoryName || "Medicine"}</p>
        <div className="medicine-footer">
          <strong>{formatCurrency(medicine.price)}</strong>
          <Button size="icon" onClick={() => onAddToCart(medicine)} aria-label="Add to cart">
            <ShoppingCart size={17} />
          </Button>
        </div>
        <Button variant="ghost" className="wishlist-btn" type="button">
          <Heart size={16} /> Wishlist
        </Button>
      </CardContent>
    </Card>
  );
}

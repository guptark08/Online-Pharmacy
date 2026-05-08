import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import StatusBadge from "../components/common/StatusBadge";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { addCartItem } from "../store/slices/cartSlice";
import { toggleWishlistItem } from "../store/slices/wishlistSlice";
import {
  clearSelectedMedicine,
  fetchMedicineById,
} from "../store/slices/catalogSlice";
import { formatCurrency, getStockLabel, resolveMedicineImage } from "../utils/format";

export default function MedicineDetailPage() {
  const { medicineId } = useParams();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAuth();
  const medicine = useAppSelector((state) => state.catalog.selectedMedicine);
  const status = useAppSelector((state) => state.catalog.selectedStatus);
  const error = useAppSelector((state) => state.catalog.error);
  const [quantity, setQuantity] = useState(1);
  const imageSrc = resolveMedicineImage(medicine || {});

  useDocumentTitle(medicine?.name || "Medicine Detail");

  useEffect(() => {
    dispatch(fetchMedicineById(medicineId));

    return () => {
      dispatch(clearSelectedMedicine());
    };
  }, [dispatch, medicineId]);

  const handleAddToCart = async () => {
    if (!medicine) {
      return;
    }

    if (!isAuthenticated) {
      navigate("/login", { state: { from: { pathname: `/app/catalog/${medicineId}` } } });
      return;
    }

    await dispatch(
      addCartItem({
        medicineId: medicine.id,
        medicineName: medicine.name,
        price: medicine.price,
        quantity,
        requiresPrescription: medicine.requiresPrescription,
      }),
    );

    navigate("/app/cart");
  };

  const handleWishlistToggle = () => {
    if (!medicine) {
      return;
    }
    dispatch(toggleWishlistItem(medicine));
  };

  if (status === "loading" || !medicine) {
    if (status === "failed") {
      return (
        <div className="page-stack">
          <ErrorAlert message={error} />
          <EmptyState
            title="Medicine not available"
            description="The medicine detail could not be loaded from the catalog service."
          />
        </div>
      );
    }

    return <LoadingState title="Loading medicine detail" description="We are preparing dosage and availability information." />;
  }

  return (
    <div className="page-stack">
      <ErrorAlert message={error} />

      <section className="detail-hero">
        <div className="detail-hero__media">
          <img
            src={imageSrc}
            alt={medicine.name}
            onError={(event) => {
              event.currentTarget.src = "/medicine-images/default.svg";
            }}
          />
        </div>

        <div className="detail-hero__content">
          <p className="eyebrow">{medicine.category?.name || "Health essentials"}</p>
          <h1>{medicine.name}</h1>
          <p>{medicine.description}</p>

          <div className="detail-hero__tags">
            <StatusBadge status={medicine.requiresPrescription ? "PRESCRIPTION_REQUIRED" : "APPROVED"} />
            <StatusBadge status={medicine.stock < 10 ? "LOW_STOCK" : "ACTIVE"} />
          </div>

          <div className="detail-hero__stats">
            <Card title="Price" accent="glass">
              <h3>{formatCurrency(medicine.price)}</h3>
            </Card>
            <Card title="Manufacturer" accent="mint">
              <h3>{medicine.manufacturer}</h3>
            </Card>
            <Card title="Availability" accent="sunrise">
              <h3>{getStockLabel(medicine.stock)}</h3>
            </Card>
          </div>

          <div className="detail-hero__actions">
            <label className="quantity-picker">
              <span>Quantity</span>
              <input
                type="number"
                min="1"
                max={medicine.stock || 1}
                value={quantity}
                onChange={(event) => setQuantity(Number(event.target.value) || 1)}
              />
            </label>
            <Button onClick={handleAddToCart}>Add to Cart</Button>
            <Button variant="ghost" onClick={handleWishlistToggle}>Wishlist</Button>
          </div>
        </div>
      </section>

      <section className="grid grid--two">
        <Card title="Dosage Guidance" accent="glass">
          <p>{medicine.dosageInfo || "Use according to your doctor's guidance."}</p>
        </Card>
        <Card title="Side Effects" accent="glass">
          <p>{medicine.sideEffects || "No side effects were listed for this product."}</p>
        </Card>
      </section>
    </div>
  );
}

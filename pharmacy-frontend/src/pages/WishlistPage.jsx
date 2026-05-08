import { Link, useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import EmptyState from "../components/common/EmptyState";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { addCartItem } from "../store/slices/cartSlice";
import { removeWishlistItem } from "../store/slices/wishlistSlice";
import { formatCurrency, resolveMedicineImage } from "../utils/format";

export default function WishlistPage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAuth();
  const wishlistItems = useAppSelector((state) => state.wishlist.items);

  useDocumentTitle("Wishlist");

  const handleAddToCart = async (medicine) => {
    if (!isAuthenticated) {
      navigate("/login", { state: { from: { pathname: "/app/wishlist" } } });
      return;
    }

    await dispatch(
      addCartItem({
        medicineId: medicine.id,
        medicineName: medicine.name,
        price: medicine.price,
        quantity: 1,
        requiresPrescription: medicine.requiresPrescription,
      }),
    );
  };

  if (!wishlistItems.length) {
    return (
      <EmptyState
        title="Your wishlist is empty"
        description="Add medicines from the catalog to save them for later."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="page-section">
        <h1>My Wishlist</h1>
        <div className="grid grid--three">
          {wishlistItems.map((medicine) => (
            <Card key={medicine.id} className="medicine-card">
              <div className="medicine-card__media">
                <img
                  src={resolveMedicineImage(medicine)}
                  alt={medicine.name}
                  onError={(event) => {
                    event.currentTarget.src = "/medicine-images/default.svg";
                  }}
                />
              </div>
              <div className="medicine-card__body">
                <h3>{medicine.name}</h3>
                <p>{medicine.description}</p>
                <strong>{formatCurrency(medicine.price)}</strong>
                <div className="medicine-card__actions">
                  <Link className="text-button" to={`/app/catalog/${medicine.id}`}>
                    View Details
                  </Link>
                  <Button onClick={() => handleAddToCart(medicine)}>Add to Cart</Button>
                  <Button variant="ghost" onClick={() => dispatch(removeWishlistItem(medicine.id))}>
                    Remove
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </section>
    </div>
  );
}

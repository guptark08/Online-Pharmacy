import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "../components/catalog/MedicineCard";
import Button from "../components/common/Button";
import LoadingState from "../components/common/LoadingState";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { addCartItem } from "../store/slices/cartSlice";
import { fetchCategories, fetchMedicines } from "../store/slices/catalogSlice";

const WORKFLOW_STEPS = [
  {
    title: "Search Medicine",
    description: "Find your required medicine in seconds.",
  },
  {
    title: "Upload Prescription",
    description: "Required only for Rx medicines.",
  },
  {
    title: "Make Payment",
    description: "Secure payment options with status tracking.",
  },
  {
    title: "Get Delivered",
    description: "Fast doorstep delivery from verified partners.",
  },
];

const TRUST_POINTS = [
  "Fast and free delivery",
  "100% authentic medicines",
  "Secure payment",
  "Easy returns support",
];

export default function HomePage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAuth();
  const categories = useAppSelector((state) => state.catalog.categories);
  const medicines = useAppSelector((state) => state.catalog.medicines);
  const status = useAppSelector((state) => state.catalog.status);

  useDocumentTitle("Home");

  useEffect(() => {
    dispatch(fetchCategories());
    dispatch(fetchMedicines({}));
  }, [dispatch]);

  const handleQuickAdd = async (medicine) => {
    if (!isAuthenticated) {
      navigate("/login", { state: { from: { pathname: "/app/cart" } } });
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
    navigate("/app/cart");
  };

  const handleBrowseCategory = (categoryId) => {
    navigate(`/app/catalog/category/${categoryId}`);
  };

  return (
    <div className="home-shell">
      <section className="home-hero">
        <div className="home-hero__content">
          <h1>
            Your Health,
            <br />
            <span>Delivered Safely</span>
          </h1>
          <p>
            Order medicines online, upload prescriptions for Rx products, and track every order from
            placement to delivery.
          </p>
          <div className="home-hero__actions">
            <Button onClick={() => navigate("/app/catalog")}>Order Now</Button>
            <Button variant="ghost" onClick={() => navigate("/app/account/orders")}>
              Track Orders
            </Button>
          </div>
          <div className="home-hero__benefits">
            <span>Fast Delivery</span>
            <span>100% Authentic</span>
            <span>Best Prices</span>
          </div>
        </div>
        <div className="home-hero__media">
          <div className="home-hero__badge">20% OFF</div>
        </div>
      </section>

      <section className="workflow-strip">
        <h2>How PulseMeds Works</h2>
        <div className="workflow-strip__grid">
          {WORKFLOW_STEPS.map((step, index) => (
            <article key={step.title} className="workflow-step">
              <p className="workflow-step__label">Step {index + 1}</p>
              <h3>{step.title}</h3>
              <p>{step.description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="category-strip">
        <div className="section-header-inline">
          <h2>Explore Categories</h2>
          <Button variant="ghost" onClick={() => navigate("/app/catalog")}>
            View All
          </Button>
        </div>
        <div className="category-strip__grid">
          {categories.slice(0, 6).map((category) => (
            <button
              key={category.id}
              type="button"
              className="category-tile"
              onClick={() => handleBrowseCategory(category.id)}
            >
              <strong>{category.name}</strong>
              <span>{category.description || "Browse medicines in this category"}</span>
            </button>
          ))}
        </div>
      </section>

      <section className="offer-banner">
        <div>
          <p>Save more on medicines</p>
          <h2>Flat 20% OFF</h2>
          <p>On selected orders above minimum cart value.</p>
          <Button onClick={() => navigate("/app/catalog")}>Shop Now</Button>
        </div>
      </section>

      <section className="home-products">
        <div className="section-header-inline">
          <h2>Popular Medicines</h2>
          <Button variant="ghost" onClick={() => navigate("/app/catalog")}>
            Open Catalog
          </Button>
        </div>
        {status === "loading" && medicines.length === 0 ? (
          <LoadingState />
        ) : (
          <div className="grid grid--three">
            {medicines.slice(0, 3).map((medicine) => (
              <MedicineCard key={medicine.id} medicine={medicine} onAdd={handleQuickAdd} />
            ))}
          </div>
        )}
      </section>

      <section className="trust-strip">
        {TRUST_POINTS.map((point) => (
          <article key={point} className="trust-point">
            <strong>{point}</strong>
          </article>
        ))}
      </section>
    </div>
  );
}

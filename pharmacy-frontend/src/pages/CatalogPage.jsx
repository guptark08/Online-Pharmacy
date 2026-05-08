import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "../components/catalog/MedicineCard";
import FilterBar from "../components/catalog/FilterBar";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatCard from "../components/common/StatCard";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { addCartItem } from "../store/slices/cartSlice";
import {
  fetchCategories,
  fetchMedicines,
  resetFilters,
  selectCatalogFilters,
  selectCatalogInsights,
  selectCatalogPagination,
  setPage,
  setFilters,
} from "../store/slices/catalogSlice";
import { toggleWishlistItem } from "../store/slices/wishlistSlice";

export default function CatalogPage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAuth();
  const categories = useAppSelector((state) => state.catalog.categories);
  const medicines = useAppSelector((state) => state.catalog.medicines);
  const status = useAppSelector((state) => state.catalog.status);
  const error = useAppSelector((state) => state.catalog.error);
  const filters = useAppSelector(selectCatalogFilters);
  const insights = useAppSelector(selectCatalogInsights);
  const pagination = useAppSelector(selectCatalogPagination);
  const wishlistItems = useAppSelector((state) => state.wishlist.items);
  const wishlistedIds = new Set(wishlistItems.map((item) => item.id));
  const [draftFilters, setDraftFilters] = useState(filters);

  useDocumentTitle("Catalog");

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  useEffect(() => {
    dispatch(fetchMedicines(filters));
  }, [dispatch, filters]);

  useEffect(() => {
    setDraftFilters(filters);
  }, [filters]);

  const handleInputChange = (event) => {
    const { name, value, type, checked } = event.target;

    setDraftFilters((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    dispatch(setFilters(draftFilters));
  };

  const handleReset = () => {
    dispatch(resetFilters());
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 0 || nextPage >= pagination.totalPages) {
      return;
    }
    dispatch(setPage(nextPage));
  };

  const handleAddToCart = async (medicine) => {
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

  return (
    <div className="page-stack">
      <section className="page-section">
        <SectionHeading
          eyebrow="Medicine Search"
          title="Find medicines by name, category, or prescription need"
          description="Use filters below, then add items directly to cart."
        />

        <div className="grid grid--three">
          <StatCard label="In Stock" value={insights.availableMedicines} hint="Ready to order now" />
          <StatCard label="Prescription" value={insights.prescriptionMedicines} hint="Require valid Rx" accent="mint" />
          <StatCard label="Results" value={insights.totalMedicines} hint="Matching medicines" accent="sunrise" />
        </div>

        <FilterBar
          filters={draftFilters}
          categories={categories}
          onInputChange={handleInputChange}
          onSubmit={handleSubmit}
          onReset={handleReset}
        />
      </section>

      <section className="page-section">
        <ErrorAlert message={error} />

        {status === "loading" ? (
          <LoadingState title="Searching medicines" description="Loading matching products." />
        ) : medicines.length === 0 ? (
          <EmptyState
            title="No medicines found"
            description="Try a broader name or remove some filters."
          />
        ) : (
          <div className="grid grid--three">
            {medicines.map((medicine) => (
              <MedicineCard
                key={medicine.id}
                medicine={medicine}
                onAdd={handleAddToCart}
                onWishlistToggle={(value) => dispatch(toggleWishlistItem(value))}
                wishlisted={wishlistedIds.has(medicine.id)}
              />
            ))}
          </div>
        )}

        {pagination.totalPages > 1 ? (
          <div className="pagination">
            <button
              type="button"
              disabled={pagination.first}
              onClick={() => handlePageChange(pagination.page - 1)}
            >
              Previous
            </button>
            <span>
              Page {pagination.page + 1} of {pagination.totalPages}
            </span>
            <button
              type="button"
              disabled={pagination.last}
              onClick={() => handlePageChange(pagination.page + 1)}
            >
              Next
            </button>
          </div>
        ) : null}
      </section>
    </div>
  );
}

import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import MedicineCard from "../components/catalog/MedicineCard";
import Button from "../components/common/Button";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { addCartItem } from "../store/slices/cartSlice";
import { fetchCategories, fetchMedicines } from "../store/slices/catalogSlice";

export default function CategoryMedicinesPage() {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAuth();
  const categories = useAppSelector((state) => state.catalog.categories);
  const medicines = useAppSelector((state) => state.catalog.medicines);
  const status = useAppSelector((state) => state.catalog.status);
  const error = useAppSelector((state) => state.catalog.error);
  const [search, setSearch] = useState("");

  const category = useMemo(
    () => categories.find((item) => String(item.id) === String(categoryId)),
    [categories, categoryId],
  );

  useDocumentTitle(category ? `${category.name} Medicines` : "Category Medicines");

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  useEffect(() => {
    dispatch(
      fetchMedicines({
        categoryId,
        search,
      }),
    );
  }, [categoryId, dispatch, search]);

  const handleAddToCart = async (medicine) => {
    if (!isAuthenticated) {
      navigate("/login", { state: { from: { pathname: `/app/catalog/${medicine.id}` } } });
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
      <SectionHeading
        eyebrow="Category View"
        title={category ? `${category.name} Medicines` : "Medicines by Category"}
        description="This page shows a focused category interface with quick add-to-cart."
        action={(
          <Button variant="ghost" onClick={() => navigate("/app/catalog")}>
            Back to All Medicines
          </Button>
        )}
      />

      <div className="category-search-row">
        <input
          type="search"
          placeholder="Search inside this category..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      <ErrorAlert message={error} />

      {status === "loading" ? (
        <LoadingState title="Loading category medicines" description="Fetching products for this category." />
      ) : medicines.length === 0 ? (
        <EmptyState title="No medicines found" description="Try a different search or open all medicines." />
      ) : (
        <div className="grid grid--three">
          {medicines.map((medicine) => (
            <MedicineCard key={medicine.id} medicine={medicine} onAdd={handleAddToCart} />
          ))}
        </div>
      )}
    </div>
  );
}

import { createAsyncThunk, createSelector, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../../api/client";

const initialState = {
  medicines: [],
  categories: [],
  selectedMedicine: null,
  pagination: {
    page: 0,
    size: 9,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  },
  filters: {
    search: "",
    categoryId: "",
    prescriptionOnly: false,
  },
  status: "idle",
  categoriesStatus: "idle",
  selectedStatus: "idle",
  error: null,
};

export const fetchCategories = createAsyncThunk(
  "catalog/fetchCategories",
  async (_, { rejectWithValue }) => {
    try {
      return await apiRequest("/api/catalog/categories");
    } catch (error) {
      return rejectWithValue(error.message || "Unable to fetch categories.");
    }
  },
);

export const fetchMedicines = createAsyncThunk(
  "catalog/fetchMedicines",
  async (filters = {}, { rejectWithValue }) => {
    try {
      return await apiRequest("/api/catalog/medicines", {
        query: {
          name: filters.search,
          categoryId: filters.categoryId || undefined,
          requiresPrescription: filters.prescriptionOnly || undefined,
          page: filters.page ?? 0,
          size: filters.size ?? 9,
        },
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load medicines.");
    }
  },
);

export const fetchMedicineById = createAsyncThunk(
  "catalog/fetchMedicineById",
  async (medicineId, { rejectWithValue }) => {
    try {
      return await apiRequest(`/api/catalog/medicines/${medicineId}`);
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load medicine details.");
    }
  },
);

const catalogSlice = createSlice({
  name: "catalog",
  initialState,
  reducers: {
    setFilters(state, action) {
      state.filters = {
        ...state.filters,
        ...action.payload,
      };
      state.filters.page = 0;
    },
    setPage(state, action) {
      state.filters.page = action.payload;
    },
    resetFilters(state) {
      state.filters = initialState.filters;
      state.pagination = initialState.pagination;
    },
    clearSelectedMedicine(state) {
      state.selectedMedicine = null;
      state.selectedStatus = "idle";
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCategories.pending, (state) => {
        state.categoriesStatus = "loading";
      })
      .addCase(fetchCategories.fulfilled, (state, action) => {
        state.categoriesStatus = "succeeded";
        state.categories = action.payload;
      })
      .addCase(fetchCategories.rejected, (state, action) => {
        state.categoriesStatus = "failed";
        state.error = action.payload;
      })
      .addCase(fetchMedicines.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(fetchMedicines.fulfilled, (state, action) => {
        state.status = "succeeded";

        if (Array.isArray(action.payload)) {
          state.medicines = action.payload;
          state.pagination = {
            page: 0,
            size: action.payload.length,
            totalElements: action.payload.length,
            totalPages: action.payload.length > 0 ? 1 : 0,
            first: true,
            last: true,
          };
          return;
        }

        state.medicines = action.payload.content || [];
        state.pagination = {
          page: action.payload.page ?? 0,
          size: action.payload.size ?? state.pagination.size,
          totalElements: action.payload.totalElements ?? state.medicines.length,
          totalPages: action.payload.totalPages ?? (state.medicines.length > 0 ? 1 : 0),
          first: action.payload.first ?? true,
          last: action.payload.last ?? true,
        };
      })
      .addCase(fetchMedicines.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(fetchMedicineById.pending, (state) => {
        state.selectedStatus = "loading";
        state.error = null;
      })
      .addCase(fetchMedicineById.fulfilled, (state, action) => {
        state.selectedStatus = "succeeded";
        state.selectedMedicine = action.payload;
      })
      .addCase(fetchMedicineById.rejected, (state, action) => {
        state.selectedStatus = "failed";
        state.error = action.payload;
      });
  },
});

export const { setFilters, setPage, resetFilters, clearSelectedMedicine } = catalogSlice.actions;

const selectCatalogState = (state) => state.catalog;

export const selectCatalogFilters = createSelector(
  [selectCatalogState],
  (catalog) => catalog.filters,
);

export const selectCatalogPagination = createSelector(
  [selectCatalogState],
  (catalog) => catalog.pagination,
);

export const selectCatalogInsights = createSelector(
  [selectCatalogState],
  (catalog) => ({
    totalMedicines: catalog.medicines.length,
    prescriptionMedicines: catalog.medicines.filter(
      (medicine) => medicine.requiresPrescription,
    ).length,
    availableMedicines: catalog.medicines.filter((medicine) => medicine.stock > 0)
      .length,
  }),
);

export default catalogSlice.reducer;

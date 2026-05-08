import { createSelector, createSlice } from "@reduxjs/toolkit";

const STORAGE_KEY = "pharmacy-wishlist";

function loadWishlist() {
  if (typeof window === "undefined") {
    return [];
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (error) {
    return [];
  }
}

function persistWishlist(items) {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

const wishlistSlice = createSlice({
  name: "wishlist",
  initialState: {
    items: loadWishlist(),
  },
  reducers: {
    toggleWishlistItem(state, action) {
      const medicine = action.payload;
      const exists = state.items.some((item) => item.id === medicine.id);
      state.items = exists
        ? state.items.filter((item) => item.id !== medicine.id)
        : [medicine, ...state.items];
      persistWishlist(state.items);
    },
    removeWishlistItem(state, action) {
      state.items = state.items.filter((item) => item.id !== action.payload);
      persistWishlist(state.items);
    },
  },
});

const selectWishlistState = (state) => state.wishlist;
export const selectWishlistItems = createSelector([selectWishlistState], (wishlist) => wishlist.items);
export const selectWishlistCount = createSelector([selectWishlistItems], (items) => items.length);

export const { toggleWishlistItem, removeWishlistItem } = wishlistSlice.actions;
export default wishlistSlice.reducer;

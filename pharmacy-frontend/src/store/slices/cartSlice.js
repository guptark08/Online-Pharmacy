import { createAsyncThunk, createSelector, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../../api/client";
import { logout } from "./authSlice";

function getToken(getState) {
  return getState().auth.token;
}

const initialState = {
  cart: {
    items: [],
    totalAmount: 0,
    totalItems: 0,
  },
  status: "idle",
  actionStatus: "idle",
  error: null,
};

export const fetchCart = createAsyncThunk(
  "cart/fetchCart",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/orders/cart", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load cart.");
    }
  },
);

export const addCartItem = createAsyncThunk(
  "cart/addCartItem",
  async (cartItem, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/orders/cart/items", {
        method: "POST",
        token: getToken(getState),
        body: cartItem,
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to add item.");
    }
  },
);

export const updateCartItem = createAsyncThunk( "cart/updateCartItem", async ({ itemId, quantity }, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/orders/cart/items/${itemId}`, {
        method: "PUT",
        token: getToken(getState),
        query: { quantity },
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to update quantity.");
    }
  },
);

export const removeCartItem = createAsyncThunk(
  "cart/removeCartItem",
  async (itemId, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/orders/cart/items/${itemId}`, {
        method: "DELETE",
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to remove item.");
    }
  },
);

export const clearCart = createAsyncThunk(
  "cart/clearCart",
  async (_, { getState, rejectWithValue }) => {
    try {
      await apiRequest("/api/orders/cart", {
        method: "DELETE",
        token: getToken(getState),
      });
      return initialState.cart;
    } catch (error) {
      return rejectWithValue(error.message || "Unable to clear cart.");
    }
  },
);

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchCart.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(fetchCart.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.cart = action.payload || initialState.cart;
      })
      .addCase(fetchCart.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(addCartItem.pending, (state) => {
        state.actionStatus = "loading";
        state.error = null;
      })
      .addCase(addCartItem.fulfilled, (state, action) => {
        state.actionStatus = "succeeded";
        state.cart = action.payload;
      })
      .addCase(addCartItem.rejected, (state, action) => {
        state.actionStatus = "failed";
        state.error = action.payload;
      })
      .addCase(updateCartItem.fulfilled, (state, action) => {
        state.actionStatus = "succeeded";
        state.cart = action.payload;
      })
      .addCase(removeCartItem.fulfilled, (state, action) => {
        state.actionStatus = "succeeded";
        state.cart = action.payload;
      })
      .addCase(clearCart.fulfilled, (state, action) => {
        state.actionStatus = "succeeded";
        state.cart = action.payload;
      })
      .addCase(logout, () => initialState);
  },
});

const selectCartState = (state) => state.cart;

export const selectCartTotals = createSelector(
  [selectCartState],
  (cartState) => ({
    totalItems: cartState.cart.totalItems,
    totalAmount: cartState.cart.totalAmount,
  }),
);

export const selectCartRequiresPrescription = createSelector(
  [selectCartState],
  (cartState) => cartState.cart.items.some((item) => item.requiresPrescription),
);

export default cartSlice.reducer;
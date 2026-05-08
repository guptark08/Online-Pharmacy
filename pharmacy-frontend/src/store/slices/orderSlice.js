import { createAsyncThunk, createSelector, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../../api/client";
import { logout } from "./authSlice";

function getToken(getState) {
  return getState().auth.token;
}

function upsertOrder(orders, updatedOrder) {
  const existingIndex = orders.findIndex((order) => order.id === updatedOrder.id);

  if (existingIndex === -1) {
    return [updatedOrder, ...orders];
  }

  return orders.map((order) => (order.id === updatedOrder.id ? updatedOrder : order));
}

const initialState = {
  orders: [],
  activeOrder: null,
  addresses: [],
  prescriptions: [],
  lastPayment: null,
  ordersStatus: "idle",
  activeOrderStatus: "idle",
  addressStatus: "idle",
  prescriptionStatus: "idle",
  checkoutStatus: "idle",
  paymentStatus: "idle",
  error: null,
};

export const fetchOrders = createAsyncThunk(
  "orders/fetchOrders",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/orders", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to fetch orders.");
    }
  },
);

export const fetchOrderById = createAsyncThunk(
  "orders/fetchOrderById",
  async (orderId, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/orders/${orderId}`, {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to fetch order.");
    }
  },
);

export const fetchAddresses = createAsyncThunk(
  "orders/fetchAddresses",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/orders/addresses", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to fetch addresses.");
    }
  },
);

export const saveAddress = createAsyncThunk(
  "orders/saveAddress",
  async (address, { getState, rejectWithValue }) => {
    const addressPayload = {
      ...address,
      default: Boolean(address.isDefault ?? address.default),
    };
    delete addressPayload.isDefault;

    try {
      return await apiRequest("/api/orders/addresses", {
        method: "POST",
        token: getToken(getState),
        body: addressPayload,
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to save address.");
    }
  },
);

export const fetchPrescriptions = createAsyncThunk(
  "orders/fetchPrescriptions",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/catalog/prescriptions/my", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to fetch prescriptions.");
    }
  },
);

export const uploadPrescription = createAsyncThunk(
  "orders/uploadPrescription",
  async (file, { getState, rejectWithValue }) => {
    const formData = new FormData();
    formData.append("file", file);

    try {
      return await apiRequest("/api/catalog/prescriptions/upload", {
        method: "POST",
        token: getToken(getState),
        body: formData,
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to upload prescription.");
    }
  },
);

export const startCheckout = createAsyncThunk(
  "orders/startCheckout",
  async (payload, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/orders/checkout/start", {
        method: "POST",
        token: getToken(getState),
        body: payload,
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to start checkout.");
    }
  },
);

export const payForOrder = createAsyncThunk(
  "orders/payForOrder",
  async ({ orderId, paymentMethod, transactionReference }, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/orders/${orderId}/payment`, {
        method: "POST",
        token: getToken(getState),
        body: {
          paymentMethod,
          transactionReference,
        },
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to process payment.");
    }
  },
);

export const cancelOrder = createAsyncThunk(
  "orders/cancelOrder",
  async (orderId, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/orders/${orderId}/cancel`, {
        method: "POST",
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to cancel order.");
    }
  },
);

const orderSlice = createSlice({
  name: "orders",
  initialState,
  reducers: {
    clearOrderFeedback(state) {
      state.error = null;
      state.lastPayment = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrders.pending, (state) => {
        state.ordersStatus = "loading";
        state.error = null;
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.ordersStatus = "succeeded";
        state.orders = action.payload;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.ordersStatus = "failed";
        state.error = action.payload;
      })
      .addCase(fetchOrderById.pending, (state) => {
        state.activeOrderStatus = "loading";
        state.error = null;
      })
      .addCase(fetchOrderById.fulfilled, (state, action) => {
        state.activeOrderStatus = "succeeded";
        state.activeOrder = action.payload;
        state.orders = upsertOrder(state.orders, action.payload);
      })
      .addCase(fetchOrderById.rejected, (state, action) => {
        state.activeOrderStatus = "failed";
        state.error = action.payload;
      })
      .addCase(fetchAddresses.pending, (state) => {
        state.addressStatus = "loading";
      })
      .addCase(fetchAddresses.fulfilled, (state, action) => {
        state.addressStatus = "succeeded";
        state.addresses = action.payload;
      })
      .addCase(fetchAddresses.rejected, (state, action) => {
        state.addressStatus = "failed";
        state.error = action.payload;
      })
      .addCase(saveAddress.fulfilled, (state, action) => {
        state.addressStatus = "succeeded";
        state.addresses = [action.payload, ...state.addresses.filter((item) => item.id !== action.payload.id)];
      })
      .addCase(fetchPrescriptions.pending, (state) => {
        state.prescriptionStatus = "loading";
      })
      .addCase(fetchPrescriptions.fulfilled, (state, action) => {
        state.prescriptionStatus = "succeeded";
        state.prescriptions = action.payload;
      })
      .addCase(fetchPrescriptions.rejected, (state, action) => {
        state.prescriptionStatus = "failed";
        state.error = action.payload;
      })
      .addCase(uploadPrescription.fulfilled, (state, action) => {
        state.prescriptionStatus = "succeeded";
        state.prescriptions = [action.payload, ...state.prescriptions];
      })
      .addCase(startCheckout.pending, (state) => {
        state.checkoutStatus = "loading";
        state.error = null;
      })
      .addCase(startCheckout.fulfilled, (state, action) => {
        state.checkoutStatus = "succeeded";
        state.activeOrder = action.payload;
        state.orders = upsertOrder(state.orders, action.payload);
      })
      .addCase(startCheckout.rejected, (state, action) => {
        state.checkoutStatus = "failed";
        state.error = action.payload;
      })
      .addCase(payForOrder.pending, (state) => {
        state.paymentStatus = "loading";
        state.error = null;
      })
      .addCase(payForOrder.fulfilled, (state, action) => {
        state.paymentStatus = "succeeded";
        state.lastPayment = action.payload;
      })
      .addCase(payForOrder.rejected, (state, action) => {
        state.paymentStatus = "failed";
        state.error = action.payload;
      })
      .addCase(cancelOrder.fulfilled, (state, action) => {
        state.activeOrder = action.payload;
        state.orders = upsertOrder(state.orders, action.payload);
      })
      .addCase(logout, () => initialState);
  },
});

export const { clearOrderFeedback } = orderSlice.actions;

const selectOrderState = (state) => state.orders;

export const selectCheckoutOptions = createSelector(
  [selectOrderState],
  (orderState) => ({
    addressCount: orderState.addresses.length,
    availablePrescriptions: orderState.prescriptions.filter(
      (prescription) => prescription.status !== "REJECTED",
    ),
  }),
);

export default orderSlice.reducer;

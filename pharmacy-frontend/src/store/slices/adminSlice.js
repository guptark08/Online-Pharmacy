import { createAsyncThunk, createSelector, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../../api/client";
import { logout } from "./authSlice";

function getToken(getState) {
  return getState().auth.token;
}

const initialState = {
  dashboard: null,
  orders: [],
  users: [],
  reports: {
    sales: null,
    inventory: null,
  },
  dashboardStatus: "idle",
  ordersStatus: "idle",
  usersStatus: "idle",
  reportsStatus: "idle",
  error: null,
};

export const fetchDashboard = createAsyncThunk(
  "admin/fetchDashboard",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/admin/dashboard", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load dashboard.");
    }
  },
);

export const fetchAdminOrders = createAsyncThunk(
  "admin/fetchAdminOrders",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/admin/orders", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load admin orders.");
    }
  },
);

export const updateAdminOrderStatus = createAsyncThunk(
  "admin/updateAdminOrderStatus",
  async ({ orderId, status }, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/admin/orders/${orderId}/status`, {
        method: "PUT",
        token: getToken(getState),
        body: { status },
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to update order status.");
    }
  },
);

export const fetchUsers = createAsyncThunk(
  "admin/fetchUsers",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/admin/users", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load users.");
    }
  },
);

export const updateUser = createAsyncThunk(
  "admin/updateUser",
  async ({ userId, role, active }, { getState, rejectWithValue }) => {
    try {
      return await apiRequest(`/api/admin/users/${userId}`, {
        method: "PUT",
        token: getToken(getState),
        body: { role, active },
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to update user.");
    }
  },
);

export const fetchSalesReport = createAsyncThunk(
  "admin/fetchSalesReport",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/admin/reports/sales", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load sales report.");
    }
  },
);

export const fetchInventoryReport = createAsyncThunk(
  "admin/fetchInventoryReport",
  async (_, { getState, rejectWithValue }) => {
    try {
      return await apiRequest("/api/admin/reports/inventory", {
        token: getToken(getState),
      });
    } catch (error) {
      return rejectWithValue(error.message || "Unable to load inventory report.");
    }
  },
);

const adminSlice = createSlice({
  name: "admin",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboard.pending, (state) => {
        state.dashboardStatus = "loading";
        state.error = null;
      })
      .addCase(fetchDashboard.fulfilled, (state, action) => {
        state.dashboardStatus = "succeeded";
        state.dashboard = action.payload;
      })
      .addCase(fetchDashboard.rejected, (state, action) => {
        state.dashboardStatus = "failed";
        state.error = action.payload;
      })
      .addCase(fetchAdminOrders.pending, (state) => {
        state.ordersStatus = "loading";
      })
      .addCase(fetchAdminOrders.fulfilled, (state, action) => {
        state.ordersStatus = "succeeded";
        state.orders = action.payload;
      })
      .addCase(fetchAdminOrders.rejected, (state, action) => {
        state.ordersStatus = "failed";
        state.error = action.payload;
      })
      .addCase(updateAdminOrderStatus.fulfilled, (state, action) => {
        state.orders = state.orders.map((order) =>
          order.id === action.payload.id ? action.payload : order,
        );
      })
      .addCase(fetchUsers.pending, (state) => {
        state.usersStatus = "loading";
      })
      .addCase(fetchUsers.fulfilled, (state, action) => {
        state.usersStatus = "succeeded";
        state.users = action.payload.map((user) => ({
          ...user,
          address: user.address ?? user.addrsss ?? "",
        }));
      })
      .addCase(fetchUsers.rejected, (state, action) => {
        state.usersStatus = "failed";
        state.error = action.payload;
      })
      .addCase(updateUser.fulfilled, (state, action) => {
        state.users = state.users.map((user) =>
          user.id === action.payload.id
            ? {
                ...action.payload,
                address: action.payload.address ?? action.payload.addrsss ?? "",
              }
            : user,
        );
      })
      .addCase(fetchSalesReport.pending, (state) => {
        state.reportsStatus = "loading";
      })
      .addCase(fetchSalesReport.fulfilled, (state, action) => {
        state.reportsStatus = "succeeded";
        state.reports.sales = action.payload;
      })
      .addCase(fetchSalesReport.rejected, (state, action) => {
        state.reportsStatus = "failed";
        state.error = action.payload;
      })
      .addCase(fetchInventoryReport.fulfilled, (state, action) => {
        state.reports.inventory = action.payload;
      })
      .addCase(logout, () => initialState);
  },
});

const selectAdminState = (state) => state.admin;

export const selectAdminInsights = createSelector(
  [selectAdminState],
  (adminState) => ({
    totalUsers: adminState.users.length,
    openOrders: adminState.orders.filter((order) =>
      ["PRESCRIPTION_PENDING", "PAYMENT_PENDING", "PAID", "PACKED"].includes(order.status),
    ).length,
    revenue: adminState.dashboard?.totalRevenue || adminState.reports.sales?.totalRevenue || 0,
  }),
);

export default adminSlice.reducer;

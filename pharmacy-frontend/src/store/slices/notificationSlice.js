import { createSelector, createSlice } from "@reduxjs/toolkit";
import { fetchAdminOrders } from "./adminSlice";
import { logout } from "./authSlice";
import { fetchOrders } from "./orderSlice";

const MAX_NOTIFICATIONS = 40;

function createNotification({ title, message, tone = "info", orderId }) {
  return {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    title,
    message,
    tone,
    orderId,
    seen: false,
    createdAt: new Date().toISOString(),
  };
}

function pushNotification(feed, notification) {
  return [notification, ...feed].slice(0, MAX_NOTIFICATIONS);
}

function buildUserNotification(order, previousStatus) {
  const orderLabel = `Order #${order.id}`;

  if (previousStatus === order.status) {
    return null;
  }

  switch (order.status) {
    case "PRESCRIPTION_APPROVED":
      return createNotification({
        title: "Prescription approved",
        message: `${orderLabel} is approved by admin. You can complete payment now.`,
        tone: "success",
        orderId: order.id,
      });
    case "PRESCRIPTION_REJECTED":
      return createNotification({
        title: "Prescription rejected",
        message: `${orderLabel} was rejected. Upload a clearer prescription to continue.`,
        tone: "danger",
        orderId: order.id,
      });
    case "PAYMENT_PENDING":
      return createNotification({
        title: "Payment pending",
        message: `${orderLabel} is ready for payment.`,
        tone: "warning",
        orderId: order.id,
      });
    case "PAID":
      return createNotification({
        title: "Payment successful",
        message: `${orderLabel} payment is confirmed.`,
        tone: "success",
        orderId: order.id,
      });
    case "PACKED":
      return createNotification({
        title: "Order packed",
        message: `${orderLabel} has been packed by the pharmacy team.`,
        tone: "info",
        orderId: order.id,
      });
    case "OUT_FOR_DELIVERY":
      return createNotification({
        title: "Out for delivery",
        message: `${orderLabel} is on the way to your address.`,
        tone: "info",
        orderId: order.id,
      });
    case "DELIVERED":
      return createNotification({
        title: "Order delivered",
        message: `${orderLabel} was delivered successfully.`,
        tone: "success",
        orderId: order.id,
      });
    case "ADMIN_CANCELLED":
      return createNotification({
        title: "Order cancelled by admin",
        message: `${orderLabel} was cancelled by the admin team.`,
        tone: "danger",
        orderId: order.id,
      });
    default:
      return null;
  }
}

function buildAdminNotification(order, previousStatus, isNewOrder = false) {
  const orderLabel = `Order #${order.id}`;

  if (isNewOrder) {
    return createNotification({
      title: "New order placed",
      message: `${orderLabel} was placed by ${order.userEmail}.`,
      tone: order.status === "PRESCRIPTION_PENDING" ? "warning" : "info",
      orderId: order.id,
    });
  }

  if (previousStatus === order.status) {
    return null;
  }

  switch (order.status) {
    case "CUSTOMER_CANCELLED":
      return createNotification({
        title: "Customer cancelled an order",
        message: `${orderLabel} was cancelled by ${order.userEmail}.`,
        tone: "danger",
        orderId: order.id,
      });
    case "PRESCRIPTION_PENDING":
      return createNotification({
        title: "Prescription review needed",
        message: `${orderLabel} is waiting for prescription review.`,
        tone: "warning",
        orderId: order.id,
      });
    case "PAID":
      return createNotification({
        title: "Order paid",
        message: `${orderLabel} payment is complete and ready for fulfillment.`,
        tone: "success",
        orderId: order.id,
      });
    default:
      return null;
  }
}

function createOrderStatusMap(orders) {
  return orders.reduce((statusMap, order) => {
    statusMap[order.id] = order.status;
    return statusMap;
  }, {});
}

const initialState = {
  userFeed: [],
  adminFeed: [],
  userUnreadCount: 0,
  adminUnreadCount: 0,
  userOrderStatusMap: {},
  adminOrderStatusMap: {},
  userInitialized: false,
  adminInitialized: false,
};

const notificationSlice = createSlice({
  name: "notifications",
  initialState,
  reducers: {
    markUserNotificationsRead(state) {
      state.userFeed = state.userFeed.map((notification) => ({
        ...notification,
        seen: true,
      }));
      state.userUnreadCount = 0;
    },
    markAdminNotificationsRead(state) {
      state.adminFeed = state.adminFeed.map((notification) => ({
        ...notification,
        seen: true,
      }));
      state.adminUnreadCount = 0;
    },
    resetNotifications() {
      return initialState;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrders.fulfilled, (state, action) => {
        const orders = Array.isArray(action.payload) ? action.payload : [];

        if (!state.userInitialized) {
          state.userOrderStatusMap = createOrderStatusMap(orders);
          state.userInitialized = true;
          return;
        }

        const nextStatusMap = {};

        orders.forEach((order) => {
          nextStatusMap[order.id] = order.status;
          const previousStatus = state.userOrderStatusMap[order.id];

          if (previousStatus && previousStatus !== order.status) {
            const notification = buildUserNotification(order, previousStatus);
            if (notification) {
              state.userFeed = pushNotification(state.userFeed, notification);
              state.userUnreadCount += 1;
            }
          }
        });

        state.userOrderStatusMap = nextStatusMap;
      })
      .addCase(fetchAdminOrders.fulfilled, (state, action) => {
        const orders = Array.isArray(action.payload) ? action.payload : [];

        if (!state.adminInitialized) {
          state.adminOrderStatusMap = createOrderStatusMap(orders);
          state.adminInitialized = true;
          return;
        }

        const nextStatusMap = {};

        orders.forEach((order) => {
          nextStatusMap[order.id] = order.status;
          const previousStatus = state.adminOrderStatusMap[order.id];
          const isNewOrder = !previousStatus;

          if (isNewOrder || previousStatus !== order.status) {
            const notification = buildAdminNotification(order, previousStatus, isNewOrder);
            if (notification) {
              state.adminFeed = pushNotification(state.adminFeed, notification);
              state.adminUnreadCount += 1;
            }
          }
        });

        state.adminOrderStatusMap = nextStatusMap;
      })
      .addCase(logout, () => initialState);
  },
});

export const {
  markUserNotificationsRead,
  markAdminNotificationsRead,
  resetNotifications,
} = notificationSlice.actions;

const selectNotificationState = (state) => state.notifications;

export const selectUserNotificationFeed = createSelector(
  [selectNotificationState],
  (notificationState) => notificationState.userFeed,
);

export const selectAdminNotificationFeed = createSelector(
  [selectNotificationState],
  (notificationState) => notificationState.adminFeed,
);

export const selectUserUnreadCount = createSelector(
  [selectNotificationState],
  (notificationState) => notificationState.userUnreadCount,
);

export const selectAdminUnreadCount = createSelector(
  [selectNotificationState],
  (notificationState) => notificationState.adminUnreadCount,
);

export default notificationSlice.reducer;

import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/authSlice";
import catalogReducer from "./slices/catalogSlice";
import cartReducer from "./slices/cartSlice";
import orderReducer from "./slices/orderSlice";
import adminReducer from "./slices/adminSlice";
import notificationReducer from "./slices/notificationSlice";
import wishlistReducer from "./slices/wishlistSlice";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    catalog: catalogReducer,
    cart: cartReducer,
    orders: orderReducer,
    admin: adminReducer,
    notifications: notificationReducer,
    wishlist: wishlistReducer,
  },
});

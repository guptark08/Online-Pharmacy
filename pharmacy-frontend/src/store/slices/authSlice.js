import { createAsyncThunk, createSelector, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../../api/client";
import { clearSession, loadSession, saveSession } from "../../utils/storage";

function normalizeUser(payload = {}) {
  if (!payload) {
    return null;
  }

  return {
    id: payload.id ?? payload.userId ?? null,
    userId: payload.userId ?? payload.id ?? null,
    name: payload.name ?? "",
    email: payload.email ?? "",
    mobile: payload.mobile ?? "",
    address: payload.address ?? payload.addrsss ?? "",
    role: payload.role ?? "CUSTOMER",
    isActive: payload.isActive ?? payload.active ?? true,
    createdAt: payload.createdAt ?? null,
    updatedAt: payload.updatedAt ?? null,
  };
}

async function authenticate(path, credentials) {
  const authPayload = await apiRequest(path, {
    method: "POST",
    body: credentials,
  });

  let user = normalizeUser(authPayload);

  if (authPayload?.token) {
    try {
      const profile = await apiRequest("/api/auth/me", {
        token: authPayload.token,
      });
      user = {
        ...user,
        ...normalizeUser(profile),
      };
    } catch (error) {
      user = normalizeUser(authPayload);
    }
  }

  const session = {
    token: authPayload.token,
    user,
  };

  saveSession(session);
  return session;
}

const persistedSession = loadSession();

const initialState = {
  token: persistedSession?.token || "",
  user: normalizeUser(persistedSession?.user),
  status: "idle",
  error: null,
};

export const loginUser = createAsyncThunk(
  "auth/loginUser",
  async (credentials, { rejectWithValue }) => {
    try {
      return await authenticate("/api/auth/login", credentials);
    } catch (error) {
      return rejectWithValue(error.message || "Unable to sign in.");
    }
  },
);

export const signupUser = createAsyncThunk(
  "auth/signupUser",
  async (profile, { rejectWithValue }) => {
    try {
      return await authenticate("/api/auth/signup", profile);
    } catch (error) {
      return rejectWithValue(error.message || "Unable to create account.");
    }
  },
);

export const fetchProfile = createAsyncThunk(
  "auth/fetchProfile",
  async (tokenOverride, { getState, rejectWithValue }) => {
    const token = tokenOverride || getState().auth.token;

    if (!token) {
      return rejectWithValue("Session not found.");
    }

    try {
      const profile = await apiRequest("/api/auth/me", { token });
      const session = {
        token,
        user: normalizeUser(profile),
      };
      saveSession(session);
      return session.user;
    } catch (error) {
      return rejectWithValue(error.message || "Unable to fetch profile.");
    }
  },
);

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout(state) {
      state.token = "";
      state.user = null;
      state.status = "idle";
      state.error = null;
      clearSession();
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload.token;
        state.user = action.payload.user;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(signupUser.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(signupUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload.token;
        state.user = action.payload.user;
      })
      .addCase(signupUser.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(fetchProfile.pending, (state) => {
        state.error = null;
      })
      .addCase(fetchProfile.fulfilled, (state, action) => {
        state.user = action.payload;
      })
      .addCase(fetchProfile.rejected, (state, action) => {
        state.error = action.payload;
      });
  },
});

export const { logout } = authSlice.actions;

const selectAuthState = (state) => state.auth;

export const selectAuthToken = createSelector(
  [selectAuthState],
  (auth) => auth.token,
);

export const selectCurrentUser = createSelector(
  [selectAuthState],
  (auth) => auth.user,
);

export const selectIsAuthenticated = createSelector(
  [selectAuthToken],
  (token) => Boolean(token),
);

export const selectIsAdmin = createSelector(
  [selectCurrentUser],
  (user) => user?.role === "ADMIN",
);

export default authSlice.reducer;

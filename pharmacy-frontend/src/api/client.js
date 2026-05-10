import axios from "axios";
import { useAuthStore } from "../store/auth-store.js";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export class ApiError extends Error {
  constructor(message, status, payload) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

function makeUrl(path) {
  return API_BASE_URL ? `${API_BASE_URL}${path}` : path;
}

function handleAuthError(token, status) {
  if (token && (status === 401 || status === 403)) {
    useAuthStore.getState().logout();
  }
}

export async function apiRequest(path, options = {}) {
  const { method = "GET", body, query, token, headers = {} } = options;

  try {
    const response = await axios({
      url: makeUrl(path),
      method,
      params: query,
      data: body,
      headers: {
        ...headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });

    return response.status === 204 ? null : response.data;
  } catch (error) {
    const status = error?.response?.status ?? 500;
    const payload = error?.response?.data;
    const message =
      payload?.message ||
      payload?.error ||
      payload?.details ||
      payload?.title ||
      error.message ||
      "Request failed.";

    handleAuthError(token, status);

    throw new ApiError(message, status, payload);
  }
}

export async function apiBlobRequest(path, options = {}) {
  const { method = "GET", query, token, headers = {} } = options;

  try {
    const response = await axios({
      url: makeUrl(path),
      method,
      params: query,
      responseType: "blob",
      headers: {
        ...headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });

    return {
      blob: response.data,
      contentType: response.headers["content-type"],
      contentDisposition: response.headers["content-disposition"],
    };
  } catch (error) {
    const status = error?.response?.status ?? 500;
    const message = error.message || "Request failed.";

    handleAuthError(token, status);

    throw new ApiError(message, status, error?.response?.data);
  }
}

import axios from "axios";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export class ApiError extends Error {
  constructor(message, status, payload) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

export async function apiRequest(
  path,
  { method = "GET", body, query, token, headers = {} } = {},
) {
  const requestHeaders = { ...headers, ...(token ? { Authorization: `Bearer ${token}` } : {}) };
  const requestUrl = API_BASE_URL ? `${API_BASE_URL}${path}` : path;

  try {
    const response = await axios({
      url: requestUrl,
      method,
      params: query,
      data: body,
      headers: requestHeaders,
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
      "Request failed. Please try again.";

    throw new ApiError(message, status, payload);
  }
}

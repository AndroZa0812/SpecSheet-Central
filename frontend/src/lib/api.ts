import axios from "axios";
import type { AxiosInstance } from "axios";
import type { Review, ProductResponse } from "./types";

const api: AxiosInstance = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete config.headers["Content-Type"];
  }
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export async function getReviews(productId: number) {
  return api.get<Review[]>(`/products/${productId}/reviews`);
}

export async function createReview(productId: number, review: { rating: number; comment: string }) {
  return api.post<Review>(`/products/${productId}/reviews`, review);
}

export async function updateReview(reviewId: number, review: { rating: number; comment: string }) {
  return api.put<Review>(`/reviews/${reviewId}`, review);
}

export async function deleteReview(reviewId: number) {
  return api.delete(`/reviews/${reviewId}`);
}

export async function uploadDatasheet(
  productId: number,
  file: File | null,
  url: string | null,
  clear: boolean,
) {
  const formData = new FormData();
  if (file) formData.append("datasheetFile", file);
  if (url) formData.append("datasheetUrl", url);
  if (clear) formData.append("clearDatasheet", "true");
  return api.post<ProductResponse>(`/admin/products/${productId}/datasheet`, formData);
}

export default api;

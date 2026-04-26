import axios from "axios";
import type { AxiosInstance } from "axios";
import type { Review } from "./types";

const api: AxiosInstance = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
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
      window.location.href = "/";
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

export default api;

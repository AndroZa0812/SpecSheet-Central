import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import type { Snippet } from "svelte";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export type WithElementRef<T, RefType = HTMLElement> = Omit<T, "ref"> & {
  ref?: RefType | null;
};

export type WithoutChild<T> = Omit<T, "child" | "children"> & {
  child?: Snippet<[{ props: Record<string, unknown> }]>;
  children?: Snippet<[]>;
};

export type WithoutChildren<T> = Omit<T, "children">;

export type WithoutChildrenOrChild<T> = Omit<T, "child" | "children"> & {
  child?: Snippet<[{ props: Record<string, unknown> }]>;
  children?: Snippet<[]>;
};

export function formatPrice(price: number): string {
  return `$${price.toFixed(2)}`;
}

export function getStockStatus(quantity: number, threshold?: number): string {
  const t = threshold ?? 10;
  if (quantity === 0) return "Out of Stock";
  if (quantity < t) return "Low Stock";
  return "In Stock";
}

export function getStockVariant(quantity: number, threshold?: number): "default" | "secondary" | "destructive" {
  const t = threshold ?? 10;
  if (quantity === 0) return "destructive";
  if (quantity < t) return "secondary";
  return "default";
}

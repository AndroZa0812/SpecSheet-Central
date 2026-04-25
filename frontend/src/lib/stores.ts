import { writable, derived } from "svelte/store";
import type { AuthUser, CartItem } from "./types";

function createAuthStore() {
  const stored = localStorage.getItem("user");
  const { subscribe, set } = writable<AuthUser | null>(
    stored ? (JSON.parse(stored) as AuthUser) : null,
  );

  return {
    subscribe,
    login: (user: AuthUser, token: string) => {
      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(user));
      set(user);
    },
    logout: () => {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      set(null);
    },
  };
}

export const auth = createAuthStore();

function createCartStore() {
  const stored = localStorage.getItem("cart");
  const { subscribe, set, update } = writable<CartItem[]>(
    stored ? (JSON.parse(stored) as CartItem[]) : [],
  );

  return {
    subscribe,
    add: (product: CartItem) =>
      update((items) => {
        const existing = items.find((i: CartItem) => i.id === product.id);
        if (existing) {
          existing.quantity += 1;
        } else {
          items.push({ ...product, quantity: 1 });
        }
        localStorage.setItem("cart", JSON.stringify(items));
        return [...items];
      }),
    remove: (productId: number) =>
      update((items) => {
        const filtered = items.filter((i: CartItem) => i.id !== productId);
        localStorage.setItem("cart", JSON.stringify(filtered));
        return filtered;
      }),
    updateQuantity: (productId: number, quantity: number) =>
      update((items) => {
        const item = items.find((i: CartItem) => i.id === productId);
        if (item) {
          item.quantity = quantity;
          if (quantity <= 0) {
            return items.filter((i: CartItem) => i.id !== productId);
          }
        }
        localStorage.setItem("cart", JSON.stringify(items));
        return [...items];
      }),
    clear: () => {
      localStorage.removeItem("cart");
      set([]);
    },
  };
}

export const cart = createCartStore();

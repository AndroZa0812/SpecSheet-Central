import { writable, derived } from "svelte/store";
import type { RouteParams } from "./types";

export const currentPath = writable<string>(
  window.location.pathname + window.location.search,
);

function resolvePath(to: string): string {
  return to.startsWith("/") ? to : "/" + to;
}

export function navigate(to: string): void {
  const path = resolvePath(to);
  history.pushState({}, "", path);
  currentPath.set(window.location.pathname + window.location.search);
}

export function link(href: string) {
  return {
    href: resolvePath(href),
    onclick: (e: MouseEvent) => {
      e.preventDefault();
      navigate(href);
    },
  };
}

window.addEventListener("popstate", () => {
  currentPath.set(window.location.pathname + window.location.search);
});

export function match(pattern: string, path: string): RouteParams | null {
  const patternParts = pattern.split("/");
  const pathParts = path.split("/");
  if (patternParts.length !== pathParts.length) return null;

  const params: Record<string, string> = {};
  for (let i = 0; i < patternParts.length; i++) {
    if (patternParts[i].startsWith(":")) {
      params[patternParts[i].slice(1)] = pathParts[i];
    } else if (patternParts[i] !== pathParts[i]) {
      return null;
    }
  }
  return params;
}

function queryParams(search: string): Record<string, string> {
  const params: Record<string, string> = {};
  if (search?.startsWith("?")) {
    search
      .slice(1)
      .split("&")
      .forEach((pair) => {
        const [k, v] = pair.split("=");
        if (k) params[decodeURIComponent(k)] = decodeURIComponent(v || "");
      });
  }
  return params;
}

const query = derived(currentPath, ($p) =>
  queryParams($p.split("?")[1] ?? ""),
);

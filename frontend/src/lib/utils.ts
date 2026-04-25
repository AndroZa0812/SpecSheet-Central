export function formatPrice(price: number): string {
  return `$${price.toFixed(2)}`;
}

export function getStockStatus(quantity: number): string {
  if (quantity === 0) return "Out of Stock";
  if (quantity < 10) return "Low Stock";
  return "In Stock";
}

export function getStockClass(quantity: number): string {
  if (quantity === 0) return "out";
  if (quantity < 10) return "low";
  return "in";
}

export function formatPrice(price) {
  return `$${price.toFixed(2)}`
}

export function getStockStatus(quantity) {
  if (quantity === 0) return 'Out of Stock'
  if (quantity < 10) return 'Low Stock'
  return 'In Stock'
}

export function getStockClass(quantity) {
  if (quantity === 0) return 'out'
  if (quantity < 10) return 'low'
  return 'in'
}

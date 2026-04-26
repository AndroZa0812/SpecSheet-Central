# Expandable Orders & Stock Availability Design

**Goal:** Add expandable order rows showing order items in the admin panel, and handle stock restoration when orders are cancelled.

## Feature 1: Expandable Order Rows

### Frontend (Admin.svelte)

- Add `expandedOrderId` state variable (`number | null`)
- Each order row gets a click handler to toggle `expandedOrderId`
- When expanded, show a nested row below with a table of order items:
  - Product Name
  - Quantity
  - Unit Price (`$priceAtPurchase.toFixed(2)`)
  - Line Total (`$ (qty * priceAtPurchase).toFixed(2)`)
- Add a chevron icon to indicate expandable rows
- The `OrderResponse` type already includes `items[]` with all needed fields

### No Backend Changes Needed

The `GET /api/admin/orders` endpoint already returns order items in the response.

## Feature 2: Stock Restoration on Cancellation

### Backend (OrderService.updateStatus)

When status transitions to CANCELLED from a non-CANCELLED status:
- Iterate each OrderItem
- Add `item.quantity` back to `product.stockQuantity`
- Save each product

When status transitions FROM CANCELLED to a non-CANCELLED status:
- Iterate each OrderItem
- Re-deduct `item.quantity` from `product.stockQuantity`
- Throw error if insufficient stock (same validation as order creation)
- Save each product

This keeps `stockQuantity` as the source of truth for available stock.

### Frontend (Admin.svelte)

After status update, reload product data too (call `loadData()` which already fetches products when on the products tab, and also refresh products when on orders tab so dashboard stats are current).

## Implementation Notes

- The `updateStatus` backend method must be `@Transactional`
- Stock restoration/re-deduction must happen within the same transaction as the status update
- The admin panel should reload products after any status change to keep dashboard stats accurate
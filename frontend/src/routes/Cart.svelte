<script lang="ts">
  import { cart, auth } from "../lib/stores.js";
  import { formatPrice } from "../lib/utils.js";
  import { navigate } from "../lib/router.js";
  import api from "../lib/api.js";
  import type { OrderResponse } from "../lib/types.js";

  let error = $state("");
  let ordering = $state(false);
  let orderSuccess = $state(false);

  let total = $derived($cart.reduce((sum: number, item) => sum + item.price * item.quantity, 0));

  async function placeOrder() {
    if (!$auth) {
      navigate("/login");
      return;
    }
    ordering = true;
    error = "";
    try {
      const items = $cart.map((i) => ({ productId: i.id, quantity: i.quantity }));
      await api.post<OrderResponse>("/orders", { items });
      cart.clear();
      orderSuccess = true;
    } catch (e) {
      error = (e as any).response?.data?.message || "Order failed. Please try again.";
    } finally {
      ordering = false;
    }
  }
</script>

<div class="cart-page">
  <h1>Shopping Cart</h1>

  {#if orderSuccess}
    <div class="success">Order placed successfully!</div>
  {:else if $cart.length === 0}
    <p>Your cart is empty.</p>
    <a href="/products" class="browse-link">Browse Products</a>
  {:else}
    <div class="cart-items">
      {#each $cart as item (item.id)}
        <div class="cart-item">
          <div class="item-info">
            <h3>{item.name}</h3>
            <p class="sku">{item.sku}</p>
            <p class="price">{formatPrice(item.price)}</p>
          </div>
          <div class="item-quantity">
            <button onclick={() => cart.updateQuantity(item.id, item.quantity - 1)}>-</button>
            <span>{item.quantity}</span>
            <button onclick={() => cart.updateQuantity(item.id, item.quantity + 1)}>+</button>
          </div>
          <div class="item-total">
            {formatPrice(item.price * item.quantity)}
          </div>
          <button onclick={() => cart.remove(item.id)} class="remove-btn">&times;</button>
        </div>
      {/each}
    </div>

    <div class="cart-summary">
      <p class="total">Total: <strong>{formatPrice(total)}</strong></p>
      {#if error}<p class="error">{error}</p>{/if}
      <button onclick={placeOrder} disabled={ordering} class="order-btn">
        {ordering ? "Placing Order..." : "Place Order"}
      </button>
    </div>
  {/if}
</div>

<style>
  .cart-page { padding: 2rem; max-width: 900px; margin: 0 auto; }
  h1 { margin-bottom: 1.5rem; }
  .cart-items { display: flex; flex-direction: column; gap: 1rem; }
  .cart-item { display: flex; align-items: center; gap: 1rem; padding: 1rem; border: 1px solid var(--gray-200); border-radius: 0.5rem; }
  .item-info { flex: 1; }
  .item-info h3 { font-size: 1rem; }
  .sku { font-size: 0.8rem; color: #666; }
  .price { color: var(--primary); font-weight: 600; }
  .item-quantity { display: flex; align-items: center; gap: 0.5rem; }
  .item-quantity button { width: 2rem; height: 2rem; border: 1px solid var(--gray-200); background: white; border-radius: 0.25rem; cursor: pointer; font-size: 1rem; }
  .item-total { font-weight: 600; min-width: 80px; text-align: right; }
  .remove-btn { background: none; border: none; color: var(--danger); font-size: 1.5rem; cursor: pointer; }
  .cart-summary { margin-top: 2rem; text-align: right; }
  .total { font-size: 1.25rem; margin-bottom: 1rem; }
  .order-btn { padding: 0.75rem 2rem; background: var(--primary); color: white; border: none; border-radius: 0.375rem; font-size: 1rem; cursor: pointer; }
  .order-btn:disabled { opacity: 0.6; }
  .error { color: var(--danger); margin-bottom: 1rem; }
  .success { padding: 1rem; background: #dcfce7; color: var(--success); border-radius: 0.5rem; margin-bottom: 1rem; }
  .browse-link { display: inline-block; margin-top: 1rem; color: var(--primary); }
</style>

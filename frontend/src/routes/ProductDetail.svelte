<script lang="ts">
  import { onMount } from "svelte";
  import api from "../lib/api.js";
  import { cart } from "../lib/stores.js";
  import { navigate } from "../lib/router.js";
  import { formatPrice, getStockStatus, getStockClass } from "../lib/utils.js";
  import type { ProductResponse, RouteParams } from "../lib/types.js";

  let { params }: { params: RouteParams } = $props();
  let product: ProductResponse | null = $state(null);
  let loading = $state(true);

  onMount(() => fetchProduct(params.id));

  async function fetchProduct(id: string | undefined) {
    loading = true;
    try {
      const res = await api.get<ProductResponse>(`/products/${id}`);
      product = res.data;
    } catch (e) {
      product = null;
    } finally {
      loading = false;
    }
  }
</script>

<div class="detail">
  {#if loading}
    <p>Loading...</p>
  {:else if !product}
    <p>Product not found.</p>
  {:else}
    <button onclick={() => navigate("/products")} class="back-btn">&larr; Back to Catalog</button>
    <div class="product-detail">
      <div class="detail-image">
        {#if product.imageUrl}
          <img src={product.imageUrl} alt={product.name} />
        {:else}
          <div class="placeholder">No Image</div>
        {/if}
      </div>
      <div class="detail-info">
        <h1>{product.name}</h1>
        <p class="sku">{product.sku}</p>
        <p class="manufacturer">{product.manufacturer}</p>
        <p class="price">{formatPrice(product.price)}</p>
        <p class="stock {getStockClass(product.stockQuantity)}">
          {getStockStatus(product.stockQuantity)}
        </p>
        <button onclick={() => cart.add(product as any)} class="add-btn">Add to Cart</button>

        {#if product.datasheetUrl}
          <a href={product.datasheetUrl} target="_blank" class="datasheet-link">View Datasheet</a>
        {/if}

        {#if product.specs && Object.keys(product.specs).length > 0}
          <h3>Specifications</h3>
          <table class="specs-table">
            <thead>
              <tr><th>Key</th><th>Value</th></tr>
            </thead>
            <tbody>
              {#each Object.entries(product.specs) as [key, value]}
                <tr>
                  <td class="spec-key">{key}</td>
                  <td class="spec-value">{value}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        {/if}
      </div>
    </div>
  {/if}
</div>

<style>
  .detail { padding: 2rem; max-width: 1200px; margin: 0 auto; }
  .back-btn { background: none; border: 1px solid var(--gray-200); padding: 0.5rem 1rem; border-radius: 0.375rem; cursor: pointer; margin-bottom: 2rem; }
  .product-detail { display: flex; gap: 3rem; }
  .detail-image { width: 400px; height: 400px; background: var(--gray-100); display: flex; align-items: center; justify-content: center; border-radius: 0.5rem; }
  .detail-image img { max-width: 100%; max-height: 100%; object-fit: contain; }
  .placeholder { color: var(--gray-800); }
  .detail-info { flex: 1; }
  h1 { margin-bottom: 0.5rem; }
  .sku, .manufacturer { color: #666; font-size: 0.9rem; }
  .price { font-size: 2rem; font-weight: 700; color: var(--primary); margin: 1rem 0; }
  .stock { font-size: 1rem; font-weight: 500; margin-bottom: 1rem; }
  .stock.in { color: var(--success); }
  .stock.low { color: var(--warning); }
  .stock.out { color: var(--danger); }
  .add-btn { padding: 0.75rem 2rem; background: var(--primary); color: white; border: none; border-radius: 0.375rem; font-size: 1rem; cursor: pointer; margin-right: 1rem; }
  .add-btn:hover { background: var(--primary-dark); }
  .datasheet-link { display: inline-block; padding: 0.75rem 2rem; background: var(--gray-100); color: var(--primary); text-decoration: none; border-radius: 0.375rem; }
  h3 { margin: 2rem 0 1rem; }
  .specs-table { width: 100%; border-collapse: collapse; }
  .specs-table th, .specs-table td { padding: 0.75rem; border-bottom: 1px solid var(--gray-200); text-align: left; }
  .spec-key { font-weight: 600; width: 40%; background: var(--gray-100); }
</style>

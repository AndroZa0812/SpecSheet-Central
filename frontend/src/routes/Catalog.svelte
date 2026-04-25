<script lang="ts">
  import { onMount } from "svelte";
  import api from "../lib/api.js";
  import { cart } from "../lib/stores.js";
  import Link from "../components/Link.svelte";
  import { formatPrice, getStockStatus, getStockClass } from "../lib/utils.js";
  import type { ProductResponse, Category } from "../lib/types.js";

  let products: ProductResponse[] = $state([]);
  let categories: Category[] = $state([]);
  let loading = $state(true);
  let search = $state("");
  let selectedCategory = $state("");
  let selectedManufacturer = $state("");

  async function fetchProducts() {
    loading = true;
    try {
      const params: Record<string, string> = {};
      if (search) params.search = search;
      if (selectedCategory) params.categoryId = selectedCategory;
      if (selectedManufacturer) params.manufacturer = selectedManufacturer;
      const [prodRes, catRes] = await Promise.all([
        api.get<ProductResponse[]>("/products", { params }),
        api.get<Category[]>("/categories"),
      ]);
      products = prodRes.data;
      categories = catRes.data;
    } catch (e) {
      console.error("Failed to load products", e);
    } finally {
      loading = false;
    }
  }

  onMount(fetchProducts);
</script>

<div class="catalog">
  <aside class="filters">
    <h3>Filters</h3>
    <input type="text" placeholder="Search..." bind:value={search} oninput={fetchProducts} />
    <select bind:value={selectedCategory} onchange={fetchProducts}>
      <option value="">All Categories</option>
      {#each categories as cat}
        <option value={cat.id}>{cat.name}</option>
      {/each}
    </select>
    <select bind:value={selectedManufacturer} onchange={fetchProducts}>
      <option value="">All Manufacturers</option>
      {#each [...new Set(products.map(p => p.manufacturer).filter(Boolean))] as mfr}
        <option value={mfr}>{mfr}</option>
      {/each}
    </select>
  </aside>

  <div class="product-grid">
    {#if loading}
      <p>Loading...</p>
    {:else if products.length === 0}
      <p>No products found.</p>
    {:else}
      {#each products as product}
        <div class="product-card">
          <Link to="/products/{product.id}">
            <div class="card-image">
              {#if product.imageUrl}
                <img src={product.imageUrl} alt={product.name} />
              {:else}
                <div class="placeholder">No Image</div>
              {/if}
            </div>
            <div class="card-body">
              <h4>{product.name}</h4>
              <p class="sku">{product.sku}</p>
              <p class="price">{formatPrice(product.price)}</p>
              <p class="stock {getStockClass(product.stockQuantity)}">
                {getStockStatus(product.stockQuantity)}
              </p>
            </div>
          </Link>
          <button onclick={() => cart.add(product as any)} class="add-btn">
            Add to Cart
          </button>
        </div>
      {/each}
    {/if}
  </div>
</div>

<style>
  .catalog { display: flex; gap: 2rem; padding: 2rem; }
  .filters { width: 250px; display: flex; flex-direction: column; gap: 1rem; }
  .filters h3 { margin-bottom: 0.5rem; }
  .filters input, .filters select { padding: 0.5rem; border: 1px solid var(--gray-200); border-radius: 0.375rem; }
  .product-grid { flex: 1; display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1.5rem; }
  .product-card { border: 1px solid var(--gray-200); border-radius: 0.5rem; overflow: hidden; transition: box-shadow 0.2s; }
  .product-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
  .product-card a { text-decoration: none; color: inherit; }
  .card-image { height: 160px; background: var(--gray-100); display: flex; align-items: center; justify-content: center; }
  .card-image img { max-width: 100%; max-height: 100%; object-fit: contain; }
  .placeholder { color: var(--gray-800); font-size: 0.85rem; }
  .card-body { padding: 1rem; }
  .card-body h4 { font-size: 1rem; margin-bottom: 0.25rem; }
  .sku { font-size: 0.8rem; color: #666; }
  .price { font-size: 1.25rem; font-weight: 700; color: var(--primary); margin: 0.5rem 0; }
  .stock { font-size: 0.85rem; font-weight: 500; }
  .stock.in { color: var(--success); }
  .stock.low { color: var(--warning); }
  .stock.out { color: var(--danger); }
  .add-btn { width: 100%; padding: 0.6rem; background: var(--primary); color: white; border: none; cursor: pointer; font-size: 0.9rem; }
  .add-btn:hover { background: var(--primary-dark); }
</style>

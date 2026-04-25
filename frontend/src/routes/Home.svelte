<script lang="ts">
  import { onMount } from "svelte";
  import Link from "../components/Link.svelte";
  import api from "../lib/api.js";
  import type { Category } from "../lib/types.js";

  let categories: Category[] = $state([]);

  onMount(async () => {
    const res = await api.get<Category[]>("/categories");
    categories = res.data;
  });
</script>

<div class="home">
  <section class="hero">
    <div class="hero-content">
      <span class="badge">New Arrivals Weekly</span>
      <h1>Premium Electronic Components for Your Projects</h1>
      <p>Browse thousands of quality components from Arduino boards to sensors, LEDs, and more. Fast shipping and expert support.</p>
      <div class="hero-buttons">
        <Link to="/products" class="btn btn-primary">Shop Now</Link>
        <Link to="/products" class="btn btn-secondary">Learn More</Link>
      </div>
    </div>
  </section>

  <section class="categories">
    <h2>Shop by Category</h2>
    <div class="category-grid">
      {#each categories as category}
        <Link to={`/products?categoryId=${category.id}`} class="category-card">
          <div class="category-icon">
            {#if category.name === "Microcontrollers"}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 9h6v6H9z"/></svg>
            {:else if category.name === "Sensors"}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
            {:else if category.name === "LEDs"}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><path d="M9 18h6M10 22h4M12 2v1M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"/></svg>
            {:else}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#dc2626" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            {/if}
          </div>
          <span class="category-name">{category.name}</span>
        </Link>
      {/each}
    </div>
  </section>
</div>

<style>
  .hero {
    background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
    color: white;
    padding: 4rem 2rem;
  }
  .hero-content { max-width: 1200px; margin: 0 auto; }
  .badge {
    display: inline-block;
    background: rgba(255,255,255,0.2);
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.875rem;
    margin-bottom: 1rem;
  }
  h1 { font-size: 3rem; font-weight: 700; max-width: 600px; line-height: 1.2; margin-bottom: 1rem; }
  p { font-size: 1.125rem; max-width: 500px; opacity: 0.9; margin-bottom: 2rem; }
  .hero-buttons { display: flex; gap: 1rem; }
  .hero-buttons :global(.btn) { padding: 0.75rem 1.5rem; border-radius: 8px; text-decoration: none; font-weight: 500; }
  .hero-buttons :global(.btn-primary) { background: white; color: var(--primary); }
  .hero-buttons :global(.btn-secondary) { background: transparent; color: white; border: 1px solid white; }
  .categories { max-width: 1200px; margin: 0 auto; padding: 3rem 2rem; }
  .categories h2 { font-size: 1.5rem; margin-bottom: 1.5rem; }
  .category-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; }
  .category-card {
    display: flex; flex-direction: column; align-items: center; gap: 0.75rem;
    padding: 1.5rem; border: 1px solid var(--gray-200); border-radius: 12px;
    text-decoration: none; color: var(--gray-900); transition: box-shadow 0.2s;
  }
  .category-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
  .category-icon { width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; background: var(--gray-100); border-radius: 12px; }
  .category-name { font-weight: 500; }
</style>

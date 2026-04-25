<script>
  import { onMount } from 'svelte'
  import api from '../lib/api.js'
  import { formatPrice } from '../lib/utils.js'

  let selectedIds = $state([])
  let products = $state([])
  let categories = $state([])
  let loading = $state(false)

  let allSpecKeys = $derived([...new Set(products.flatMap(p => Object.keys(p.specs || {})))])

  onMount(async () => {
    const res = await api.get('/categories')
    categories = res.data
  })

  async function loadProducts() {
    if (selectedIds.length === 0) {
      products = []
      return
    }
    loading = true
    try {
      const results = await Promise.all(
        selectedIds.map(id => api.get(`/products/${id}`).catch(() => null))
      )
      products = results.filter(Boolean).map(r => r.data)
    } catch (e) {
      console.error('Failed to load products for comparison', e)
    } finally {
      loading = false
    }
  }

  function addProduct(id) {
    if (!selectedIds.includes(id)) {
      selectedIds = [...selectedIds, id]
      loadProducts()
    }
  }

  function removeProduct(id) {
    selectedIds = selectedIds.filter(s => s !== id)
    loadProducts()
  }

  let inputId = $state('')
  function handleAdd() {
    const id = parseInt(inputId)
    if (id) addProduct(id)
    inputId = ''
  }
</script>

<div class="compare-page">
  <h1>Compare Products</h1>

  <div class="add-controls">
    <input type="number" bind:value={inputId} placeholder="Enter Product ID" />
    <button onclick={handleAdd}>Add</button>
  </div>

  {#if loading}
    <p>Loading...</p>
  {:else if products.length === 0}
    <p>Add products by ID to compare them side by side.</p>
  {:else}
    <div class="comparison-table-wrapper">
      <table class="comparison-table">
        <thead>
          <tr>
            <th></th>
            {#each products as product}
              <th>
                {product.name}
                <button onclick={() => removeProduct(product.id)} class="remove-btn">&times;</button>
              </th>
            {/each}
          </tr>
        </thead>
        <tbody>
          <tr>
            <td class="label">Price</td>
            {#each products as product}
              <td>{formatPrice(product.price)}</td>
            {/each}
          </tr>
          <tr>
            <td class="label">SKU</td>
            {#each products as product}
              <td>{product.sku}</td>
            {/each}
          </tr>
          <tr>
            <td class="label">Manufacturer</td>
            {#each products as product}
              <td>{product.manufacturer}</td>
            {/each}
          </tr>
          <tr>
            <td class="label">Stock</td>
            {#each products as product}
              <td>{product.stockQuantity}</td>
            {/each}
          </tr>
          {#each allSpecKeys as key}
            <tr>
              <td class="label">{key}</td>
              {#each products as product}
                <td>{product.specs?.[key] || '-'}</td>
              {/each}
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<style>
  .compare-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
  h1 { margin-bottom: 1.5rem; }
  .add-controls { display: flex; gap: 0.5rem; margin-bottom: 2rem; }
  .add-controls input { padding: 0.5rem; border: 1px solid var(--gray-200); border-radius: 0.375rem; width: 200px; }
  .add-controls button { padding: 0.5rem 1rem; background: var(--primary); color: white; border: none; border-radius: 0.375rem; cursor: pointer; }
  .comparison-table-wrapper { overflow-x: auto; }
  .comparison-table { width: 100%; border-collapse: collapse; }
  .comparison-table th, .comparison-table td { padding: 1rem; border: 1px solid var(--gray-200); text-align: center; min-width: 150px; }
  .comparison-table th { background: var(--gray-900); color: white; }
  .label { font-weight: 600; text-align: left !important; background: var(--gray-100); }
  .remove-btn { background: none; border: none; color: var(--danger); font-size: 1.2rem; cursor: pointer; margin-left: 0.5rem; }
</style>

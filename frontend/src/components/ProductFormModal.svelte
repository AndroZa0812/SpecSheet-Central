<script>
  import { onMount } from 'svelte'
  import api from '../lib/api.js'

  let { product, categories, onsave, onclose } = $props()

  let form = $state({
    name: '',
    sku: '',
    price: '',
    stockQuantity: '',
    categoryId: '',
    manufacturer: '',
    imageUrl: '',
    datasheetUrl: '',
    specs: []
  })

  onMount(() => {
    if (product) {
      form = {
        name: product.name,
        sku: product.sku,
        price: String(product.price),
        stockQuantity: String(product.stockQuantity),
        categoryId: String(product.categoryId || ''),
        manufacturer: product.manufacturer || '',
        imageUrl: product.imageUrl || '',
        datasheetUrl: product.datasheetUrl || '',
        specs: Object.entries(product.specs || {}).map(([k, v]) => ({ key: k, value: v }))
      }
    }
  })

  function addSpec() {
    form.specs = [...form.specs, { key: '', value: '' }]
  }

  function removeSpec(index) {
    form.specs.splice(index, 1)
    form.specs = [...form.specs]
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const data = {
      name: form.name,
      sku: form.sku,
      price: parseFloat(form.price),
      stockQuantity: parseInt(form.stockQuantity),
      categoryId: parseInt(form.categoryId),
      manufacturer: form.manufacturer,
      imageUrl: form.imageUrl,
      datasheetUrl: form.datasheetUrl,
      specs: Object.fromEntries(form.specs.filter(s => s.key).map(s => [s.key, s.value]))
    }

    try {
      if (product) {
        await api.put(`/products/${product.id}`, data)
      } else {
        await api.post('/products', data)
      }
      onsave()
    } catch (e) {
      alert(e.response?.data?.message || 'Save failed')
    }
  }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="modal-backdrop" onclick={onclose}>
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div class="modal" onclick={(e) => e.stopPropagation()}>
    <div class="modal-header">
      <h2>{product ? 'Edit Product' : 'Add Product'}</h2>
      <button class="btn-close" onclick={onclose}>×</button>
    </div>

    <form onsubmit={handleSubmit} class="modal-body">
      <div class="form-grid">
        <label>
          Name
          <input type="text" bind:value={form.name} required />
        </label>
        <label>
          SKU
          <input type="text" bind:value={form.sku} required />
        </label>
        <label>
          Price
          <input type="number" step="0.01" bind:value={form.price} required />
        </label>
        <label>
          Stock Quantity
          <input type="number" bind:value={form.stockQuantity} required />
        </label>
        <label>
          Category
          <select bind:value={form.categoryId} required>
            <option value="">Select...</option>
            {#each categories as cat}
              <option value={cat.id}>{cat.name}</option>
            {/each}
          </select>
        </label>
        <label>
          Manufacturer
          <input type="text" bind:value={form.manufacturer} />
        </label>
        <label>
          Image URL
          <input type="text" bind:value={form.imageUrl} />
        </label>
        <label>
          Datasheet URL
          <input type="text" bind:value={form.datasheetUrl} />
        </label>
      </div>

      <div class="specs-section">
        <div class="specs-header">
          <h3>Technical Specifications</h3>
          <button type="button" class="btn-add-spec" onclick={addSpec}>+ Add Spec</button>
        </div>
        {#each form.specs as spec, i}
          <div class="spec-row">
            <input type="text" placeholder="Key" bind:value={spec.key} />
            <input type="text" placeholder="Value" bind:value={spec.value} />
            <button type="button" class="btn-remove-spec" onclick={() => removeSpec(i)}>×</button>
          </div>
        {/each}
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-cancel" onclick={onclose}>Cancel</button>
        <button type="submit" class="btn btn-primary">Save Product</button>
      </div>
    </form>
  </div>
</div>

<style>
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 200;
  }
  .modal {
    background: white;
    border-radius: 12px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
  }
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid var(--gray-200);
  }
  .modal-header h2 { font-size: 1.25rem; }
  .btn-close { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: var(--gray-800); }
  .modal-body { padding: 1.5rem; }
  .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
  label { display: block; font-size: 0.875rem; font-weight: 500; }
  label input, label select { display: block; width: 100%; margin-top: 0.25rem; padding: 0.5rem; border: 1px solid var(--gray-200); border-radius: 6px; }
  .specs-section { margin-top: 1.5rem; }
  .specs-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
  .specs-header h3 { font-size: 1rem; }
  .btn-add-spec { font-size: 0.875rem; background: var(--primary); color: white; border: none; padding: 0.375rem 0.75rem; border-radius: 6px; cursor: pointer; }
  .spec-row { display: flex; gap: 0.5rem; margin-bottom: 0.5rem; }
  .spec-row input { flex: 1; padding: 0.375rem; border: 1px solid var(--gray-200); border-radius: 4px; }
  .btn-remove-spec { background: none; border: none; font-size: 1.25rem; cursor: pointer; color: var(--danger); padding: 0 0.5rem; }
  .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid var(--gray-200); }
  .btn { padding: 0.75rem 1.5rem; border-radius: 8px; border: none; cursor: pointer; font-weight: 500; }
  .btn-cancel { background: var(--gray-100); color: var(--gray-800); }
  .btn-primary { background: var(--primary); color: white; }
</style>

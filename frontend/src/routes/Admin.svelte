<script>
  import { onMount } from 'svelte'
  import api from '../lib/api.js'
  import { auth } from '../lib/stores.js'
  import { navigate } from '../lib/router.js'
  import ProductFormModal from '../components/ProductFormModal.svelte'

  let activeTab = $state('products')
  let products = $state([])
  let categories = $state([])
  let orders = $state([])
  let loading = $state(true)
  let showModal = $state(false)
  let editingProduct = $state(null)

  onMount(() => {
    if (!$auth || $auth.role !== 'ADMIN') {
      navigate('/')
    }
    loadData()
  })

  async function loadData() {
    loading = true
    try {
      if (activeTab === 'products') {
        const [prodRes, catRes] = await Promise.all([
          api.get('/products'),
          api.get('/categories')
        ])
        products = prodRes.data
        categories = catRes.data
      } else if (activeTab === 'orders') {
        const res = await api.get('/orders')
        orders = res.data
      }
    } catch (e) {
      console.error('Failed to load admin data', e)
    } finally {
      loading = false
    }
  }

  function openCreate() {
    editingProduct = null
    showModal = true
  }

  function openEdit(product) {
    editingProduct = product
    showModal = true
  }

  async function handleSave() {
    showModal = false
    loadData()
  }

  async function handleDelete(id) {
    if (confirm('Delete this product?')) {
      await api.delete(`/products/${id}`)
      loadData()
    }
  }

  async function updateStock(id, quantity) {
    try {
      await api.patch(`/products/${id}/stock`, null, { params: { quantity } })
      loadData()
    } catch (e) {
      alert('Failed to update stock')
    }
  }

  async function updateOrderStatus(id, status) {
    try {
      await api.patch(`/orders/${id}/status`, null, { params: { status } })
      loadData()
    } catch (e) {
      alert('Failed to update order status')
    }
  }
</script>

<div class="admin-page">
  <h1>Admin Panel</h1>

  <div class="tabs">
    <button class:active={activeTab === 'products'} onclick={() => { activeTab = 'products'; loadData() }}>Products</button>
    <button class:active={activeTab === 'orders'} onclick={() => { activeTab = 'orders'; loadData() }}>Orders</button>
  </div>

  {#if activeTab === 'products'}
    <button onclick={openCreate} class="create-btn">+ Add Product</button>

    {#if loading}
      <p>Loading...</p>
    {:else}
      <table class="admin-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>SKU</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Category</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each products as product}
            <tr>
              <td>{product.name}</td>
              <td>{product.sku}</td>
              <td>${product.price}</td>
              <td>
                <input
                  type="number"
                  value={product.stockQuantity}
                  onchange={(e) => updateStock(product.id, parseInt(e.target.value))}
                  class="stock-input"
                />
              </td>
              <td>{product.categoryName}</td>
              <td class="actions">
                <button onclick={() => openEdit(product)} class="edit-btn">Edit</button>
                <button onclick={() => handleDelete(product.id)} class="delete-btn">Delete</button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  {/if}

  {#if activeTab === 'orders'}
    {#if loading}
      <p>Loading...</p>
    {:else}
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>User</th>
            <th>Date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each orders as order}
            <tr>
              <td>#{order.id}</td>
              <td>{order.userEmail}</td>
              <td>{new Date(order.orderDate).toLocaleDateString()}</td>
              <td>${order.totalAmount.toFixed(2)}</td>
              <td>{order.status}</td>
              <td>
                <select onchange={(e) => updateOrderStatus(order.id, e.target.value)} value={order.status}>
                  <option value="PENDING">PENDING</option>
                  <option value="IN_DELIVERY">IN_DELIVERY</option>
                  <option value="DELIVERED">DELIVERED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  {/if}
</div>

{#if showModal}
  <ProductFormModal
    product={editingProduct}
    categories={categories}
    onsave={handleSave}
    onclose={() => showModal = false}
  />
{/if}

<style>
  .admin-page { padding: 2rem; max-width: 1200px; margin: 0 auto; }
  h1 { margin-bottom: 1.5rem; }
  .tabs { display: flex; gap: 0; margin-bottom: 1.5rem; }
  .tabs button { padding: 0.75rem 1.5rem; border: 1px solid var(--gray-200); background: white; cursor: pointer; }
  .tabs button:first-child { border-radius: 0.375rem 0 0 0.375rem; }
  .tabs button:last-child { border-radius: 0 0.375rem 0.375rem 0; }
  .tabs button.active { background: var(--primary); color: white; border-color: var(--primary); }
  .create-btn { margin-bottom: 1rem; padding: 0.5rem 1rem; background: var(--success); color: white; border: none; border-radius: 0.375rem; cursor: pointer; }
  .admin-table { width: 100%; border-collapse: collapse; }
  .admin-table th, .admin-table td { padding: 0.75rem; border-bottom: 1px solid var(--gray-200); text-align: left; }
  .admin-table th { background: var(--gray-100); font-weight: 600; }
  .stock-input { width: 70px; padding: 0.25rem; border: 1px solid var(--gray-200); border-radius: 0.25rem; }
  .actions { display: flex; gap: 0.5rem; }
  .edit-btn, .delete-btn { padding: 0.3rem 0.75rem; border: none; border-radius: 0.25rem; cursor: pointer; font-size: 0.85rem; }
  .edit-btn { background: var(--primary); color: white; }
  .delete-btn { background: var(--danger); color: white; }
</style>

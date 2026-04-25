<script>
  import { Link } from 'svelte-routing'
  import { auth, cart } from '../lib/stores.js'
  import { navigate } from 'svelte-routing'

  let cartCount = 0
  cart.subscribe(items => {
    cartCount = items.reduce((sum, i) => sum + i.quantity, 0)
  })

  function handleLogout() {
    auth.logout()
    navigate('/')
  }

  let searchQuery = ''
  function handleSearch(e) {
    if (e.key === 'Enter' && searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`)
    }
  }
</script>

<nav class="navbar">
  <div class="nav-brand">
    <Link to="/">
      <span class="logo">S</span>
      <span class="brand-text">SpecSheet Central</span>
    </Link>
  </div>

  <div class="nav-links">
    <Link to="/">Home</Link>
    <Link to="/products">Products</Link>
    <Link to="/compare">Compare</Link>
    {#if $auth?.role === 'ADMIN'}
      <Link to="/admin">Admin</Link>
    {/if}
  </div>

  <div class="nav-search">
    <input
      type="text"
      placeholder="Search for components..."
      bind:value={searchQuery}
      on:keydown={handleSearch}
    />
  </div>

  <div class="nav-actions">
    {#if $auth}
      <span class="user-email">{$auth.email}</span>
      <button on:click={handleLogout} class="btn-link">Logout</button>
    {:else}
      <Link to="/login">Login</Link>
      <Link to="/register">Register</Link>
    {/if}
    <Link to="/cart" class="cart-link">
      Cart ({cartCount})
    </Link>
  </div>
</nav>

<style>
  .navbar {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    padding: 0.75rem 2rem;
    background: var(--gray-900);
    color: white;
    position: sticky;
    top: 0;
    z-index: 100;
  }
  .nav-brand a {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    color: white;
  }
  .logo {
    width: 2rem;
    height: 2rem;
    background: var(--primary);
    border-radius: 0.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
  }
  .brand-text { font-weight: 600; font-size: 1.1rem; }
  .nav-links { display: flex; gap: 1rem; }
  .nav-links a { color: var(--gray-200); text-decoration: none; font-size: 0.9rem; }
  .nav-links a:hover { color: white; }
  .nav-search { flex: 1; max-width: 400px; }
  .nav-search input {
    width: 100%;
    padding: 0.5rem 1rem;
    border-radius: 0.5rem;
    border: none;
    font-size: 0.9rem;
  }
  .nav-actions { display: flex; align-items: center; gap: 1rem; }
  .nav-actions a, .btn-link {
    color: var(--gray-200);
    text-decoration: none;
    font-size: 0.9rem;
    background: none;
    border: none;
    cursor: pointer;
  }
  .nav-actions a:hover, .btn-link:hover { color: white; }
  .user-email { font-size: 0.85rem; color: var(--gray-200); }
  .cart-link { font-weight: 600; color: var(--primary) !important; }
</style>
